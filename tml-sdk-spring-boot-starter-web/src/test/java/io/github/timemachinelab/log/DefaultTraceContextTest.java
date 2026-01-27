package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLogConstant;
import io.github.timemachinelab.log.context.TmlLogTraceContext;
import io.github.timemachinelab.log.context.DefaultTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceContext核心功能测试
 * 测试TraceContext接口的各种实现和功能
 *
 * @author glser
 * @since 2026/01/18
 */
@SpringBootTest
@ActiveProfiles("log-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class DefaultTraceContextTest {

    private TmlLogTraceContext tmlLogTraceContext;

    @BeforeEach
    public void setUp() {
        tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        tmlLogTraceContext.clear();
    }

    @AfterEach
    public void tearDown() {
        tmlLogTraceContext.clear();
    }

    @Test
    @DisplayName("测试TraceContext单例模式")
    public void testTraceContextSingleton() {
        TmlLogTraceContext instance1 = TmlLogTraceContext.Holder.get();
        TmlLogTraceContext instance2 = TmlLogTraceContext.Holder.get();
        
        assertNotNull(instance1, "TraceContext实例不应为null");
        assertNotNull(instance2, "TraceContext实例不应为null");
        assertSame(instance1, instance2, "应该返回同一个实例");
        
        log.info("✓ TraceContext单例模式测试通过");
    }

    @Test
    @DisplayName("测试默认TraceContext实现类型")
    public void testDefaultTraceContextType() {
        TmlLogTraceContext context = TmlLogTraceContext.Holder.get();
        
        assertTrue(context instanceof DefaultTraceContext,
                "默认实现应该是TmlLogTraceContext");
        
        log.info("✓ 默认TraceContext实现类型测试通过");
    }

    @Test
    @DisplayName("测试set和get基本功能")
    public void testSetAndGet() {
        String key = "testKey";
        String value = "testValue";
        
        tmlLogTraceContext.set(key, value);
        String retrievedValue = tmlLogTraceContext.get(key);
        
        assertEquals(value, retrievedValue, "get应该返回set的值");
        
        log.info("✓ set和get基本功能测试通过");
    }

    @Test
    @DisplayName("测试set覆盖已有值")
    public void testSetOverwrite() {
        String key = "testKey";
        String value1 = "value1";
        String value2 = "value2";
        
        tmlLogTraceContext.set(key, value1);
        assertEquals(value1, tmlLogTraceContext.get(key));
        
        tmlLogTraceContext.set(key, value2);
        assertEquals(value2, tmlLogTraceContext.get(key), "新值应该覆盖旧值");
        
        log.info("✓ set覆盖已有值测试通过");
    }

    @Test
    @DisplayName("测试get不存在的key")
    public void testGetNonExistentKey() {
        String value = tmlLogTraceContext.get("nonExistentKey");
        
        assertNull(value, "不存在的key应该返回null");
        
        log.info("✓ get不存在的key测试通过");
    }

    @Test
    @DisplayName("测试remove功能")
    public void testRemove() {
        String key = "testKey";
        String value = "testValue";
        
        tmlLogTraceContext.set(key, value);
        assertNotNull(tmlLogTraceContext.get(key));
        
        tmlLogTraceContext.remove(key);
        assertNull(tmlLogTraceContext.get(key), "remove后应该返回null");
        
        log.info("✓ remove功能测试通过");
    }

    @Test
    @DisplayName("测试remove不存在的key")
    public void testRemoveNonExistentKey() {
        assertDoesNotThrow(() -> {
            tmlLogTraceContext.remove("nonExistentKey");
        }, "remove不存在的key不应该抛出异常");
        
        log.info("✓ remove不存在的key测试通过");
    }

    @Test
    @DisplayName("测试clear功能")
    public void testClear() {
        tmlLogTraceContext.set("key1", "value1");
        tmlLogTraceContext.set("key2", "value2");
        tmlLogTraceContext.set("key3", "value3");
        
        assertNotNull(tmlLogTraceContext.get("key1"));
        assertNotNull(tmlLogTraceContext.get("key2"));
        assertNotNull(tmlLogTraceContext.get("key3"));
        
        tmlLogTraceContext.clear();
        
        assertNull(tmlLogTraceContext.get("key1"), "clear后所有key都应该被清除");
        assertNull(tmlLogTraceContext.get("key2"), "clear后所有key都应该被清除");
        assertNull(tmlLogTraceContext.get("key3"), "clear后所有key都应该被清除");
        
        log.info("✓ clear功能测试通过");
    }

    @Test
    @DisplayName("测试getAll功能")
    public void testGetAll() {
        tmlLogTraceContext.set("key1", "value1");
        tmlLogTraceContext.set("key2", "value2");
        tmlLogTraceContext.set("key3", "value3");
        
        Map<String, String> allValues = tmlLogTraceContext.getAll();
        
        assertNotNull(allValues, "getAll不应该返回null");
        assertTrue(allValues.size() >= 3, "应该包含所有设置的值");
        assertEquals("value1", allValues.get("key1"));
        assertEquals("value2", allValues.get("key2"));
        assertEquals("value3", allValues.get("key3"));
        
        log.info("✓ getAll功能测试通过");
    }

    @Test
    @DisplayName("测试getAll在空MDC时的行为")
    public void testGetAllWhenEmpty() {
        tmlLogTraceContext.clear();
        
        Map<String, String> allValues = tmlLogTraceContext.getAll();
        
        assertNotNull(allValues, "getAll不应该返回null");
        assertTrue(allValues.isEmpty(), "空MDC应该返回空Map");
        
        log.info("✓ getAll在空MDC时的行为测试通过");
    }

    @Test
    @DisplayName("测试generateTraceId功能")
    public void testGenerateTraceId() {
        String traceId1 = tmlLogTraceContext.generateTraceId();
        String traceId2 = tmlLogTraceContext.generateTraceId();
        
        assertNotNull(traceId1, "生成的traceId不应为null");
        assertNotNull(traceId2, "生成的traceId不应为null");
        assertFalse(traceId1.isEmpty(), "生成的traceId不应为空");
        assertFalse(traceId2.isEmpty(), "生成的traceId不应为空");
        assertNotEquals(traceId1, traceId2, "每次生成的traceId应该不同");
        
        // 验证UUID格式（去掉横线后应该是32位）
        assertEquals(32, traceId1.length(), "UUID去掉横线后应该是32位");
        assertTrue(traceId1.matches("[0-9a-f]{32}"), "应该是32位十六进制字符串");
        
        log.info("✓ generateTraceId功能测试通过");
    }

    @Test
    @DisplayName("测试getTraceIdHeader默认值")
    public void testGetTraceIdHeader() {
        String header = tmlLogTraceContext.getTraceIdHeader();
        
        assertNotNull(header, "traceId header不应为null");
        assertEquals(TmlLogConstant.TRACE_ID_HEADER, header, 
                "默认header应该是" + TmlLogConstant.TRACE_ID_HEADER);
        
        log.info("✓ getTraceIdHeader默认值测试通过");
    }

    @Test
    @DisplayName("测试getTraceIdKey默认值")
    public void testGetTraceIdKey() {
        String key = tmlLogTraceContext.getTraceIdKey();
        
        assertNotNull(key, "traceId key不应为null");
        assertEquals(TmlLogConstant.TRACE_ID, key, 
                "默认key应该是" + TmlLogConstant.TRACE_ID);
        
        log.info("✓ getTraceIdKey默认值测试通过");
    }

    @Test
    @DisplayName("测试TraceContext在多线程环境下的隔离")
    public void testTraceContextThreadIsolation() throws Exception {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Map<Integer, String> threadTraceIds = new ConcurrentHashMap<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    String traceId = "thread-" + threadId + "-trace-id";
                    tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, traceId);
                    
                    // 模拟业务处理
                    Thread.sleep(10);
                    
                    String retrievedTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                    threadTraceIds.put(threadId, retrievedTraceId);
                    
                    log.info("[线程{}] 设置traceId: {}, 获取traceId: {}", 
                            threadId, traceId, retrievedTraceId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        // 验证每个线程的traceId都是独立的
        assertEquals(threadCount, threadTraceIds.size());
        for (int i = 0; i < threadCount; i++) {
            String expectedTraceId = "thread-" + i + "-trace-id";
            assertEquals(expectedTraceId, threadTraceIds.get(i), 
                    "线程" + i + "的traceId应该是独立的");
        }
        
        log.info("✓ TraceContext多线程隔离测试通过");
    }

    @Test
    @DisplayName("测试自定义TraceContext替换默认实现")
    public void testCustomTraceContextReplacement() {
        // 保存原始实现
        TmlLogTraceContext original = TmlLogTraceContext.Holder.get();
        
        try {
            // 创建自定义实现
            CustomTmlLogTraceContext custom = new CustomTmlLogTraceContext();
            TmlLogTraceContext.Holder.set(custom);
            
            // 验证已替换
            TmlLogTraceContext current = TmlLogTraceContext.Holder.get();
            assertSame(custom, current, "应该使用自定义实现");
            
            // 测试自定义实现的功能
            current.set("customKey", "customValue");
            assertEquals("customValue", current.get("customKey"));
            
            // 验证自定义的generateTraceId
            String customTraceId = current.generateTraceId();
            assertTrue(customTraceId.startsWith("CUSTOM-"), 
                    "自定义实现应该生成特定格式的traceId");
            
            log.info("✓ 自定义TraceContext替换测试通过");
        } finally {
            // 恢复原始实现
            TmlLogTraceContext.Holder.set(original);
        }
    }

    @Test
    @DisplayName("测试TraceContext的线程安全性")
    public void testTraceContextThreadSafety() throws Exception {
        int operationCount = 1000;
        CountDownLatch latch = new CountDownLatch(operationCount);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 0; i < operationCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String key = "key-" + (index % 10);
                    String value = "value-" + index;
                    
                    tmlLogTraceContext.set(key, value);
                    String retrieved = tmlLogTraceContext.get(key);
                    
                    // 验证能正确读写
                    assertNotNull(retrieved);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "所有操作应该在30秒内完成");
        
        executor.shutdown();
        
        log.info("✓ TraceContext线程安全性测试通过");
    }

    @Test
    @DisplayName("测试TraceContext性能")
    public void testTraceContextPerformance() {
        int iterations = 10000;
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            String key = "key-" + (i % 100);
            String value = "value-" + i;
            
            tmlLogTraceContext.set(key, value);
            tmlLogTraceContext.get(key);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        log.info("执行{}次set/get操作耗时: {}ms", iterations, duration);
        assertTrue(duration < 5000, "10000次操作应该在5秒内完成");
        
        log.info("✓ TraceContext性能测试通过");
    }

    /**
     * 自定义TraceContext实现，用于测试
     */
    static class CustomTmlLogTraceContext implements TmlLogTraceContext {
        private final Map<String, String> data = new ConcurrentHashMap<>();

        @Override
        public void set(String key, String value) {
            if (key != null && value != null) {
                data.put(key, value);
            }
        }

        @Override
        public String get(String key) {
            return key != null ? data.get(key) : null;
        }

        @Override
        public void remove(String key) {
            if (key != null) {
                data.remove(key);
            }
        }

        @Override
        public void clear() {
            data.clear();
        }

        @Override
        public Map<String, String> getAll() {
            return new ConcurrentHashMap<>(data);
        }

        @Override
        public String generateTraceId() {
            return "CUSTOM-" + UUID.randomUUID().toString();
        }

        @Override
        public String getTraceIdHeader() {
            return "X-Custom-Trace-Id";
        }

        @Override
        public String getTraceIdKey() {
            return "customTraceId";
        }
    }
}
