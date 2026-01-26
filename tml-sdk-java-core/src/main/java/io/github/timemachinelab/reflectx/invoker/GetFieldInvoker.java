package io.github.timemachinelab.reflectx.invoker;

import io.github.timemachinelab.reflectx.exception.ReflectionException;

import java.lang.reflect.Field;

/**
 * 描述: 获取值适配器
 * @author suifeng
 * 日期: 2026/1/26
 */
public class GetFieldInvoker implements Invoker {

    private final Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
        try {
            field.setAccessible(true);
        } catch (SecurityException e) {
            throw new ReflectionException("Could not set accessible for field: " + field.getName(), e);
        }
    }

    @Override
    public Object invoke(Object target, Object[] args) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Cannot get field: " + field.getName(), e);
        }
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
