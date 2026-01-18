package io.github.timemachinelab.log;

import io.github.timemachinelab.log.context.TraceContext;
import org.slf4j.MDC;

/**
 * @author glser
 * @since 2026/1/18
 */

public class TestTraceContext implements TraceContext {
    @Override
    public void set(String key, String value) {

    }

    @Override
    public String get(String key) {
        return "";
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void clear() {

    }

    @Override
    public String generateTraceId() {
        return "testTraceId";
    }

}
