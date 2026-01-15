package io.github.timemachinelab.log.interceptor;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.github.timemachinelab.log.config.TmlLog;
import org.slf4j.MDC;

/**
 * @Author glser
 * @Date 2026/01/15
 * @description: TraceId 上下文持有者，基于 TTL 实现跨线程传递
 */
public class TraceIdHolder {

    /**
     * TTL 作为主存储，自动支持线程池传递
     */
    private static final TransmittableThreadLocal<String> TRACE_ID = new TransmittableThreadLocal<>();

    /**
     * 设置 traceId
     * 同时写入 TTL 和 MDC
     *
     * @param traceId traceId
     */
    public static void set(String traceId) {
        TRACE_ID.set(traceId);
        MDC.put(TmlLog.TRACE_ID, traceId);
    }

    /**
     * 获取 traceId
     *
     * @return traceId
     */
    public static String get() {
        return TRACE_ID.get();
    }

    /**
     * 清理 traceId
     */
    public static void clear() {
        TRACE_ID.remove();
        MDC.remove(TmlLog.TRACE_ID);
    }

    /**
     * 从 TTL 同步到 MDC
     * 用于子线程执行时，确保 MDC 也有值（日志输出需要）
     */
    public static void syncToMdc() {
        String traceId = TRACE_ID.get();
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TmlLog.TRACE_ID, traceId);
        }
    }

    /**
     * 清理 MDC（不清理 TTL）
     * 用于子线程执行完毕后清理 MDC，防止线程池复用时数据污染
     */
    public static void clearMdc() {
        MDC.remove(TmlLog.TRACE_ID);
    }
}
