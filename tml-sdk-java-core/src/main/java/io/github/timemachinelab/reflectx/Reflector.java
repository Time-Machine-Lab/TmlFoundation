package io.github.timemachinelab.reflectx;

import io.github.timemachinelab.reflectx.exception.ReflectionException;
import io.github.timemachinelab.reflectx.invoker.GetFieldInvoker;
import io.github.timemachinelab.reflectx.invoker.Invoker;
import io.github.timemachinelab.reflectx.invoker.MethodInvoker;
import io.github.timemachinelab.reflectx.invoker.SetFieldInvoker;
import io.github.timemachinelab.reflectx.util.PropertyNamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 反射器
 * <p>
 * 1. 缓存了类的元数据信息（Getter/Setter/Field/Constructor）
 * 2. 自动处理字段访问降级策略
 * </p>
 * @author suifeng
 * 日期: 2026/1/26
 */
public class Reflector {

    /** 全局缓存：Class -> Reflector 实例 */
    private static final Map<Class<?>, Reflector> REFLECTOR_CACHE = new ConcurrentHashMap<>();

    private final Class<?> type;

    private final Map<String, Invoker> getMethods = new HashMap<>();
    private final Map<String, Invoker> setMethods = new HashMap<>();
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    private final Map<String, Class<?>> setTypes = new HashMap<>();

    private Constructor<?> defaultConstructor;

    /**
     * 构造函数私有化
     */
    private Reflector(Class<?> clazz) {
        this.type = clazz;
        addDefaultConstructor(clazz);
        addGetMethods(clazz);
        addSetMethods(clazz);
        addFields(clazz);
    }

    /**
     * 获取 Reflector 实例
     */
    public static Reflector forClass(Class<?> clazz) {
        return REFLECTOR_CACHE.computeIfAbsent(clazz, Reflector::new);
    }

    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                this.defaultConstructor = constructor;
            }
        }
    }

    private void addGetMethods(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            // 过滤掉 Object 类的方法和有参方法
            if (method.getParameterCount() > 0 || method.getDeclaringClass() == Object.class) continue;

            String name = method.getName();
            if (PropertyNamer.isGetter(name)) {
                String property = PropertyNamer.methodToProperty(name);
                addGetMethod(property, method);
            }
        }
    }

    private void addSetMethods(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            // 过滤掉非单参数方法
            if (method.getParameterCount() != 1 || method.getDeclaringClass() == Object.class) continue;

            String name = method.getName();
            if (PropertyNamer.isSetter(name)) {
                String property = PropertyNamer.methodToProperty(name);
                addSetMethod(property, method);
            }
        }
    }

    /**
     * 扫描字段（兜底策略）
     * 如果没有 Getter/Setter，则直接通过 Field 访问
     */
    private void addFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 排除 static 和 final 字段
            if (Modifier.isStatic(field.getModifiers())) continue;

            // 如果没有 Getter，添加字段访问
            if (!getMethods.containsKey(field.getName())) {
                getMethods.put(field.getName(), new GetFieldInvoker(field));
                getTypes.put(field.getName(), field.getType());
            }

            // 如果没有 Setter，添加字段访问（排除 final 字段）
            if (!setMethods.containsKey(field.getName()) && !Modifier.isFinal(field.getModifiers())) {
                setMethods.put(field.getName(), new SetFieldInvoker(field));
                setTypes.put(field.getName(), field.getType());
            }
        }

        // 递归处理父类字段
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    private boolean isValidPropertyName(String name) {
        return !name.startsWith("$") && !"serialVersionUID".equals(name) && !"class".equals(name);
    }

    private void addGetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            getMethods.put(name, new MethodInvoker(method));
            getTypes.put(name, method.getReturnType());
        }
    }

    private void addSetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            setMethods.put(name, new MethodInvoker(method));
            setTypes.put(name, method.getParameterTypes()[0]);
        }
    }

    /* ================= 公共 API ================= */

    public Invoker getGetInvoker(String propertyName) {
        Invoker invoker = getMethods.get(propertyName);
        if (invoker == null) {
            throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return invoker;
    }

    public Invoker getSetInvoker(String propertyName) {
        Invoker invoker = setMethods.get(propertyName);
        if (invoker == null) {
            throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return invoker;
    }

    public Class<?> getGetterType(String propertyName) {
        return getTypes.get(propertyName);
    }

    public Class<?> getSetterType(String propertyName) {
        return setTypes.get(propertyName);
    }

    public boolean hasGetter(String propertyName) {
        return getMethods.containsKey(propertyName);
    }

    public boolean hasSetter(String propertyName) {
        return setMethods.containsKey(propertyName);
    }

    public boolean hasDefaultConstructor() {
        return defaultConstructor != null;
    }

    public Constructor<?> getDefaultConstructor() {
        return defaultConstructor;
    }
}
