package io.github.timemachinelab.reflectx;


import io.github.timemachinelab.reflectx.factory.DefaultObjectFactory;
import io.github.timemachinelab.reflectx.factory.ObjectFactory;
import io.github.timemachinelab.reflectx.wrapper.DefaultObjectWrapperFactory;
import io.github.timemachinelab.reflectx.wrapper.ObjectWrapperFactory;

/**
 * 系统级元对象工具
 * <p>
 * 职责：
 * 1. 持有全局单例的工厂，减少内存开销。
 * 2. 提供 NULL_META_OBJECT
 * </p>
 * @author suifeng
 * 日期: 2026/1/26
 */
public final class SystemMetaObject {

    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    
    // 空对象哨兵
    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(new NullObject(), DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

    private SystemMetaObject() {}

    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }

    private static class NullObject {}
}