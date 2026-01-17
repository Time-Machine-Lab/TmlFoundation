package io.github.timemachinelab.log.context;

import org.slf4j.MDC;

import java.util.Collections;
import java.util.Map;

/**
 * TmlLog自定义的TraceContext实现
 *
 * @author glser
 * @since 2026/01/16
 */
public class TmlLogTraceContext implements TraceContext {

    @Override
    public void set(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    @Override
    public String get(String key) {
        return key != null ? MDC.get(key) : null;
    }

    @Override
    public void remove(String key) {
        if (key != null) {
            MDC.remove(key);
        }
    }

    @Override
    public void clear() {
        MDC.clear();
    }

    @Override
    public Map<String, String> getAll() {
        Map<String, String> map = MDC.getCopyOfContextMap();
        return map != null ? map : Collections.emptyMap();
    }
}
