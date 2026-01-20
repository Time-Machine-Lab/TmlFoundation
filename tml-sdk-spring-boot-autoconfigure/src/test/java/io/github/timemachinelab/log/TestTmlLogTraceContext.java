package io.github.timemachinelab.log;

import io.github.timemachinelab.log.context.TmlLogTraceContext;

/**
 * @author glser
 * @since 2026/1/18
 */

public class TestTmlLogTraceContext implements TmlLogTraceContext {
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
