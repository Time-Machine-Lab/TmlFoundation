package io.github.timemachinelab.reflectx.wrapper;

import io.github.timemachinelab.reflectx.exception.ReflectionException;
import io.github.timemachinelab.reflectx.parser.PropertyTokenizer;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * 描述: 包装器基类
 * <p>
 * 职责：提供处理集合（List/Array/Map）元素的通用方法。
 * 解决痛点：统一处理 users[0] 或 data['key'] 这种带下标的访问。
 * </p>
 * @author suifeng
 * 日期: 2026/1/26
 */
public abstract class BaseWrapper implements ObjectWrapper {

    protected static final Object[] NO_ARGUMENTS = new Object[0];

    /**
     * 从集合中取值
     * 支持：Map, List, Array (int[], String[], Object[]...)
     */
    protected  Object getCollectionValue(PropertyTokenizer prop, Object collection) {
        if (collection instanceof Map) {
            // map['key'] -> key
            return ((Map<?, ?>) collection).get(prop.getIndex());
        } else {
            // list[0] -> 0
            int i = Integer.parseInt(prop.getIndex());

            if (collection instanceof List) {
                return ((List<?>) collection).get(i);
            } else if (collection != null && collection.getClass().isArray()) {
                return Array.get(collection, i);
            } else {
                throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
            }
        }
    }

    /**
     * 向集合中设值
     */
    @SuppressWarnings("unchecked")
    protected void setCollectionValue(PropertyTokenizer prop, Object collection, Object value) {
        if (collection instanceof Map) {
            ((Map<String, Object>) collection).put(prop.getIndex(), value);
        } else {
            int i = Integer.parseInt(prop.getIndex());
            if (collection instanceof List) {
                ((List<Object>) collection).set(i, value);
            } else if (collection != null && collection.getClass().isArray()) {
                Array.set(collection, i, value);
            } else {
                throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
            }
        }
    }
}
