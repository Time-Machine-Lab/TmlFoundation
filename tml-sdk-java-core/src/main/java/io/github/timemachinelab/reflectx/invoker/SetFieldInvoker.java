package io.github.timemachinelab.reflectx.invoker;

import io.github.timemachinelab.reflectx.exception.ReflectionException;

import java.lang.reflect.Field;

/**
 * 描述: 字段设值适配器
 * <p> 用于在没有 Setter 方法时，直接通过字段写入数据
 * @author suifeng
 * 日期: 2026/1/26
 */
public class SetFieldInvoker implements Invoker {

    private final Field field;

    public SetFieldInvoker(Field field) {
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
            field.set(target, args[0]);
            return null;
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Cannot set field: " + field.getName(), e);
        }
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
