package io.github.timemachinelab.log;

import io.github.timemachinelab.log.context.DefaultTraceContext;
import io.github.timemachinelab.log.context.TmlLogTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 测试自定义 TmlLogTraceContext 和定时任务的 traceId 生成
 * 
 * @author glser
 * @since 2026/1/18
 */
@SpringBootTest
@Slf4j
@ActiveProfiles("log-test")
@Import(TestOverwriteTmlLogTraceContext.TestScheduledTask.class) // 显式导入内部类
public class TestOverwriteTmlLogTraceContext {

    @Autowired(required = false)
    private TestScheduledTask testScheduledTask;

    @Test
    public void testOverwriteTraceContext() {
        // 手动设置 traceId，这样测试日志就会有 traceId
        TmlLogTraceContext.Holder.set(new DefaultTraceContext());

        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();

        // 生成并设置 traceId
        String traceId = tmlLogTraceContext.generateTraceId();
        tmlLogTraceContext.set(tmlLogTraceContext.getTraceIdKey(), traceId);
        
        try {
            log.info("生成的 traceId: {}", traceId);

            tmlLogTraceContext.set("testKey", "测试json能不能自动输出");

            log.info("处理支付订单");
        } finally {
            // 清理 traceId
            tmlLogTraceContext.clear();
        }
    }

    /**
     * 测试定时任务的 traceId 自动生成
     * 注意：@Scheduled 必须在 Spring Bean 中才能生效，不能直接在 @Test 方法上使用
     */
    @Test
    public void testScheduledTaskTraceId() throws InterruptedException {
        if (testScheduledTask == null) {
            log.error("❌ TestScheduledTask Bean 未注册，测试失败！");
            log.error("请检查：");
            log.error("1. TestScheduledTask 是否有 @Component 注解");
            log.error("2. TestApplication 是否有 @EnableScheduling 注解");
            log.error("3. 包扫描路径是否正确");
            return;
        }

        log.info("========== 测试定时任务 traceId ==========");
        log.info("✓ TestScheduledTask Bean 已注册");
        log.info("等待 Spring 自动调度定时任务...");
        log.info("定时任务将在 2 秒后自动执行");
        
        // 等待定时任务自动执行（initialDelay = 2000ms）
        Thread.sleep(4000);
        
        log.info("定时任务测试完成");
        log.info("如果上面的日志有 traceId，说明切面生效了");
    }

    /**
     * 测试用的定时任务 Bean
     * 注意：这个类必须是 Spring Bean，@Scheduled 才能生效
     */
    @Component
    @Slf4j
    public static class TestScheduledTask {
        
        private volatile boolean executed = false;
        
        // 启用 @Scheduled，让 Spring 自动调度（延迟 2 秒执行一次）
        @Scheduled(initialDelay = 2000, fixedDelay = Long.MAX_VALUE)
        public void execute() {
            if (executed) {
                return; // 只执行一次
            }
            executed = true;
            
            log.info("========== 定时任务开始 ==========");
            log.info("定时任务执行中...");

            ExecutorService executor = Executors.newFixedThreadPool(2);
            
            // 子线程继承定时任务的 traceId
            executor.submit(() -> {
                log.info("定时任务的异步子任务1");
            });
            
            executor.submit(() -> {
                log.info("定时任务的异步子任务2");
            });
            
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            log.info("========== 定时任务结束 ==========");
        }
    }
}
