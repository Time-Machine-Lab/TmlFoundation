package io.github.timemachinelab.reflectx.wrapper;


import io.github.timemachinelab.reflectx.MetaObject;
import io.github.timemachinelab.reflectx.exception.ReflectionException;

public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {

    @Override
    public boolean hasWrapperFor(Object object) {
        // 默认实现通常返回 false，表示没有自定义的特殊 Wrapper
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new ReflectionException("The DefaultObjectWrapperFactory should not be called getWrapperFor.");
    }
}