package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLog;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 测试用定时任务
 */
@Component
@Slf4j
public class TraceTestScheduledTask {

    public static final CountDownLatch latch = new CountDownLatch(2);
    public static final AtomicReference<String> traceId1 = new AtomicReference<>();
    public static final AtomicReference<String> traceId2 = new AtomicReference<>();

    @Autowired
    private TraceTestService traceTestService;

    @Scheduled(fixedRate = 500)
    public void task() {
        String traceId = MDC.get(TmlLog.TRACE_ID);
        log.info("[定时任务] 开始执行，traceId: {}", traceId);

        // 调用 Service -> Repository 完整链路
        String data = traceTestService.process();
        
        log.info("[定时任务] 执行完成，获取数据: {}", data);

        if (traceId1.get() == null) {
            traceId1.set(traceId);
        } else if (traceId2.get() == null) {
            traceId2.set(traceId);
        }
        latch.countDown();
    }
}
