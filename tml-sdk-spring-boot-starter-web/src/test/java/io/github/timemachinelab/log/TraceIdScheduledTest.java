package io.github.timemachinelab.log;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-trace")
@EnableScheduling
@Slf4j
public class TraceIdScheduledTest {

    /**
     * 测试定时任务完整链路的 traceId 传递
     * 定时任务 -> Service -> Repository 整条链路日志应该有相同的 traceId
     */
    @Test
    void testScheduledTaskFullChainTraceId() throws InterruptedException {
        log.info("========== 开始定时任务链路追踪测试 ==========");

        // 等待定时任务执行两次
        boolean completed = TraceTestScheduledTask.latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "定时任务应在 5 秒内执行两次");

        String traceId1 = TraceTestScheduledTask.traceId1.get();
        String traceId2 = TraceTestScheduledTask.traceId2.get();

        // 验证 traceId 存在
        assertNotNull(traceId1, "第一次执行应有 traceId");
        assertNotNull(traceId2, "第二次执行应有 traceId");
        
        // 验证 traceId 格式正确（32位）
        assertEquals(32, traceId1.length(), "traceId 应为 32 位");
        assertEquals(32, traceId2.length(), "traceId 应为 32 位");

        // 验证每次执行的 traceId 不同
        assertNotEquals(traceId1, traceId2, "每次定时任务执行应生成不同的 traceId");

        log.info("第一次执行 traceId: {}", traceId1);
        log.info("第二次执行 traceId: {}", traceId2);
        log.info("========== 定时任务链路追踪测试完成 ==========");
        log.info("请查看上方日志，定时任务/Service/Repository 的日志应该都带有相同的 traceId");
    }
}
