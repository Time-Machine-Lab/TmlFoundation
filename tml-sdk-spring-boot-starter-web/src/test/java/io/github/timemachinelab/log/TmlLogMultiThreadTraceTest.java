package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLogConstant;
import io.github.timemachinelab.log.context.TmlLogTraceContext;
import io.github.timemachinelab.log.interceptor.TmlLogExecutorsTrace;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多线程场景下的链路追踪测试
 * 测试traceId在各种多线程场景下的传递和隔离
 * 
 * 重要说明：
 * 1. 本项目使用了基于TTL的ThreadContextMap实现（TmlLogThreadContextMap）
 * 2. TTL的TransmittableThreadLocal可以自动在父子线程间传递数据
 * 3. 但TTL的自动传递需要满足以下条件之一：
 *    a) 使用TTL Java Agent: -javaagent:transmittable-thread-local-xxx.jar
 *    b) 显式包装线程池: TtlExecutors.getTtlExecutorService(executor)
 *    c) 显式包装任务: TtlRunnable.get(runnable) 或 TtlCallable.get(callable)
 * 4. 如果配置了TTL Agent，则不需要手动包装；否则需要使用TmlLogExecutorsTrace工具类包装
 *
 * @author glser
 * @since 2026/01/18
 */
@SpringBootTest
@ActiveProfiles("log-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class TmlLogMultiThreadTraceTest {

    private TmlLogTraceContext tmlLogTraceContext;
    private ExecutorService executorService;

    @BeforeEach
    public void setUp() {
        tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        // 清理MDC
        tmlLogTraceContext.clear();
    }

    @AfterEach
    public void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        tmlLogTraceContext.clear();
    }

    @Test
    @DisplayName("测试普通线程池无包装时traceId传递情况")
    public void testNormalThreadPoolTraceId() throws Exception {
        String mainTraceId = "main-thread-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService normalPool = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        
        normalPool.submit(() -> {
            String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[普通线程池] 主线程traceId: {}, 子线程traceId: {}", mainTraceId, threadTraceId);
            
            // 如果使用了TTL的ThreadContextMap，即使不包装线程池，traceId也能传递
            // 但这依赖于是否使用了TTL Java Agent或者手动包装
            if (mainTraceId.equals(threadTraceId)) {
                successCount.incrementAndGet();
                log.info("[普通线程池] ✓ traceId成功传递（可能使用了TTL Agent或ThreadContextMap）");
            } else {
                log.warn("[普通线程池] ✗ traceId未传递（需要包装线程池或使用TTL Agent）");
            }
            latch.countDown();
        });
        
        latch.await(5, TimeUnit.SECONDS);
        normalPool.shutdown();
        
        // 注意：这个测试结果取决于是否配置了TTL ThreadContextMap
        log.info("✓ 普通线程池traceId传递测试完成，成功次数: {}", successCount.get());
    }

    @Test
    @DisplayName("测试普通线程池多任务traceId传递")
    public void testThreadPoolMultiTaskTraceId() throws Exception {
        String mainTraceId = "multi-task-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newFixedThreadPool(2);
        
        CountDownLatch latch = new CountDownLatch(3);
        List<String> childTraceIds = new CopyOnWriteArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            pool.submit(() -> {
                String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                log.info("[线程池多任务] 任务{} - 主线程traceId: {}, 子线程traceId: {}", 
                        taskId, mainTraceId, threadTraceId);
                childTraceIds.add(threadTraceId);
                latch.countDown();
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();
        
        // 验证所有子线程都获取到了正确的traceId
        assertEquals(3, childTraceIds.size());
        for (String childTraceId : childTraceIds) {
            assertEquals(mainTraceId, childTraceId, "子线程应该获取到主线程的traceId");
        }
        
        log.info("✓ 线程池多任务traceId传递测试通过");
    }

    @Test
    @DisplayName("测试Runnable任务traceId传递")
    public void testRunnableTraceId() throws Exception {
        String mainTraceId = "runnable-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        
        Runnable task = () -> {
            String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[Runnable任务] 主线程traceId: {}, 子线程traceId: {}", mainTraceId, threadTraceId);
            
            if (mainTraceId.equals(threadTraceId)) {
                successCount.incrementAndGet();
            }
            latch.countDown();
        };
        
        pool.submit(task);
        
        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();
        
        assertEquals(1, successCount.get(), "Runnable应该能获取到traceId");
        log.info("✓ Runnable traceId传递测试通过");
    }

    @Test
    @DisplayName("测试Callable任务traceId传递")
    public void testCallableTraceId() throws Exception {
        String mainTraceId = "callable-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newSingleThreadExecutor();
        
        Callable<String> task = () -> {
            String threadTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[Callable任务] 主线程traceId: {}, 子线程traceId: {}", mainTraceId, threadTraceId);
            return threadTraceId;
        };
        
        Future<String> future = pool.submit(task);
        
        String result = future.get(5, TimeUnit.SECONDS);
        pool.shutdown();
        
        assertEquals(mainTraceId, result, "Callable应该能获取到traceId");
        log.info("✓ Callable traceId传递测试通过");
    }

    @Test
    @DisplayName("测试多层嵌套线程池traceId传递")
    public void testNestedThreadPoolTraceId() throws Exception {
        String mainTraceId = "nested-pool-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool1 = Executors.newFixedThreadPool(2);
        ExecutorService pool2 = Executors.newFixedThreadPool(2);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        
        pool1.submit(() -> {
            String level1TraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[嵌套线程池-Level1] traceId: {}", level1TraceId);
            
            if (mainTraceId.equals(level1TraceId)) {
                successCount.incrementAndGet();
            }
            
            pool2.submit(() -> {
                String level2TraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                log.info("[嵌套线程池-Level2] traceId: {}", level2TraceId);
                
                if (mainTraceId.equals(level2TraceId)) {
                    successCount.incrementAndGet();
                }
                latch.countDown();
            });
        });
        
        latch.await(5, TimeUnit.SECONDS);
        pool1.shutdown();
        pool2.shutdown();
        
        assertEquals(2, successCount.get(), "嵌套线程池中所有层级都应该获取到traceId");
        log.info("✓ 多层嵌套线程池traceId传递测试通过");
    }

    @Test
    @DisplayName("测试并发场景下不同traceId的隔离")
    public void testConcurrentTraceIdIsolation() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        int taskCount = 10;
        CountDownLatch latch = new CountDownLatch(taskCount);
        List<Boolean> results = new CopyOnWriteArrayList<>();
        
        for (int i = 0; i < taskCount; i++) {
            final String taskTraceId = "concurrent-trace-" + i;
            
            pool.submit(() -> {
                // 每个任务设置自己的traceId
                tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, taskTraceId);
                
                try {
                    // 模拟业务处理
                    Thread.sleep(10);
                    
                    String currentTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                    log.info("[并发隔离测试] 期望traceId: {}, 实际traceId: {}", taskTraceId, currentTraceId);
                    
                    results.add(taskTraceId.equals(currentTraceId));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        pool.shutdown();
        
        // 验证所有任务的traceId都是隔离的
        assertEquals(taskCount, results.size());
        assertTrue(results.stream().allMatch(r -> r), "所有任务的traceId应该都是隔离的");
        
        log.info("✓ 并发场景traceId隔离测试通过");
    }

    @Test
    @DisplayName("测试ScheduledExecutorService的traceId传递")
    public void testScheduledExecutorServiceTraceId() throws Exception {
        String mainTraceId = "scheduled-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
        
        CountDownLatch latch = new CountDownLatch(3);
        List<String> traceIds = new CopyOnWriteArrayList<>();
        
        // 测试schedule
        scheduled.schedule(() -> {
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[ScheduledExecutor-schedule] traceId: {}", traceId);
            traceIds.add(traceId);
            latch.countDown();
        }, 100, TimeUnit.MILLISECONDS);
        
        // 测试scheduleAtFixedRate
        ScheduledFuture<?> fixedRateFuture = scheduled.scheduleAtFixedRate(() -> {
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            log.info("[ScheduledExecutor-fixedRate] traceId: {}", traceId);
            traceIds.add(traceId);
            latch.countDown();
        }, 100, 200, TimeUnit.MILLISECONDS);
        
        latch.await(5, TimeUnit.SECONDS);
        fixedRateFuture.cancel(true);
        scheduled.shutdown();
        
        // 验证所有定时任务都获取到了traceId
        assertTrue(traceIds.size() >= 2, "至少应该执行2次定时任务");
        for (String traceId : traceIds) {
            assertEquals(mainTraceId, traceId, "定时任务应该获取到主线程的traceId");
        }
        
        log.info("✓ ScheduledExecutorService traceId传递测试通过");
    }

    @Test
    @DisplayName("测试CompletableFuture的traceId传递")
    public void testCompletableFutureTraceId() throws Exception {
        String mainTraceId = "completable-future-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool1 = TmlLogExecutorsTrace.wrap(Executors.newFixedThreadPool(3));
        ExecutorService pool = Executors.newFixedThreadPool(3);
        List<String> traceIds = new CopyOnWriteArrayList<>();
        
        CompletableFuture<Void> future = CompletableFuture
                .supplyAsync(() -> {
                    String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                    log.info("[CompletableFuture-supply] traceId: {}", traceId);
                    traceIds.add(traceId);
                    return "result";
                }, pool)
                .thenApplyAsync(result -> {
                    String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                    log.info("[CompletableFuture-thenApply] traceId: {}", traceId);
                    traceIds.add(traceId);
                    return result + "-processed";
                }, pool)
                .thenAcceptAsync(result -> {
                    String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                    log.info("[CompletableFuture-thenAccept] result: {}, traceId: {}", result, traceId);
                    traceIds.add(traceId);
                }, pool);
        
        future.get(5, TimeUnit.SECONDS);
        pool.shutdown();
        
        // 验证CompletableFuture链中所有阶段都获取到了traceId
        assertEquals(3, traceIds.size());
        for (String traceId : traceIds) {
            assertEquals(mainTraceId, traceId, "CompletableFuture各阶段应该获取到主线程的traceId");
        }
        
        log.info("✓ CompletableFuture traceId传递测试通过");
    }

    @Test
    @DisplayName("测试线程池关闭后的清理")
    public void testThreadPoolShutdownCleanup() throws Exception {
        String mainTraceId = "shutdown-test-trace-id";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                log.info("[线程池关闭测试] traceId: {}", traceId);
                assertEquals(mainTraceId, traceId);
                latch.countDown();
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        
        // 关闭线程池
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS), "线程池应该正常关闭");
        
        log.info("✓ 线程池关闭清理测试通过");
    }

    @Test
    @DisplayName("测试高并发场景下的traceId稳定性")
    public void testHighConcurrencyTraceIdStability() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(20);
        int taskCount = 100;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < taskCount; i++) {
            final String taskTraceId = "high-concurrency-" + i;
            
            pool.submit(() -> {
                tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, taskTraceId);
                
                try {
                    // 模拟复杂业务处理
                    Thread.sleep((long) (Math.random() * 10));
                    
                    String currentTraceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
                    if (taskTraceId.equals(currentTraceId)) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        pool.shutdown();
        
        log.info("高并发测试完成，成功: {}/{}", successCount.get(), taskCount);
        assertEquals(taskCount, successCount.get(), "高并发场景下所有任务的traceId应该都正确");
        
        log.info("✓ 高并发场景traceId稳定性测试通过");
    }
}
