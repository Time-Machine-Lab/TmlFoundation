package io.github.timemachinelab.log.interceptor;

import io.github.timemachinelab.log.config.TmlLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 定时任务 TraceId 切面
 * 为 @Scheduled 注解的方法自动注入 traceId
 *
 * @Author glser
 * @Date 2026/01/15
 * @description: 定时任务 TraceId 切面，基于 TTL 实现跨线程传递
 */
@Aspect
public class TraceIdScheduledAspect {

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 生成新的 traceId
            String traceId = TmlLog.generateTraceId();
            TraceIdHolder.set(traceId);
            return joinPoint.proceed();
        } finally {
            TraceIdHolder.clear();
        }
    }
}
