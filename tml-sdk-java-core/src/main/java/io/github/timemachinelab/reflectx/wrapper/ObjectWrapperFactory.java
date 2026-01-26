package io.github.timemachinelab.reflectx.wrapper;

import java.util.Map;

/**
 * 描述: 包装器工厂
 * <p>根据对象类型（Bean vs Map）生产对应的 Wrapper
 * @author suifeng
 * 日期: 2026/1/26 
 */
public class ObjectWrapperFactory {

    @SuppressWarnings("unchecked")
    public static ObjectWrapper getWrapperFor(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot wrap null object");
        }
        if (obj instanceof ObjectWrapper) {
            return (ObjectWrapper) obj;
        }
        if (obj instanceof Map) {
            return new MapWrapper((Map<String, Object>) obj);
        }
        return new BeanWrapper(obj);
    }
}
