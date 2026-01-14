package io.github.timemachinelab.log.interceptor;

import io.github.timemachinelab.log.config.TmlLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;

/**
 * 定时任务 TraceId 切面
 * 为 @Scheduled 注解的方法自动注入 traceId
 */
@Aspect
public class TraceIdScheduledAspect {

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 生成新的 traceId
            String traceId = TmlLog.generateTraceId();
            MDC.put(TmlLog.TRACE_ID, traceId);
            return joinPoint.proceed();
        } finally {
            MDC.remove(TmlLog.TRACE_ID);
        }
    }
}
