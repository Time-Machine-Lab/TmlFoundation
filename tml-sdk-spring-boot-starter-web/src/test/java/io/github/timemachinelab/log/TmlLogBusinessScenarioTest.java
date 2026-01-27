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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 模拟真实业务场景的Web链路追踪测试
 * 完整的MVC三层架构：Controller -> Service -> Repository
 * 模拟电商订单业务流程
 *
 * @author glser
 * @since 2026/01/18
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("log-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@org.springframework.context.annotation.Import({
    TmlLogBusinessScenarioTest.OrderController.class,
    TmlLogBusinessScenarioTest.OrderService.class,
    TmlLogBusinessScenarioTest.OrderRepository.class,
    TmlLogBusinessScenarioTest.InventoryService.class,
    TmlLogBusinessScenarioTest.PaymentService.class,
    TmlLogBusinessScenarioTest.NotificationService.class
})
@Slf4j
public class TmlLogBusinessScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("场景1：用户下单完整流程")
    public void testCreateOrderScenario() throws Exception {
        String traceId = "order-create-trace-" + System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header(TmlLogConstant.TRACE_ID_HEADER, traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1001,\"productId\":2001,\"quantity\":2,\"amount\":299.99}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
        assertEquals(traceId, responseTraceId);
        
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("orderId"), "响应应包含订单ID");
        
        log.info("✓ 用户下单完整流程测试通过，traceId: {}", traceId);
    }

    @Test
    @DisplayName("场景2：查询订单详情")
    public void testQueryOrderScenario() throws Exception {
        String traceId = "order-query-trace-" + System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/orders/12345")
                        .header(TmlLogConstant.TRACE_ID_HEADER, traceId))
                .andExpect(status().isOk())
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
        assertEquals(traceId, responseTraceId);
        
        log.info("✓ 查询订单详情测试通过，traceId: {}", traceId);
    }

    @Test
    @DisplayName("场景3：订单列表分页查询")
    public void testQueryOrderListScenario() throws Exception {
        String traceId = "order-list-trace-" + System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/orders")
                        .header(TmlLogConstant.TRACE_ID_HEADER, traceId)
                        .param("userId", "1001")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
        assertEquals(traceId, responseTraceId);
        
        log.info("✓ 订单列表分页查询测试通过，traceId: {}", traceId);
    }

    @Test
    @DisplayName("场景4：取消订单")
    public void testCancelOrderScenario() throws Exception {
        String traceId = "order-cancel-trace-" + System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(put("/api/orders/12345/cancel")
                        .header(TmlLogConstant.TRACE_ID_HEADER, traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"用户主动取消\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
        assertEquals(traceId, responseTraceId);
        
        log.info("✓ 取消订单测试通过，traceId: {}", traceId);
    }

    @Test
    @DisplayName("场景5：订单支付流程（含异步通知）")
    public void testPayOrderScenario() throws Exception {
        String traceId = "order-pay-trace-" + System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(post("/api/orders/12345/pay")
                        .header(TmlLogConstant.TRACE_ID_HEADER, traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payMethod\":\"ALIPAY\",\"amount\":299.99}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
        assertEquals(traceId, responseTraceId);
        
        // 等待异步通知完成
        Thread.sleep(500);
        
        log.info("✓ 订单支付流程测试通过，traceId: {}", traceId);
    }

    @Test
    @DisplayName("场景6：复杂业务流程-下单并扣减库存")
    public void testComplexOrderWithInventoryScenario() throws Exception {
        String traceId = "complex-order-trace-" + System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(post("/api/orders/complex")
                        .header(TmlLogConstant.TRACE_ID_HEADER, traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1001,\"items\":[{\"productId\":2001,\"quantity\":2},{\"productId\":2002,\"quantity\":1}]}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseTraceId = result.getResponse().getHeader(TmlLogConstant.TRACE_ID_HEADER);
        assertEquals(traceId, responseTraceId);
        
        log.info("✓ 复杂业务流程测试通过，traceId: {}", traceId);
    }

    // ==================== Controller层 ====================
    
    @RestController
    @RequestMapping("/api/orders")
    @Slf4j
    static class OrderController {

        @Autowired(required = false)
        private OrderService orderService;

        @PostMapping
        public OrderResponse createOrder(@RequestBody OrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Controller] 接收创建订单请求 - userId: {}, productId: {}, quantity: {}, traceId: {}", 
                    request.getUserId(), request.getProductId(), request.getQuantity(), traceId);
            
            if (orderService == null) {
                log.warn("[Controller] OrderService未注入");
                return new OrderResponse(99999L, "SUCCESS");
            }
            
            OrderResponse response = orderService.createOrder(request);
            
            log.info("[Controller] 订单创建成功 - orderId: {}, traceId: {}", response.getOrderId(), traceId);
            return response;
        }

        @GetMapping("/{orderId}")
        public OrderDetailResponse getOrderDetail(@PathVariable Long orderId) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Controller] 查询订单详情 - orderId: {}, traceId: {}", orderId, traceId);
            
            if (orderService == null) {
                return new OrderDetailResponse();
            }
            
            OrderDetailResponse response = orderService.getOrderDetail(orderId);
            
            log.info("[Controller] 订单详情查询完成 - orderId: {}, status: {}, traceId: {}", 
                    orderId, response.getStatus(), traceId);
            return response;
        }

        @GetMapping
        public OrderListResponse getOrderList(@RequestParam Long userId,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Controller] 查询订单列表 - userId: {}, page: {}, size: {}, traceId: {}", 
                    userId, page, size, traceId);
            
            if (orderService == null) {
                return new OrderListResponse();
            }
            
            OrderListResponse response = orderService.getOrderList(userId, page, size);
            
            log.info("[Controller] 订单列表查询完成 - userId: {}, total: {}, traceId: {}", 
                    userId, response.getTotal(), traceId);
            return response;
        }

        @PutMapping("/{orderId}/cancel")
        public OrderResponse cancelOrder(@PathVariable Long orderId, @RequestBody CancelRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Controller] 取消订单 - orderId: {}, reason: {}, traceId: {}", 
                    orderId, request.getReason(), traceId);
            
            if (orderService == null) {
                return new OrderResponse(orderId, "CANCELLED");
            }
            
            OrderResponse response = orderService.cancelOrder(orderId, request.getReason());
            
            log.info("[Controller] 订单取消成功 - orderId: {}, traceId: {}", orderId, traceId);
            return response;
        }

        @PostMapping("/{orderId}/pay")
        public PaymentResponse payOrder(@PathVariable Long orderId, @RequestBody PaymentRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Controller] 订单支付 - orderId: {}, payMethod: {}, amount: {}, traceId: {}", 
                    orderId, request.getPayMethod(), request.getAmount(), traceId);
            
            if (orderService == null) {
                return new PaymentResponse("PAY-" + orderId, "SUCCESS");
            }
            
            PaymentResponse response = orderService.payOrder(orderId, request);
            
            log.info("[Controller] 订单支付完成 - orderId: {}, paymentId: {}, traceId: {}", 
                    orderId, response.getPaymentId(), traceId);
            return response;
        }

        @PostMapping("/complex")
        public OrderResponse createComplexOrder(@RequestBody ComplexOrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Controller] 创建复杂订单 - userId: {}, itemCount: {}, traceId: {}", 
                    request.getUserId(), request.getItems().size(), traceId);
            
            if (orderService == null) {
                return new OrderResponse(88888L, "SUCCESS");
            }
            
            OrderResponse response = orderService.createComplexOrder(request);
            
            log.info("[Controller] 复杂订单创建成功 - orderId: {}, traceId: {}", response.getOrderId(), traceId);
            return response;
        }
    }

    // ==================== Service层 ====================
    
    @org.springframework.stereotype.Service
    @Slf4j
    static class OrderService {

        @Autowired(required = false)
        private OrderRepository orderRepository;

        @Autowired(required = false)
        private InventoryService inventoryService;

        @Autowired(required = false)
        private PaymentService paymentService;

        @Autowired(required = false)
        private NotificationService notificationService;

        public OrderResponse createOrder(OrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Service] 开始处理订单创建 - userId: {}, traceId: {}", request.getUserId(), traceId);
            
            // 1. 验证用户信息
            log.info("[Service] 验证用户信息 - userId: {}, traceId: {}", request.getUserId(), traceId);
            
            // 2. 验证商品信息
            log.info("[Service] 验证商品信息 - productId: {}, traceId: {}", request.getProductId(), traceId);
            
            // 3. 检查库存
            if (inventoryService != null) {
                inventoryService.checkInventory(request.getProductId(), request.getQuantity());
            }
            
            // 4. 创建订单
            Long orderId = orderRepository != null ? 
                    orderRepository.saveOrder(request) : 
                    System.currentTimeMillis();
            
            log.info("[Service] 订单创建完成 - orderId: {}, traceId: {}", orderId, traceId);
            
            return new OrderResponse(orderId, "SUCCESS");
        }

        public OrderDetailResponse getOrderDetail(Long orderId) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Service] 查询订单详情 - orderId: {}, traceId: {}", orderId, traceId);
            
            // 从数据库查询订单
            OrderDetailResponse response = orderRepository != null ?
                    orderRepository.findOrderById(orderId) :
                    createMockOrderDetail(orderId);
            
            log.info("[Service] 订单详情查询完成 - orderId: {}, status: {}, traceId: {}", 
                    orderId, response.getStatus(), traceId);
            
            return response;
        }

        public OrderListResponse getOrderList(Long userId, Integer page, Integer size) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Service] 查询订单列表 - userId: {}, page: {}, size: {}, traceId: {}", 
                    userId, page, size, traceId);
            
            // 从数据库分页查询
            OrderListResponse response = orderRepository != null ?
                    orderRepository.findOrdersByUserId(userId, page, size) :
                    createMockOrderList(userId);
            
            log.info("[Service] 订单列表查询完成 - userId: {}, total: {}, traceId: {}", 
                    userId, response.getTotal(), traceId);
            
            return response;
        }

        public OrderResponse cancelOrder(Long orderId, String reason) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Service] 开始取消订单 - orderId: {}, reason: {}, traceId: {}", orderId, reason, traceId);
            
            // 1. 查询订单状态
            log.info("[Service] 查询订单状态 - orderId: {}, traceId: {}", orderId, traceId);
            
            // 2. 验证是否可取消
            log.info("[Service] 验证订单是否可取消 - orderId: {}, traceId: {}", orderId, traceId);
            
            // 3. 更新订单状态
            if (orderRepository != null) {
                orderRepository.updateOrderStatus(orderId, "CANCELLED");
            }
            
            // 4. 恢复库存
            if (inventoryService != null) {
                inventoryService.restoreInventory(orderId);
            }
            
            log.info("[Service] 订单取消完成 - orderId: {}, traceId: {}", orderId, traceId);
            
            return new OrderResponse(orderId, "CANCELLED");
        }

        public PaymentResponse payOrder(Long orderId, PaymentRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Service] 开始订单支付 - orderId: {}, payMethod: {}, traceId: {}", 
                    orderId, request.getPayMethod(), traceId);
            
            // 1. 验证订单状态
            log.info("[Service] 验证订单状态 - orderId: {}, traceId: {}", orderId, traceId);
            
            // 2. 调用支付服务
            String paymentId = paymentService != null ?
                    paymentService.processPayment(orderId, request) :
                    "PAY-" + orderId;
            
            // 3. 更新订单状态
            if (orderRepository != null) {
                orderRepository.updateOrderStatus(orderId, "PAID");
            }
            
            // 4. 异步发送通知
            if (notificationService != null) {
                notificationService.sendPaymentNotification(orderId, paymentId);
            }
            
            log.info("[Service] 订单支付完成 - orderId: {}, paymentId: {}, traceId: {}", 
                    orderId, paymentId, traceId);
            
            return new PaymentResponse(paymentId, "SUCCESS");
        }

        public OrderResponse createComplexOrder(ComplexOrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Service] 开始创建复杂订单 - userId: {}, itemCount: {}, traceId: {}", 
                    request.getUserId(), request.getItems().size(), traceId);
            
            // 1. 验证所有商品
            for (OrderItem item : request.getItems()) {
                log.info("[Service] 验证商品 - productId: {}, quantity: {}, traceId: {}", 
                        item.getProductId(), item.getQuantity(), traceId);
            }
            
            // 2. 批量检查库存
            if (inventoryService != null) {
                inventoryService.batchCheckInventory(request.getItems());
            }
            
            // 3. 计算总价
            BigDecimal totalAmount = request.getItems().stream()
                    .map(item -> BigDecimal.valueOf(item.getQuantity() * 99.99))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("[Service] 计算订单总价 - totalAmount: {}, traceId: {}", totalAmount, traceId);
            
            // 4. 创建订单
            Long orderId = orderRepository != null ?
                    orderRepository.saveComplexOrder(request) :
                    System.currentTimeMillis();
            
            // 5. 批量扣减库存
            if (inventoryService != null) {
                inventoryService.batchDeductInventory(orderId, request.getItems());
            }
            
            log.info("[Service] 复杂订单创建完成 - orderId: {}, totalAmount: {}, traceId: {}", 
                    orderId, totalAmount, traceId);
            
            return new OrderResponse(orderId, "SUCCESS");
        }

        private OrderDetailResponse createMockOrderDetail(Long orderId) {
            OrderDetailResponse response = new OrderDetailResponse();
            response.setOrderId(orderId);
            response.setStatus("PENDING");
            response.setAmount(BigDecimal.valueOf(299.99));
            return response;
        }

        private OrderListResponse createMockOrderList(Long userId) {
            OrderListResponse response = new OrderListResponse();
            response.setTotal(10);
            response.setOrders(new ArrayList<>());
            return response;
        }
    }

    // ==================== Repository层 ====================
    
    @org.springframework.stereotype.Repository
    @Slf4j
    static class OrderRepository {

        public Long saveOrder(OrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Repository] 保存订单到数据库 - userId: {}, productId: {}, traceId: {}", 
                    request.getUserId(), request.getProductId(), traceId);
            
            // 模拟数据库插入操作
            Long orderId = System.currentTimeMillis();
            
            log.info("[Repository] 订单保存成功 - orderId: {}, traceId: {}", orderId, traceId);
            
            return orderId;
        }

        public OrderDetailResponse findOrderById(Long orderId) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Repository] 从数据库查询订单 - orderId: {}, traceId: {}", orderId, traceId);
            
            // 模拟数据库查询
            OrderDetailResponse response = new OrderDetailResponse();
            response.setOrderId(orderId);
            response.setUserId(1001L);
            response.setProductId(2001L);
            response.setQuantity(2);
            response.setAmount(BigDecimal.valueOf(299.99));
            response.setStatus("PENDING");
            response.setCreateTime(LocalDateTime.now());
            
            log.info("[Repository] 订单查询成功 - orderId: {}, status: {}, traceId: {}", 
                    orderId, response.getStatus(), traceId);
            
            return response;
        }

        public OrderListResponse findOrdersByUserId(Long userId, Integer page, Integer size) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Repository] 分页查询用户订单 - userId: {}, page: {}, size: {}, traceId: {}", 
                    userId, page, size, traceId);
            
            // 模拟数据库分页查询
            OrderListResponse response = new OrderListResponse();
            response.setTotal(25);
            response.setPage(page);
            response.setSize(size);
            response.setOrders(new ArrayList<>());
            
            log.info("[Repository] 订单列表查询成功 - userId: {}, total: {}, traceId: {}", 
                    userId, response.getTotal(), traceId);
            
            return response;
        }

        public void updateOrderStatus(Long orderId, String status) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Repository] 更新订单状态 - orderId: {}, status: {}, traceId: {}", 
                    orderId, status, traceId);
            
            // 模拟数据库更新操作
            
            log.info("[Repository] 订单状态更新成功 - orderId: {}, newStatus: {}, traceId: {}", 
                    orderId, status, traceId);
        }

        public Long saveComplexOrder(ComplexOrderRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[Repository] 保存复杂订单 - userId: {}, itemCount: {}, traceId: {}", 
                    request.getUserId(), request.getItems().size(), traceId);
            
            // 模拟数据库事务操作
            Long orderId = System.currentTimeMillis();
            
            log.info("[Repository] 复杂订单保存成功 - orderId: {}, traceId: {}", orderId, traceId);
            
            return orderId;
        }
    }

    // ==================== 其他Service ====================
    
    @org.springframework.stereotype.Service
    @Slf4j
    static class InventoryService {

        public void checkInventory(Long productId, Integer quantity) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[InventoryService] 检查库存 - productId: {}, quantity: {}, traceId: {}", 
                    productId, quantity, traceId);
            
            // 模拟库存检查
            log.info("[InventoryService] 库存充足 - productId: {}, availableStock: 100, traceId: {}", 
                    productId, traceId);
        }

        public void restoreInventory(Long orderId) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[InventoryService] 恢复库存 - orderId: {}, traceId: {}", orderId, traceId);
            
            // 模拟库存恢复
            log.info("[InventoryService] 库存恢复成功 - orderId: {}, traceId: {}", orderId, traceId);
        }

        public void batchCheckInventory(List<OrderItem> items) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[InventoryService] 批量检查库存 - itemCount: {}, traceId: {}", items.size(), traceId);
            
            for (OrderItem item : items) {
                log.info("[InventoryService] 检查商品库存 - productId: {}, quantity: {}, traceId: {}", 
                        item.getProductId(), item.getQuantity(), traceId);
            }
            
            log.info("[InventoryService] 批量库存检查完成 - traceId: {}", traceId);
        }

        public void batchDeductInventory(Long orderId, List<OrderItem> items) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[InventoryService] 批量扣减库存 - orderId: {}, itemCount: {}, traceId: {}", 
                    orderId, items.size(), traceId);
            
            for (OrderItem item : items) {
                log.info("[InventoryService] 扣减商品库存 - productId: {}, quantity: {}, traceId: {}", 
                        item.getProductId(), item.getQuantity(), traceId);
            }
            
            log.info("[InventoryService] 批量库存扣减完成 - orderId: {}, traceId: {}", orderId, traceId);
        }
    }

    @org.springframework.stereotype.Service
    @Slf4j
    static class PaymentService {

        public String processPayment(Long orderId, PaymentRequest request) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            log.info("[PaymentService] 处理支付 - orderId: {}, payMethod: {}, amount: {}, traceId: {}", 
                    orderId, request.getPayMethod(), request.getAmount(), traceId);
            
            // 模拟调用第三方支付接口
            String paymentId = "PAY-" + System.currentTimeMillis();
            
            log.info("[PaymentService] 支付处理成功 - orderId: {}, paymentId: {}, traceId: {}", 
                    orderId, paymentId, traceId);
            
            return paymentId;
        }
    }

    @org.springframework.stereotype.Service
    @Slf4j
    static class NotificationService {

        public void sendPaymentNotification(Long orderId, String paymentId) {
            TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            // 异步发送通知
            CompletableFuture.runAsync(() -> {
                String asyncTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                log.info("[NotificationService] 异步发送支付通知 - orderId: {}, paymentId: {}, traceId: {}", 
                        orderId, paymentId, asyncTraceId);
                
                // 模拟发送短信
                log.info("[NotificationService] 发送短信通知 - orderId: {}, traceId: {}", orderId, asyncTraceId);
                
                // 模拟发送邮件
                log.info("[NotificationService] 发送邮件通知 - orderId: {}, traceId: {}", orderId, asyncTraceId);
                
                log.info("[NotificationService] 通知发送完成 - orderId: {}, traceId: {}", orderId, asyncTraceId);
            });
        }
    }

    // ==================== DTO类 ====================
    
    @Data
    static class OrderRequest {
        private Long userId;
        private Long productId;
        private Integer quantity;
        private BigDecimal amount;
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
    static class OrderDetailResponse {
        private Long orderId;
        private Long userId;
        private Long productId;
        private Integer quantity;
        private BigDecimal amount;
        private String status;
        private LocalDateTime createTime;
    }

    @Data
    static class OrderListResponse {
        private Integer total;
        private Integer page;
        private Integer size;
        private List<OrderDetailResponse> orders;
    }

    @Data
    static class CancelRequest {
        private String reason;
    }

    @Data
    static class PaymentRequest {
        private String payMethod;
        private BigDecimal amount;
    }

    @Data
    static class PaymentResponse {
        private String paymentId;
        private String status;

        public PaymentResponse() {}

        public PaymentResponse(String paymentId, String status) {
            this.paymentId = paymentId;
            this.status = status;
        }
    }

    @Data
    static class ComplexOrderRequest {
        private Long userId;
        private List<OrderItem> items;
    }

    @Data
    static class OrderItem {
        private Long productId;
        private Integer quantity;
    }
}
