package io.github.timemachinelab.reflectx.factory;

import io.github.timemachinelab.reflectx.exception.ReflectionException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 描述: 默认对象工厂
 * @author suifeng
 * 日期: 2026/1/26 
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public <T> T create(Class<T> type) {
        return create(type, null, null);
    }

    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        // 1. 解析接口类型。如果是 List.class，这里会转换成 ArrayList.class
        Class<?> classToCreate = resolveInterface(type);

        // 2. 实例化
        return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
    }

    /**
     * 实例化核心逻辑
     */
    private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        try {
            Constructor<T> constructor;
            // Case 1: 无参构造
            if (constructorArgTypes == null || constructorArgs == null) {
                constructor = type.getDeclaredConstructor();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true); // 暴力破解私有构造
                }
                return constructor.newInstance();
            }

            // Case 2: 有参构造
            constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0]));
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(constructorArgs.toArray(new Object[0]));

        } catch (Exception e) {
            String argTypes = constructorArgTypes != null ? constructorArgTypes.toString() : "";
            String argValues = constructorArgs != null ? constructorArgs.toString() : "";
            throw new ReflectionException("Error instantiating " + type + " with invalid types (" + argTypes + ") or values (" + argValues + "). Cause: " + e.getMessage(), e);
        }
    }

    /**
     * 接口解析
     * "new List()" 会报错，必须转换为 "new ArrayList()"
     */
    protected Class<?> resolveInterface(Class<?> type) {
        Class<?> classToCreate;
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            classToCreate = ArrayList.class;
        } else if (type == Map.class) {
            classToCreate = HashMap.class;
        } else if (type == SortedSet.class) {
            classToCreate = TreeSet.class;
        } else if (type == Set.class) {
            classToCreate = HashSet.class;
        } else {
            classToCreate = type;
        }
        return classToCreate;
    }

    @Override
    public <T> boolean isCollection(Class<T> type) {
        return Collection.class.isAssignableFrom(type);
    }
}
