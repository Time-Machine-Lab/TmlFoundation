package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLog;
import io.github.timemachinelab.log.interceptor.TmlLogExecutors;
import io.github.timemachinelab.log.interceptor.TraceIdHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TmlLogExecutors 全场景测试
 * 测试所有多线程环境下的链路追踪功能
 *
 * @Author glser
 * @Date 2026/01/15
 */
@SpringBootTest
@ActiveProfiles("test-trace")
@Slf4j
public class TmlLogExecutorsTest {

    private static final String TEST_TRACE_ID = "test-trace-id-12345";

    @Autowired
    private TraceTestService traceTestService;

    @BeforeEach
    void setUp() {
        // 每个测试前设置 traceId
        TraceIdHolder.set(TEST_TRACE_ID);
        log.info("[主线程] 设置 traceId: {}", TEST_TRACE_ID);
    }

    @AfterEach
    void tearDown() {
        // 每个测试后清理
        TraceIdHolder.clear();
    }

    // ==================== newFixedThreadPool 测试 ====================

    @Test
    @DisplayName("newFixedThreadPool - 子线程应能获取 traceId 并输出到日志")
    void testNewFixedThreadPool() throws Exception {
        ExecutorService executor = TmlLogExecutors.newFixedThreadPool(2);
        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-FixedThreadPool] traceId 验证");
            latch.countDown();
            traceTestService.process();
        });

        executor.submit(() -> {
            traceTestService.process();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get(), "TTL 应传递 traceId");
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get(), "MDC 应同步 traceId");

        executor.shutdown();
    }


    // ==================== newCachedThreadPool 测试 ====================

    @Test
    @DisplayName("newCachedThreadPool - 子线程应能获取 traceId 并输出到日志")
    void testNewCachedThreadPool() throws Exception {
        ExecutorService executor = TmlLogExecutors.newCachedThreadPool();
        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-CachedThreadPool] traceId 验证");
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        executor.shutdown();
    }

    // ==================== newSingleThreadExecutor 测试 ====================

    @Test
    @DisplayName("newSingleThreadExecutor - 子线程应能获取 traceId 并输出到日志")
    void testNewSingleThreadExecutor() throws Exception {
        ExecutorService executor = TmlLogExecutors.newSingleThreadExecutor();
        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-SingleThreadExecutor] traceId 验证");
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        executor.shutdown();
    }

    // ==================== newScheduledThreadPool 测试 ====================

    @Test
    @DisplayName("newScheduledThreadPool - 延迟任务应能获取 traceId")
    void testNewScheduled_ThreadPool_Schedule() throws Exception {
        ScheduledExecutorService executor = TmlLogExecutors.newScheduledThreadPool(2);
        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.schedule(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-ScheduledThreadPool-delay] traceId 验证");
            latch.countDown();
        }, 100, TimeUnit.MILLISECONDS);

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        executor.shutdown();
    }

    @Test
    @DisplayName("newScheduledThreadPool - 周期任务应能获取 traceId")
    void testNewScheduled_ThreadPool_ScheduleAtFixedRate() throws Exception {
        ScheduledExecutorService executor = TmlLogExecutors.newScheduledThreadPool(2);
        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            if (latch.getCount() > 0) {
                childTraceId.set(TraceIdHolder.get());
                childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
                log.info("[子线程-ScheduledThreadPool-fixedRate] traceId 验证");
                latch.countDown();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        future.cancel(true);
        executor.shutdown();
    }


    // ==================== wrap(ExecutorService) 测试 ====================

    @Test
    @DisplayName("wrap(ExecutorService) - 包装已有线程池应能传递 traceId")
    void testWrapExecutorService() throws Exception {
        // 原始线程池
        ExecutorService original = Executors.newFixedThreadPool(2);
        // 包装后的线程池
        ExecutorService executor = TmlLogExecutors.wrap(original);

        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-wrap(ExecutorService)] traceId 验证");
            latch.countDown();

        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        executor.shutdown();
    }

    // ==================== wrap(ScheduledExecutorService) 测试 ====================

    @Test
    @DisplayName("wrap(ScheduledExecutorService) - 包装已有定时线程池应能传递 traceId")
    void testWrapScheduledExecutorService() throws Exception {
        ScheduledExecutorService original = Executors.newScheduledThreadPool(2);
        ScheduledExecutorService executor = TmlLogExecutors.wrap(original);

        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.schedule(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-wrap(ScheduledExecutorService)] traceId 验证");
            latch.countDown();
        }, 50, TimeUnit.MILLISECONDS);

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        executor.shutdown();
    }

    // ==================== wrap(Runnable) 测试 ====================

    @Test
    @DisplayName("wrap(Runnable) - 手动包装任务应能传递 traceId")
    void testWrapRunnable() throws Exception {
        // 使用原始线程池（未包装）
        ExecutorService executor = Executors.newSingleThreadExecutor();

        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // 手动包装 Runnable
        executor.submit(TmlLogExecutors.wrap(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-wrap(Runnable)] traceId 验证");
            latch.countDown();
        }));

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        executor.shutdown();
    }

    // ==================== wrap(Callable) 测试 ====================

    @Test
    @DisplayName("wrap(Callable) - 手动包装 Callable 应能传递 traceId")
    void testWrapCallable() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 手动包装 Callable
        Future<String> future = executor.submit(TmlLogExecutors.wrap(() -> {
            String traceId = TraceIdHolder.get();
            String mdcTraceId = MDC.get(TmlLog.TRACE_ID);
            log.info("[子线程-wrap(Callable)] traceId 验证");
            return traceId + "|" + mdcTraceId;
        }));

        String result = future.get(3, TimeUnit.SECONDS);
        String[] parts = result.split("\\|");
        assertEquals(TEST_TRACE_ID, parts[0], "TTL 应传递 traceId");
        assertEquals(TEST_TRACE_ID, parts[1], "MDC 应同步 traceId");

        executor.shutdown();
    }


    // ==================== getTaskDecorator 测试 ====================

    @Test
    @DisplayName("wrap - Spring ThreadPoolTaskExecutor 应能传递 traceId")
    void testWrap() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("test-async-");
        executor.setTaskDecorator(TmlLogExecutors.wrap());
        executor.initialize();

        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(() -> {
            childTraceId.set(TraceIdHolder.get());
            childMdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[子线程-TaskDecorator] traceId 验证");
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, childTraceId.get());
        assertEquals(TEST_TRACE_ID, childMdcTraceId.get());

        executor.shutdown();
    }

    // ==================== CompletableFuture 测试 ====================

    @Test
    @DisplayName("CompletableFuture - 使用包装后的线程池应能传递 traceId")
    void testCompletableFuture() throws Exception {
        ExecutorService executor = TmlLogExecutors.newFixedThreadPool(4);

        AtomicReference<String> task1TraceId = new AtomicReference<>();
        AtomicReference<String> task2TraceId = new AtomicReference<>();
        AtomicReference<String> task1MdcTraceId = new AtomicReference<>();
        AtomicReference<String> task2MdcTraceId = new AtomicReference<>();

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            task1TraceId.set(TraceIdHolder.get());
            task1MdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[CompletableFuture-task1] traceId 验证");
        }, executor);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            task2TraceId.set(TraceIdHolder.get());
            task2MdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[CompletableFuture-task2] traceId 验证");
        }, executor);

        CompletableFuture.allOf(future1, future2).get(3, TimeUnit.SECONDS);

        assertEquals(TEST_TRACE_ID, task1TraceId.get());
        assertEquals(TEST_TRACE_ID, task2TraceId.get());
        assertEquals(TEST_TRACE_ID, task1MdcTraceId.get());
        assertEquals(TEST_TRACE_ID, task2MdcTraceId.get());

        executor.shutdown();
    }

    @Test
    @DisplayName("CompletableFuture - supplyAsync 应能传递 traceId")
    void testCompletableFutureSupplyAsync() throws Exception {
        ExecutorService executor = TmlLogExecutors.newFixedThreadPool(2);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            String traceId = TraceIdHolder.get();
            String mdcTraceId = MDC.get(TmlLog.TRACE_ID);
            log.info("[CompletableFuture-supplyAsync] traceId 验证");
            return traceId + "|" + mdcTraceId;
        }, executor);

        String result = future.get(3, TimeUnit.SECONDS);
        String[] parts = result.split("\\|");
        assertEquals(TEST_TRACE_ID, parts[0]);
        assertEquals(TEST_TRACE_ID, parts[1]);

        executor.shutdown();
    }


    // ==================== 嵌套异步测试 ====================

    @Test
    @DisplayName("嵌套异步 - 多层嵌套应能传递 traceId")
    void testNestedAsync() throws Exception {
        ExecutorService executor1 = TmlLogExecutors.newFixedThreadPool(2);
        ExecutorService executor2 = TmlLogExecutors.newFixedThreadPool(2);

        AtomicReference<String> level1TraceId = new AtomicReference<>();
        AtomicReference<String> level2TraceId = new AtomicReference<>();
        AtomicReference<String> level1MdcTraceId = new AtomicReference<>();
        AtomicReference<String> level2MdcTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor1.submit(() -> {
            level1TraceId.set(TraceIdHolder.get());
            level1MdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[嵌套异步-第一层] traceId 验证");

            // 嵌套提交到另一个线程池
            try {
                executor2.submit(() -> {
                    level2TraceId.set(TraceIdHolder.get());
                    level2MdcTraceId.set(MDC.get(TmlLog.TRACE_ID));
                    log.info("[嵌套异步-第二层] traceId 验证");
                    latch.countDown();
                }).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(TEST_TRACE_ID, level1TraceId.get());
        assertEquals(TEST_TRACE_ID, level2TraceId.get());
        assertEquals(TEST_TRACE_ID, level1MdcTraceId.get());
        assertEquals(TEST_TRACE_ID, level2MdcTraceId.get());

        executor1.shutdown();
        executor2.shutdown();
    }


    // ==================== invokeAll 测试 ====================

    @Test
    @DisplayName("invokeAll - 批量任务应能传递 traceId")
    void testInvokeAll() throws Exception {
        ExecutorService executor = TmlLogExecutors.newFixedThreadPool(4);

        List<Callable<String>> tasks = Arrays.asList(
            () -> {
                log.info("[invokeAll-task1] traceId 验证");
                return TraceIdHolder.get() + "|" + MDC.get(TmlLog.TRACE_ID);
            },
            () -> {
                log.info("[invokeAll-task2] traceId 验证");
                return TraceIdHolder.get() + "|" + MDC.get(TmlLog.TRACE_ID);
            },
            () -> {
                log.info("[invokeAll-task3] traceId 验证");
                return TraceIdHolder.get() + "|" + MDC.get(TmlLog.TRACE_ID);
            }
        );

        List<Future<String>> futures = executor.invokeAll(tasks);

        for (Future<String> future : futures) {
            String result = future.get();
            String[] parts = result.split("\\|");
            assertEquals(TEST_TRACE_ID, parts[0], "TTL 应传递 traceId");
            assertEquals(TEST_TRACE_ID, parts[1], "MDC 应同步 traceId");
        }

        executor.shutdown();
    }

    // ==================== invokeAny 测试 ====================

    @Test
    @DisplayName("invokeAny - 竞争任务应能传递 traceId")
    void testInvokeAny() throws Exception {
        ExecutorService executor = TmlLogExecutors.newFixedThreadPool(4);

        List<Callable<String>> tasks = Arrays.asList(
            () -> {
                log.info("[invokeAny-task1] traceId 验证");
                return TraceIdHolder.get() + "|" + MDC.get(TmlLog.TRACE_ID);
            },
            () -> {
                log.info("[invokeAny-task2] traceId 验证");
                return TraceIdHolder.get() + "|" + MDC.get(TmlLog.TRACE_ID);
            }
        );

        String result = executor.invokeAny(tasks);
        String[] parts = result.split("\\|");
        assertEquals(TEST_TRACE_ID, parts[0]);
        assertEquals(TEST_TRACE_ID, parts[1]);

        executor.shutdown();
    }


    // ==================== 线程池复用测试 ====================

    @Test
    @DisplayName("线程池复用 - 不同请求的 traceId 应相互隔离")
    void testThreadPoolReuse() throws Exception {
        ExecutorService executor = TmlLogExecutors.newFixedThreadPool(1);

        // 第一个请求
        String traceId1 = "request-1-trace-id";
        TraceIdHolder.set(traceId1);

        AtomicReference<String> result1 = new AtomicReference<>();
        CountDownLatch latch1 = new CountDownLatch(1);

        executor.submit(() -> {
            result1.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[线程池复用-请求1] traceId: {}", result1.get());
            latch1.countDown();
        });

        assertTrue(latch1.await(3, TimeUnit.SECONDS));
        assertEquals(traceId1, result1.get());

        // 第二个请求（不同的 traceId）
        String traceId2 = "request-2-trace-id";
        TraceIdHolder.set(traceId2);

        AtomicReference<String> result2 = new AtomicReference<>();
        CountDownLatch latch2 = new CountDownLatch(1);

        executor.submit(() -> {
            result2.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[线程池复用-请求2] traceId: {}", result2.get());
            latch2.countDown();
        });

        assertTrue(latch2.await(3, TimeUnit.SECONDS));
        assertEquals(traceId2, result2.get());

        // 验证两个请求的 traceId 不同
        assertNotEquals(result1.get(), result2.get());

        executor.shutdown();
    }

    // ==================== MDC 清理测试 ====================

    @Test
    @DisplayName("MDC 清理 - 任务执行后 MDC 应被清理")
    void testMdcCleanup() throws Exception {
        ExecutorService executor = TmlLogExecutors.newFixedThreadPool(1);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> mdcAfterTask = new AtomicReference<>();

        executor.submit(() -> {
            log.info("[MDC清理测试] 任务执行中，MDC: {}", MDC.get(TmlLog.TRACE_ID));
            // 任务执行完后，MDC 应该被清理
        });

        // 等待第一个任务完成
        Thread.sleep(200);

        // 清除主线程的 traceId，模拟新请求
        TraceIdHolder.clear();

        // 提交第二个任务，检查 MDC 是否被污染
        executor.submit(() -> {
            mdcAfterTask.set(MDC.get(TmlLog.TRACE_ID));
            log.info("[MDC清理测试] 第二个任务，MDC: {}", mdcAfterTask.get());
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        // 由于主线程已清除 traceId，子线程的 MDC 应该是 null
        assertNull(mdcAfterTask.get(), "MDC 应该被清理，不应有残留");

        executor.shutdown();
    }
}
