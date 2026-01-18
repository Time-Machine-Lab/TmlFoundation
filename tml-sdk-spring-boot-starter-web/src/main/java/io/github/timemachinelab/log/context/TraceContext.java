package io.github.timemachinelab.log.context;

import io.github.timemachinelab.log.config.TmlLogConstant;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 链路上下文接口，可自定义设置存放的值、traceId实现方式，可修改trace header名称等
 *
 * @author glser
 * @since 2026/01/16
 */
public interface TraceContext {

    void set(String key, String value);

    String get(String key);

    void remove(String key);

    void clear();

    default Map<String, String> getAll() {
        return Collections.emptyMap();
    }

    default String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    default String getTraceIdHeader() {
        return TmlLogConstant.TRACE_ID_HEADER;
    }

    default String getTraceIdKey() {
        return TmlLogConstant.TRACE_ID;
    }

    /**
     * 全局 TraceContext 持有者
     */
    class Holder {
        private static volatile TraceContext INSTANCE = null;

        private Holder() {}

        public static TraceContext get() {
            if (INSTANCE == null) {
                synchronized (Holder.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new TmlLogTraceContext();
                    }
                }
            }
            return INSTANCE;
        }

        /**
         * 提供 TraceContext 的自定义实现
         */
        public static void set(TraceContext traceContext) {
            if (traceContext == null) {
                throw new IllegalArgumentException("traceContext cannot be null");
            }
            INSTANCE = traceContext;
        }
    }
}
