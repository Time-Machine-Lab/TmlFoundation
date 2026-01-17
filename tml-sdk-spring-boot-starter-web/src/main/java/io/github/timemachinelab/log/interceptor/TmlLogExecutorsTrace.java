package io.github.timemachinelab.log.interceptor;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.*;

/**
 * TmlLog 异步任务日志链路追踪工具类，默认基于Ttl实现
 *
 * @author glser
 * @since 2026/01/16
 */
public final class TmlLogExecutorsTrace {

    private TmlLogExecutorsTrace() {}

    /**
     * 包装已有线程池，使其支持链路追踪
     */
    public static ExecutorService wrap(ExecutorService executor) {
        return TtlExecutors.getTtlExecutorService(executor);
    }

    /**
     * 包装已有定时任务线程池，使其支持链路追踪
     */
    public static ScheduledExecutorService wrap(ScheduledExecutorService executor) {
        return TtlExecutors.getTtlScheduledExecutorService(executor);
    }

    /**
     * 包装 Runnable，使其支持链路追踪
     */
    public static Runnable wrap(Runnable task) {
        return TtlRunnable.get(task);
    }

    /**
     * 包装 Callable，使其支持链路追踪
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        return TtlCallable.get(task);
    }

    /**
     * 提供 Spring @Async 的 TaskDecorator
     */
    public static TaskDecorator wrap() {
        return TtlRunnable::get;
    }
}
