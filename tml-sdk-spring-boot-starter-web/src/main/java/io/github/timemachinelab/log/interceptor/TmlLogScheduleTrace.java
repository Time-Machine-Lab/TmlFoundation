package io.github.timemachinelab.log.interceptor;

import io.github.timemachinelab.log.context.TraceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 定时任务 TraceId 切面
 * 为 @Scheduled 注解的方法自动注入 traceId
 *
 * @author glser
 * @since 2026/01/16
 */
@Aspect
public class TmlLogScheduleTrace {

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceContext traceContext = TraceContext.Holder.get();
        try {
            // 生成新的 traceId
            String traceId = traceContext.generateTraceId();
            traceContext.set(traceContext.getTraceIdKey(), traceId);
            return joinPoint.proceed();
        } finally {
            traceContext.remove(traceContext.getTraceIdKey());
        }
    }
}
