package io.github.timemachinelab.log.mdc;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.apache.logging.log4j.spi.ThreadContextMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 TTL 的 Log4j2 ThreadContextMap，多线程链路追踪
 * 
 * @author glser
 * @since 2026/1/18
 */
public class TmlLogThreadContextMap implements ThreadContextMap {
    
    private final TransmittableThreadLocal<Map<String, String>> ttlThreadLocal =
            new TransmittableThreadLocal<Map<String, String>>() {
                @Override
                protected Map<String, String> initialValue() {
                    return new HashMap<>();
                }

                @Override
                protected Map<String, String> childValue(Map<String, String> parentValue) {
                    return parentValue != null ? new HashMap<>(parentValue) : new HashMap<>();
                }
            };

    @Override
    public void put(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        ttlThreadLocal.get().put(key, value);
    }

    @Override
    public String get(String key) {
        return ttlThreadLocal.get().get(key);
    }

    @Override
    public void remove(String key) {
        ttlThreadLocal.get().remove(key);
    }

    @Override
    public void clear() {
        ttlThreadLocal.remove();
    }

    @Override
    public boolean containsKey(String key) {
        return ttlThreadLocal.get().containsKey(key);
    }

    @Override
    public Map<String, String> getCopy() {
        Map<String, String> map = ttlThreadLocal.get();
        return map != null ? new HashMap<>(map) : new HashMap<>();
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        Map<String, String> map = ttlThreadLocal.get();
        return map != null && !map.isEmpty() ? new HashMap<>(map) : null;
    }

    @Override
    public boolean isEmpty() {
        Map<String, String> map = ttlThreadLocal.get();
        return map == null || map.isEmpty();
    }
}
