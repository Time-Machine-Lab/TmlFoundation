package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLogConstant;
import io.github.timemachinelab.log.context.TmlLogTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 边界和极端情况测试
 * 测试各种边界条件和异常场景下的traceId处理
 *
 * @author glser
 * @since 2026/01/18
 */
@SpringBootTest
@ActiveProfiles("log-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class TmlLogEdgeCaseTest {

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
    @DisplayName("测试空traceId的处理")
    public void testEmptyTraceId() {
        // 设置空字符串
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, "");
        String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
        
        // 空字符串应该被设置
        assertEquals("", traceId, "空字符串应该被正确设置");
        
        log.info("✓ 空traceId处理测试通过");
    }

    @Test
    @DisplayName("测试null key的处理")
    public void testNullKey() {
        // 设置null key不应该抛出异常
        assertDoesNotThrow(() -> {
            tmlLogTraceContext.set(null, "value");
            tmlLogTraceContext.get(null);
            tmlLogTraceContext.remove(null);
        }, "null key应该被安全处理");
        
        log.info("✓ null key处理测试通过");
    }

    @Test
    @DisplayName("测试null value的处理")
    public void testNullValue() {
        // 设置null value不应该抛出异常
        assertDoesNotThrow(() -> {
            tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, null);
        }, "null value应该被安全处理");
        
        log.info("✓ null value处理测试通过");
    }

    @Test
    @DisplayName("测试超长traceId")
    public void testVeryLongTraceId() {
        // 生成一个超长的traceId（1000个字符）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String longTraceId = sb.toString();
        
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, longTraceId);
        String retrievedTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
        
        assertEquals(longTraceId, retrievedTraceId, "超长traceId应该被正确存储和获取");
        assertEquals(1000, retrievedTraceId.length(), "traceId长度应该保持不变");
        
        log.info("✓ 超长traceId测试通过，长度: {}", retrievedTraceId.length());
    }

    @Test
    @DisplayName("测试特殊字符traceId")
    public void testSpecialCharacterTraceId() {
        String[] specialTraceIds = {
            "trace-id-with-中文",
            "trace@id#with$special%chars",
            "trace\nid\twith\rwhitespace",
            "trace/id\\with/slashes",
            "trace{id}with[brackets]",
            "trace|id&with*symbols"
        };
        
        for (String specialTraceId : specialTraceIds) {
            tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, specialTraceId);
            String retrievedTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            
            assertEquals(specialTraceId, retrievedTraceId, 
                    "特殊字符traceId应该被正确处理: " + specialTraceId);
        }
        
        log.info("✓ 特殊字符traceId测试通过");
    }

    @Test
    @DisplayName("测试重复设置traceId")
    public void testRepeatedSetTraceId() {
        String traceId1 = "first-trace-id";
        String traceId2 = "second-trace-id";
        String traceId3 = "third-trace-id";
        
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, traceId1);
        assertEquals(traceId1, tmlLogTraceContext.get(TmlLogConstant.TRACE_ID));
        
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, traceId2);
        assertEquals(traceId2, tmlLogTraceContext.get(TmlLogConstant.TRACE_ID), "后设置的值应该覆盖前面的值");
        
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, traceId3);
        assertEquals(traceId3, tmlLogTraceContext.get(TmlLogConstant.TRACE_ID), "最后设置的值应该生效");
        
        log.info("✓ 重复设置traceId测试通过");
    }

    @Test
    @DisplayName("测试多个key的MDC操作")
    public void testMultipleKeys() {
        Map<String, String> testData = new HashMap<>();
        testData.put("traceId", "test-trace-id");
        testData.put("userId", "user-123");
        testData.put("requestId", "req-456");
        testData.put("sessionId", "session-789");
        
        // 设置多个key
        testData.forEach(tmlLogTraceContext::set);
        
        // 验证所有key都能正确获取
        testData.forEach((key, expectedValue) -> {
            String actualValue = tmlLogTraceContext.get(key);
            assertEquals(expectedValue, actualValue, "Key " + key + " 的值应该正确");
        });
        
        // 获取所有值
        Map<String, String> allValues = tmlLogTraceContext.getAll();
        assertTrue(allValues.size() >= testData.size(), "应该能获取到所有设置的值");
        
        log.info("✓ 多个key的MDC操作测试通过");
    }

    @Test
    @DisplayName("测试clear操作")
    public void testClearOperation() {
        // 设置多个值
        tmlLogTraceContext.set("key1", "value1");
        tmlLogTraceContext.set("key2", "value2");
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, "test-trace-id");
        
        assertNotNull(tmlLogTraceContext.get("key1"));
        assertNotNull(tmlLogTraceContext.get("key2"));
        assertNotNull(tmlLogTraceContext.get(TmlLogConstant.TRACE_ID));
        
        // 清空
        tmlLogTraceContext.clear();
        
        // 验证所有值都被清空
        assertNull(tmlLogTraceContext.get("key1"), "clear后key1应该为null");
        assertNull(tmlLogTraceContext.get("key2"), "clear后key2应该为null");
        assertNull(tmlLogTraceContext.get(TmlLogConstant.TRACE_ID), "clear后traceId应该为null");
        
        log.info("✓ clear操作测试通过");
    }

    @Test
    @DisplayName("测试remove操作")
    public void testRemoveOperation() {
        tmlLogTraceContext.set("key1", "value1");
        tmlLogTraceContext.set("key2", "value2");
        
        assertNotNull(tmlLogTraceContext.get("key1"));
        assertNotNull(tmlLogTraceContext.get("key2"));
        
        // 移除key1
        tmlLogTraceContext.remove("key1");
        
        assertNull(tmlLogTraceContext.get("key1"), "remove后key1应该为null");
        assertNotNull(tmlLogTraceContext.get("key2"), "key2应该仍然存在");
        
        log.info("✓ remove操作测试通过");
    }

    @Test
    @DisplayName("测试线程池异常情况下的traceId")
    public void testThreadPoolWithException() throws Exception {
        String mainTraceId = "exception-test-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        
        Future<?> future = pool.submit(() -> {
            String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[异常测试] traceId: {}", threadTraceId);
            
            assertEquals(mainTraceId, threadTraceId, "异常前应该能获取到traceId");
            
            latch.countDown();
            
            // 抛出异常
            throw new RuntimeException("测试异常");
        });
        
        latch.await(5, TimeUnit.SECONDS);
        
        // 验证异常被正确抛出
        assertThrows(ExecutionException.class, () -> future.get());
        
        pool.shutdown();
        
        log.info("✓ 线程池异常情况traceId测试通过");
    }

    @Test
    @DisplayName("测试线程中断情况下的traceId")
    public void testThreadInterruptWithTraceId() throws Exception {
        String mainTraceId = "interrupt-test-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch interruptLatch = new CountDownLatch(1);
        
        Future<?> future = pool.submit(() -> {
            String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[中断测试] 开始执行，traceId: {}", threadTraceId);
            assertEquals(mainTraceId, threadTraceId);
            
            startLatch.countDown();
            
            try {
                // 等待被中断
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                String interruptedTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                log.info("[中断测试] 被中断，traceId: {}", interruptedTraceId);
                assertEquals(mainTraceId, interruptedTraceId, "中断后应该仍能获取到traceId");
                interruptLatch.countDown();
                Thread.currentThread().interrupt();
            }
        });
        
        startLatch.await(5, TimeUnit.SECONDS);
        future.cancel(true);
        
        boolean interrupted = interruptLatch.await(5, TimeUnit.SECONDS);
        assertTrue(interrupted, "线程应该被中断");
        
        pool.shutdown();
        
        log.info("✓ 线程中断情况traceId测试通过");
    }

    @Test
    @DisplayName("测试快速创建销毁线程池")
    public void testRapidThreadPoolCreationDestruction() throws Exception {
        String mainTraceId = "rapid-pool-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        for (int i = 0; i < 10; i++) {
            ExecutorService pool = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);
            
            pool.submit(() -> {
                String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                assertEquals(mainTraceId, threadTraceId);
                latch.countDown();
            });
            
            latch.await(5, TimeUnit.SECONDS);
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
        
        log.info("✓ 快速创建销毁线程池测试通过");
    }

    @Test
    @DisplayName("测试零线程线程池")
    public void testZeroThreadPool() {
        // 创建一个核心线程数为0的线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                0, 1, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );
        
        assertNotNull(pool, "线程池不应为null");
        
        pool.shutdown();
        
        log.info("✓ 零线程线程池测试通过");
    }

    @Test
    @DisplayName("测试大量线程的线程池")
    public void testLargeThreadPool() throws Exception {
        String mainTraceId = "large-pool-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newFixedThreadPool(100);
        int taskCount = 1000;
        CountDownLatch latch = new CountDownLatch(taskCount);
        
        for (int i = 0; i < taskCount; i++) {
            pool.submit(() -> {
                String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                assertEquals(mainTraceId, threadTraceId);
                latch.countDown();
            });
        }
        
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "所有任务应该在30秒内完成");
        
        pool.shutdown();
        
        log.info("✓ 大量线程线程池测试通过");
    }

    @Test
    @DisplayName("测试getAll方法返回的Map修改不影响原数据")
    public void testGetAllImmutability() {
        tmlLogTraceContext.set("key1", "value1");
        tmlLogTraceContext.set("key2", "value2");
        
        Map<String, String> allValues = tmlLogTraceContext.getAll();
        
        // 尝试修改返回的Map
        assertDoesNotThrow(() -> {
            allValues.put("key3", "value3");
            allValues.remove("key1");
        }, "修改返回的Map不应该抛出异常");
        
        // 验证原数据未被修改
        assertEquals("value1", tmlLogTraceContext.get("key1"), "原数据key1应该未被修改");
        assertNull(tmlLogTraceContext.get("key3"), "原数据不应该有key3");
        
        log.info("✓ getAll不可变性测试通过");
    }

    @Test
    @DisplayName("测试自定义TraceContext实现")
    public void testCustomTraceContext() {
        // 保存原始实现
        TmlLogTraceContext original = TmlLogTraceContext.Holder.get();
        
        try {
            // 设置自定义实现
            TmlLogTraceContext custom = new CustomTmlLogTraceContext();
            TmlLogTraceContext.Holder.set(custom);
            
            TmlLogTraceContext current = TmlLogTraceContext.Holder.get();
            assertTrue(current instanceof CustomTmlLogTraceContext, "应该使用自定义实现");
            
            // 测试自定义实现
            current.set("test", "value");
            assertEquals("value", current.get("test"));
            
            log.info("✓ 自定义TraceContext实现测试通过");
        } finally {
            // 恢复原始实现
            TmlLogTraceContext.Holder.set(original);
        }
    }

    @Test
    @DisplayName("测试设置null TraceContext抛出异常")
    public void testSetNullTraceContext() {
        assertThrows(IllegalArgumentException.class, () -> {
            TmlLogTraceContext.Holder.set(null);
        }, "设置null TraceContext应该抛出IllegalArgumentException");
        
        log.info("✓ 设置null TraceContext异常测试通过");
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
            return new HashMap<>(data);
        }
    }
}
