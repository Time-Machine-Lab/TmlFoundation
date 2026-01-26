package io.github.timemachinelab.reflectx.invoker;

import io.github.timemachinelab.reflectx.exception.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 描述: 方法调用适配器
 * <p>用于封装 Getter 和 Setter 方法
 * @author suifeng
 * 日期: 2026/1/26
 */
public class MethodInvoker implements Invoker {

    private final Class<?> type;
    private final Method method;

    public MethodInvoker(Method method) {

        this.method = method;

        try {
            method.setAccessible(true);
        } catch (SecurityException e) {
            throw new ReflectionException("Could not set accessible for method: " + method.getName(), e);
        }

        // 如果只有一个参数，说明是 Setter，类型为参数类型
        if (method.getParameterCount() == 1) {
            this.type = method.getParameterTypes()[0];
        } else {
            // 否则是 Getter，类型为返回值类型
            this.type = method.getReturnType();
        }
    }

    @Override
    public Object invoke(Object target, Object[] args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException("Invoke method failed: " + method.getName(), e);
        }
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
