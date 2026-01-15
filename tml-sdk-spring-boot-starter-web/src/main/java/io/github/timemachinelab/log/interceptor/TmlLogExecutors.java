package io.github.timemachinelab.log.interceptor;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Author glser
 * @Date 2026/01/15
 * @description: TmlLog支持异步任务日志链路追踪的工具类，核心采用阿里ttl实现
 */
public final class TmlLogExecutors {

    private TmlLogExecutors() {}

    /**
     * Executors创建线程池
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new MdcExecutor(TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(nThreads)));
    }

    public static ExecutorService newCachedThreadPool() {
        return new MdcExecutor(TtlExecutors.getTtlExecutorService(Executors.newCachedThreadPool()));
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new MdcExecutor(TtlExecutors.getTtlExecutorService(Executors.newSingleThreadExecutor()));
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new MdcScheduledExecutor(TtlExecutors.getTtlScheduledExecutorService(Executors.newScheduledThreadPool(corePoolSize)));
    }

    /**
     * 原有线程池的装饰，使其能够提供链路追踪功能
     */
    public static ExecutorService wrap(ExecutorService executor) {
        return new MdcExecutor(TtlExecutors.getTtlExecutorService(executor));
    }

    public static ScheduledExecutorService wrap(ScheduledExecutorService executor) {
        return new MdcScheduledExecutor(TtlExecutors.getTtlScheduledExecutorService(executor));
    }

    /**
     * 单次任务的装饰，使其能够提供链路追踪功能
     */
    public static Runnable wrap(Runnable task) {
        return TtlRunnable.get(mdc(task));
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        return TtlCallable.get(mdc(task));
    }

    /**
     * 针对spring task提供装饰，使其能够提供链路追踪功能 例如@Async
     */
    public static TaskDecorator wrap() {
        return task -> TtlRunnable.get(mdc(task));
    }

    /**
     * 重写线程池所有方法，保证MDC传递
     */
    private static class MdcExecutor implements ExecutorService {
        protected final ExecutorService delegate;

        MdcExecutor(ExecutorService delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(Runnable cmd) {
            delegate.execute(mdc(cmd));
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return delegate.submit(mdc(task));
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return delegate.submit(mdc(task), result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return delegate.submit(mdc(task));
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return delegate.invokeAll(mdc(tasks));
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.invokeAll(mdc(tasks), timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return delegate.invokeAny(mdc(tasks));
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.invokeAny(mdc(tasks), timeout, unit);
        }

        @Override public void shutdown() {
            delegate.shutdown();
        }

        @Override public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }
    }

    private static class MdcScheduledExecutor extends MdcExecutor implements ScheduledExecutorService {
        private final ScheduledExecutorService scheduled;

        MdcScheduledExecutor(ScheduledExecutorService delegate) {
            super(delegate);
            this.scheduled = delegate;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable cmd, long delay, TimeUnit unit) {
            return scheduled.schedule(mdc(cmd), delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
            return scheduled.schedule(mdc(task), delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable cmd, long initialDelay, long period, TimeUnit unit) {
            return scheduled.scheduleAtFixedRate(mdc(cmd), initialDelay, period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable cmd, long initialDelay, long delay, TimeUnit unit) {
            return scheduled.scheduleWithFixedDelay(mdc(cmd), initialDelay, delay, unit);
        }
    }

    /**
     * 针对不同任务类型实现MDC传递
     */
    private static Runnable mdc(Runnable task) {
        return () -> {
            try {
                TraceIdHolder.syncToMdc();
                task.run();
            } finally {
                MDC.clear();
            }
        };
    }

    private static <T> Callable<T> mdc(Callable<T> task) {
        return () -> {
            try {
                TraceIdHolder.syncToMdc();
                return task.call();
            } finally {
                MDC.clear();
            }
        };
    }

    private static <T> List<Callable<T>> mdc(Collection<? extends Callable<T>> tasks) {
        return tasks.stream().map(TmlLogExecutors::mdc).collect(Collectors.toList());
    }
}
