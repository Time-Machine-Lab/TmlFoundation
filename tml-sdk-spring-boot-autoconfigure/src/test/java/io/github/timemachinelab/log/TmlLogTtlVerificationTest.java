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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TTL功能验证测试
 * 验证TransmittableThreadLocal在不同场景下的表现
 *
 * @author glser
 * @since 2026/01/18
 */
@SpringBootTest
@ActiveProfiles("log-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class TmlLogTtlVerificationTest {

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
    @DisplayName("验证1：普通线程池无包装时traceId传递情况")
    public void testNormalThreadPoolWithoutWrap() throws Exception {
        String mainTraceId = "normal-pool-test";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> childTraceId = new AtomicReference<>();
        
        pool.submit(() -> {
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            childTraceId.set(traceId);
            log.info("[普通线程池] 主线程traceId: {}, 子线程traceId: {}", mainTraceId, traceId);
            latch.countDown();
        });
        
        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();
        
        String result = childTraceId.get();
        if (result == null) {
            log.warn("⚠ 普通线程池无法传递traceId - 需要使用TTL包装或Java Agent");
            log.warn("⚠ 解决方案1: 使用 TmlLogExecutorsTrace.wrap(executor) 包装线程池");
            log.warn("⚠ 解决方案2: 启动时添加 -javaagent:transmittable-thread-local-xxx.jar");
        } else {
            log.info("✓ 普通线程池成功传递traceId（可能使用了TTL Agent）");
            assertEquals(mainTraceId, result);
        }
    }

    @Test
    @DisplayName("验证2：TTL包装线程池后traceId传递")
    public void testWrappedThreadPool() throws Exception {
        String mainTraceId = "wrapped-pool-test";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        ExecutorService normalPool = Executors.newSingleThreadExecutor();
        ExecutorService wrappedPool = TmlLogExecutorsTrace.wrap(normalPool);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> childTraceId = new AtomicReference<>();
        
        wrappedPool.submit(() -> {
            String traceId = tmlLogTraceContext.get(TmlLogConstant.TRACE_ID);
            childTraceId.set(traceId);
            log.info("[包装线程池] 主线程traceId: {}, 子线程traceId: {}", mainTraceId, traceId);
            latch.countDown();
        });
        
        latch.await(5, TimeUnit.SECONDS);
        wrappedPool.shutdown();
        
        String result = childTraceId.get();
        assertNotNull(result, "包装后的线程池应该能传递traceId");
        assertEquals(mainTraceId, result, "子线程应该获取到主线程的traceId");
        
        log.info("✓ TTL包装线程池成功传递traceId");
    }

    @Test
    @DisplayName("验证3：对比包装前后的差异")
    public void testCompareWrappedAndUnwrapped() throws Exception {
        String mainTraceId = "compare-test";
        tmlLogTraceContext.set(TmlLogConstant.TRACE_ID, mainTraceId);
        
        // 测试未包装的线程池
        ExecutorService normalPool = Executors.newSingleThreadExecutor();
        CountDownLatch latch1 = new CountDownLatch(1);
        AtomicReference<String> normalResult = new AtomicReference<>();
        
        normalPool.submit(() -> {
            normalResult.set(tmlLogTraceContext.get(TmlLogConstant.TRACE_ID));
            latch1.countDown();
        });
        latch1.await(5, TimeUnit.SECONDS);
        normalPool.shutdown();
        
        // 测试包装后的线程池
        ExecutorService wrappedPool = TmlLogExecutorsTrace.wrap(Executors.newSingleThreadExecutor());
        CountDownLatch latch2 = new CountDownLatch(1);
        AtomicReference<String> wrappedResult = new AtomicReference<>();
        
        wrappedPool.submit(() -> {
            wrappedResult.set(tmlLogTraceContext.get(TmlLogConstant.TRACE_ID));
            latch2.countDown();
        });
        latch2.await(5, TimeUnit.SECONDS);
        wrappedPool.shutdown();
        
        log.info("========== TTL传递对比 ==========");
        log.info("主线程traceId: {}", mainTraceId);
        log.info("普通线程池获取到的traceId: {}", normalResult.get());
        log.info("包装线程池获取到的traceId: {}", wrappedResult.get());
        log.info("================================");
        
        // 包装后的一定能传递
        assertEquals(mainTraceId, wrappedResult.get(), "包装后的线程池必须能传递traceId");
        
        // 普通的可能传递也可能不传递（取决于是否使用了TTL Agent）
        if (normalResult.get() == null) {
            log.warn("⚠ 建议：在生产环境中使用 TmlLogExecutorsTrace.wrap() 包装所有自定义线程池");
        }
    }

    @Test
    @DisplayName("验证4：Spring @Async场景（需要配置TaskDecorator）")
    public void testSpringAsyncScenario() {
        // 这个测试说明：Spring @Async也需要配置TaskDecorator
        // 配置方式：
        // @Configuration
        // public class AsyncConfig implements AsyncConfigurer {
        //     @Override
        //     public Executor getAsyncExecutor() {
        //         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //         executor.setTaskDecorator(TmlLogExecutorsTrace.wrap());
        //         return executor;
        //     }
        // }
        
        log.info("========== Spring @Async配置说明 ==========");
        log.info("如果使用Spring @Async，需要配置TaskDecorator：");
        log.info("executor.setTaskDecorator(TmlLogExecutorsTrace.wrap());");
        log.info("=========================================");
    }
}
