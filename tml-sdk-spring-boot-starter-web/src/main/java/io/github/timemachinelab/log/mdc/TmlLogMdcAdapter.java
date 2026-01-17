package io.github.timemachinelab.log.mdc;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.github.timemachinelab.log.config.TmlLogConstant;
import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于Ttl的MDCAdapter实现
 *
 * @author glser
 * @since 2026/1/17
 */
public class TmlLogMdcAdapter implements MDCAdapter, TmlLogMdc {
    private final TransmittableThreadLocal<Map<String, String>> ttlThreadLocal =
            new TransmittableThreadLocal<>() {
                @Override
                protected Map<String, String> initialValue() {
                    return new HashMap<>();
                }

                @Override
                protected Map<String, String> childValue(Map<String, String> parentValue) {
                    return new HashMap<>(parentValue);
                }
            };
    @Override
    public void put(String s, String s1) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
        ttlThreadLocal.get().put(s, s1);
    }

    @Override
    public String get(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return ttlThreadLocal.get().get(s);
    }

    @Override
    public void remove(String s) {
        if (s != null && !s.isEmpty()) {
            ttlThreadLocal.get().remove(s);
        }
    }

    @Override
    public void clear() {
        ttlThreadLocal.remove();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        Map<String, String> map = ttlThreadLocal.get();
        if (map != null && !map.isEmpty()) {
            return new HashMap<>(map);
        }
        return Map.of();
    }

    @Override
    public void setContextMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            ttlThreadLocal.remove();
        } else {
            ttlThreadLocal.set(new HashMap<>(map));
        }
    }

    @Override
    public String name() {
        return TmlLogConstant.MDC_TTL;
    }

    @Override
    public MDCAdapter adapter() {
        return this;
    }
}
