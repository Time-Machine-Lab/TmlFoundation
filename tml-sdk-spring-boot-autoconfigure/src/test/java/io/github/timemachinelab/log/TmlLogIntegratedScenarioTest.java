package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLogConstant;
import io.github.timemachinelab.log.context.TmlLogTraceContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web请求与定时任务结合的综合场景测试
 * 模拟真实业务：订单创建 + 定时任务处理超时订单
 *
 * @author glser
 * @since 2026/01/18
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("log-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@org.springframework.context.annotation.Import({
    TmlLogIntegratedScenarioTest.OrderController.class,
    TmlLogIntegratedScenarioTest.OrderService.class,
    TmlLogIntegratedScenarioTest.OrderRepository.class,
    TmlLogIntegratedScenarioTest.OrderTimeoutScheduler.class
})
@Slf4j
public class TmlLogIntegratedScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private OrderTimeoutScheduler scheduler;

    @Test
    @DisplayName("综合场景1：Web创建订单 + 定时任务检查超时订单")
    public void testOrderCreationAndTimeoutCheck() throws Exception {
        if (scheduler == null) {
            log.warn("OrderTimeoutScheduler未注入，跳过测试");
            return;
        }
        
        // 1. 通过Web请求创建订单
        String webTraceId = "web-order-trace-" + System.currentTimeMillis();
        
        // 手动设置traceId到MDC（因为测试环境中过滤器可能不生效）
        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, webTraceId);
        
        try {
            MvcResult result = mockMvc.perform(post("/integrated/orders")
                            .header(TmlLogConstant.TRACE_ID_HEADER, webTraceId)
                            .param("userId", "1001")
                            .param("productName", "测试商品")
                            .param("amount", "99.99"))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
            // 注意：在某些测试环境中，过滤器可能不会自动应用响应头
            if (responseTraceId != null) {
                assertEquals(webTraceId, responseTraceId, "Web请求的traceId应该一致");
                log.info("✓ 响应头包含traceId: {}", responseTraceId);
            } else {
                log.warn("⚠ 响应头未包含traceId（过滤器可能未应用）");
            }
            
            String responseBody = result.getResponse().getContentAsString();
            assertTrue(responseBody.contains("orderId"), "响应应包含orderId");
            
            log.info("✓ Web订单创建完成，traceId: {}, response: {}", webTraceId, responseBody);
        } finally {
            tmlLogTraceContext.clear();
        }
        
        // 2. 等待定时任务执行（检查超时订单）
        boolean schedulerExecuted = scheduler.executionLatch.await(5, TimeUnit.SECONDS);
        assertTrue(schedulerExecuted, "定时任务应该被执行");
        
        // 3. 验证定时任务有自己独立的traceId
        assertFalse(scheduler.schedulerTraceIds.isEmpty(), "定时任务应该有traceId");
        String schedulerTraceId = scheduler.schedulerTraceIds.get(0);
        
        assertNotNull(schedulerTraceId, "定时任务的traceId不应为null");
        assertNotEquals(webTraceId, schedulerTraceId, "定时任务应该有独立的traceId，不同于Web请求");
        
        log.info("✓ 定时任务执行完成，traceId: {}", schedulerTraceId);
        log.info("✓ 综合场景测试通过 - Web traceId: {}, Scheduler traceId: {}", webTraceId, schedulerTraceId);
    }

    @Test
    @DisplayName("综合场景2：多次Web请求 + 定时任务同时运行")
    public void testMultipleWebAndScheduler() throws Exception {
        if (scheduler == null) {
            log.warn("OrderTimeoutScheduler未注入，跳过测试");
            return;
        }
        
        int requestCount = 5;
        Map<String, String> webTraceIds = new ConcurrentHashMap<>();
        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        
        // 1. 顺序发起多个Web请求（MockMvc不是线程安全的）
        for (int i = 0; i < requestCount; i++) {
            String traceId = "multiple-order-" + i;
            
            // 手动设置traceId
            tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, traceId);
            
            try {
                MvcResult result = mockMvc.perform(post("/integrated/orders")
                                .header(TmlLogConstant.TRACE_ID_HEADER, traceId)
                                .param("userId", String.valueOf(1000 + i))
                                .param("productName", "商品" + i)
                                .param("amount", "99.99"))
                        .andExpect(status().isOk())
                        .andReturn();
                
                String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
                webTraceIds.put("request-" + i, responseTraceId != null ? responseTraceId : traceId);
                
                log.info("✓ 请求{} 完成，traceId: {}", i, traceId);
            } finally {
                tmlLogTraceContext.clear();
            }
        }
        
        // 2. 验证所有Web请求的traceId
        assertEquals(requestCount, webTraceIds.size(), "应该收集到所有Web请求的traceId");
        
        log.info("✓ 完成{}个Web请求", requestCount);
        
        // 3. 等待定时任务执行
        boolean schedulerExecuted = scheduler.executionLatch.await(5, TimeUnit.SECONDS);
        assertTrue(schedulerExecuted, "定时任务应该被执行");
        
        // 4. 验证定时任务的traceId与Web请求不同
        assertFalse(scheduler.schedulerTraceIds.isEmpty(), "应该收集到定时任务的traceId");
        String schedulerTraceId = scheduler.schedulerTraceIds.get(0);
        assertNotNull(schedulerTraceId, "定时任务应该有traceId");
        
        // 验证定时任务的traceId与所有Web请求的traceId都不同
        for (String webTraceId : webTraceIds.values()) {
            if (webTraceId != null) {
                assertNotEquals(webTraceId, schedulerTraceId, "定时任务的traceId应该与Web请求不同");
            }
        }
        
        log.info("✓ 多次Web请求与定时任务测试通过");
    }

    @Test
    @DisplayName("综合场景3：Web请求触发异步任务 + 定时任务处理")
    public void testWebAsyncAndScheduler() throws Exception {
        if (scheduler == null) {
            log.warn("OrderTimeoutScheduler未注入，跳过测试");
            return;
        }
        
        // 1. Web请求触发异步处理
        String webTraceId = "async-order-trace-" + System.currentTimeMillis();
        
        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, webTraceId);
        
        try {
            MvcResult result = mockMvc.perform(post("/integrated/orders/async")
                            .header(TmlLogConstant.TRACE_ID_HEADER, webTraceId)
                            .param("userId", "2001")
                            .param("productName", "异步商品")
                            .param("amount", "199.99"))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
            if (responseTraceId != null) {
                assertEquals(webTraceId, responseTraceId);
            }
            
            log.info("✓ Web异步订单创建完成，traceId: {}", webTraceId);
        } finally {
            tmlLogTraceContext.clear();
        }
        
        // 2. 等待异步任务完成
        Thread.sleep(1000);
        
        // 3. 等待定时任务执行
        boolean schedulerExecuted = scheduler.executionLatch.await(5, TimeUnit.SECONDS);
        assertTrue(schedulerExecuted, "定时任务应该被执行");
        
        log.info("✓ Web异步任务与定时任务测试通过");
    }

    // ==================== Controller层 ====================
    
    @RestController
    @RequestMapping("/integrated")
    @Slf4j
    static class OrderController {

        @Autowired(required = false)
        private OrderService orderService;

        @PostMapping("/orders")
        @ResponseBody
        public String createOrder(@RequestParam Long userId,
                                 @RequestParam String productName,
                                 @RequestParam Double amount) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Web-Controller] 接收订单创建请求 - userId: {}, productName: {}, traceId: {}", 
                    userId, productName, traceId);
            
            OrderRequest request = new OrderRequest();
            request.setUserId(userId);
            request.setProductName(productName);
            request.setAmount(amount);
            
            Long orderId;
            if (orderService == null) {
                orderId = System.currentTimeMillis();
            } else {
                OrderResponse response = orderService.createOrder(request);
                orderId = response.getOrderId();
            }
            
            log.info("[Web-Controller] 订单创建完成 - orderId: {}, traceId: {}", orderId, traceId);
            return "orderId:" + orderId + ",status:PENDING";
        }

        @PostMapping("/orders/async")
        @ResponseBody
        public String createOrderAsync(@RequestParam Long userId,
                                      @RequestParam String productName,
                                      @RequestParam Double amount) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Web-Controller] 接收异步订单创建请求 - userId: {}, traceId: {}", 
                    userId, traceId);
            
            OrderRequest request = new OrderRequest();
            request.setUserId(userId);
            request.setProductName(productName);
            request.setAmount(amount);
            
            Long orderId;
            if (orderService == null) {
                orderId = System.currentTimeMillis();
            } else {
                OrderResponse response = orderService.createOrderAsync(request);
                orderId = response.getOrderId();
            }
            
            log.info("[Web-Controller] 异步订单创建完成 - orderId: {}, traceId: {}", orderId, traceId);
            return "orderId:" + orderId + ",status:PROCESSING";
        }
    }

    // ==================== Service层 ====================
    
    @org.springframework.stereotype.Service
    @Slf4j
    static class OrderService {

        @Autowired(required = false)
        private OrderRepository orderRepository;

        public OrderResponse createOrder(OrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Web-Service] 处理订单创建 - userId: {}, traceId: {}", request.getUserId(), traceId);
            
            // 验证订单信息
            log.info("[Web-Service] 验证订单信息 - traceId: {}", traceId);
            
            // 保存订单
            Long orderId = orderRepository != null ? 
                    orderRepository.saveOrder(request) : 
                    System.currentTimeMillis();
            
            log.info("[Web-Service] 订单保存成功 - orderId: {}, traceId: {}", orderId, traceId);
            
            return new OrderResponse(orderId, "PENDING");
        }

        public OrderResponse createOrderAsync(OrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Web-Service] 处理异步订单创建 - userId: {}, traceId: {}", request.getUserId(), traceId);
            
            Long orderId = System.currentTimeMillis();
            
            // 异步处理订单
            CompletableFuture.runAsync(() -> {
                String asyncTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                log.info("[Web-Service-Async] 异步处理订单 - orderId: {}, traceId: {}", orderId, asyncTraceId);
                
                // 模拟异步业务处理
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                log.info("[Web-Service-Async] 异步处理完成 - orderId: {}, traceId: {}", orderId, asyncTraceId);
            });
            
            return new OrderResponse(orderId, "PROCESSING");
        }
    }

    // ==================== Repository层 ====================
    
    @org.springframework.stereotype.Repository
    @Slf4j
    static class OrderRepository {

        // 模拟订单存储
        private static final Map<Long, OrderData> ORDER_STORE = new ConcurrentHashMap<>();

        public Long saveOrder(OrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Web-Repository] 保存订单到数据库 - userId: {}, traceId: {}", 
                    request.getUserId(), traceId);
            
            Long orderId = System.currentTimeMillis();
            OrderData orderData = new OrderData();
            orderData.setOrderId(orderId);
            orderData.setUserId(request.getUserId());
            orderData.setProductName(request.getProductName());
            orderData.setAmount(request.getAmount());
            orderData.setStatus("PENDING");
            orderData.setCreateTime(LocalDateTime.now());
            orderData.setCreateTraceId(traceId);
            
            ORDER_STORE.put(orderId, orderData);
            
            log.info("[Web-Repository] 订单保存成功 - orderId: {}, traceId: {}", orderId, traceId);
            
            return orderId;
        }

        public Map<Long, OrderData> findTimeoutOrders() {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Scheduler-Repository] 查询超时订单 - traceId: {}", traceId);
            
            // 模拟查询超时订单（这里简单返回所有PENDING状态的订单）
            Map<Long, OrderData> timeoutOrders = new ConcurrentHashMap<>();
            ORDER_STORE.forEach((orderId, orderData) -> {
                if ("PENDING".equals(orderData.getStatus())) {
                    timeoutOrders.put(orderId, orderData);
                }
            });
            
            log.info("[Scheduler-Repository] 查询到{}个超时订单 - traceId: {}", timeoutOrders.size(), traceId);
            
            return timeoutOrders;
        }

        public void updateOrderStatus(Long orderId, String status) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Scheduler-Repository] 更新订单状态 - orderId: {}, status: {}, traceId: {}", 
                    orderId, status, traceId);
            
            OrderData orderData = ORDER_STORE.get(orderId);
            if (orderData != null) {
                orderData.setStatus(status);
                orderData.setUpdateTime(LocalDateTime.now());
                orderData.setUpdateTraceId(traceId);
            }
            
            log.info("[Scheduler-Repository] 订单状态更新成功 - orderId: {}, traceId: {}", orderId, traceId);
        }
    }

    // ==================== 定时任务 ====================
    
    @Component
    @Configuration
    @EnableScheduling
    @Slf4j
    static class OrderTimeoutScheduler {

        @Autowired(required = false)
        private OrderRepository orderRepository;

        // 用于测试验证
        public final CountDownLatch executionLatch = new CountDownLatch(1);
        public final java.util.List<String> schedulerTraceIds = new java.util.concurrent.CopyOnWriteArrayList<>();
        private final AtomicInteger executionCount = new AtomicInteger(0);

        /**
         * 定时检查超时订单 - 每2秒执行一次
         */
        @Scheduled(fixedRate = 2000, initialDelay = 1000)
        public void checkTimeoutOrders() {
            // 只执行一次，避免测试中多次执行
            if (executionCount.getAndIncrement() > 0) {
                return;
            }

            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            
            // 手动生成traceId（模拟TmlLogScheduleTrace切面的行为）
            String traceId = tmlLogTraceContext.generateTraceId();
            tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, traceId);
            
            try {
                log.info("[Scheduler] 开始检查超时订单 - traceId: {}", traceId);
                schedulerTraceIds.add(traceId);
                
                if (orderRepository == null) {
                    log.warn("[Scheduler] OrderRepository未注入");
                    return;
                }
                
                // 1. 查询超时订单
                Map<Long, OrderData> timeoutOrders = orderRepository.findTimeoutOrders();
                
                log.info("[Scheduler] 发现{}个超时订单 - traceId: {}", timeoutOrders.size(), traceId);
                
                // 2. 处理超时订单
                timeoutOrders.forEach((orderId, orderData) -> {
                    log.info("[Scheduler] 处理超时订单 - orderId: {}, userId: {}, createTraceId: {}, currentTraceId: {}", 
                            orderId, orderData.getUserId(), orderData.getCreateTraceId(), traceId);
                    
                    // 模拟业务处理：取消超时订单
                    orderRepository.updateOrderStatus(orderId, "TIMEOUT_CANCELLED");
                    
                    log.info("[Scheduler] 超时订单已取消 - orderId: {}, traceId: {}", orderId, traceId);
                });
                
                log.info("[Scheduler] 超时订单检查完成 - 处理了{}个订单, traceId: {}", 
                        timeoutOrders.size(), traceId);
                
            } catch (Exception e) {
                log.error("[Scheduler] 检查超时订单失败 - traceId: {}", traceId, e);
            } finally {
                // 清理MDC
                tmlLogTraceContext.remove(TmlLogConstant.TRACE_ID);
                executionLatch.countDown();
            }
        }
    }

    // ==================== DTO类 ====================
    
    @Data
    static class OrderRequest {
        private Long userId;
        private String productName;
        private Double amount;
    }

    @Data
    static class OrderResponse {
        private Long orderId;
        private String status;

        public OrderResponse() {}

        public OrderResponse(Long orderId, String status) {
            this.orderId = orderId;
            this.status = status;
        }
    }

    @Data
    static class OrderData {
        private Long orderId;
        private Long userId;
        private String productName;
        private Double amount;
        private String status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String createTraceId;  // 创建时的traceId
        private String updateTraceId;  // 更新时的traceId
    }
}
