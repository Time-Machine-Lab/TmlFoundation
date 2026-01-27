package io.github.timemachinelab.log.interceptor;

import io.github.timemachinelab.log.context.TmlLogTraceContext;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * 定时任务 TraceId 切面
 * 为 @Scheduled 注解的方法自动注入 traceId
 *
 * @author glser
 * @since 2026/01/16
 */
@Aspect
public class TmlLogScheduleTrace {

    @Before("execution(* *(..)) && @annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void beforeMethod() {
        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        tmlLogTraceContext.set(tmlLogTraceContext.getTraceIdKey(), tmlLogTraceContext.generateTraceId());
    }

    /**
     * 任务执行后清除TraceId
     * 确保线程池复用不会导致TraceId污染
     */
    @After("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void afterMethod() {
        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        tmlLogTraceContext.clear();
    }
}
