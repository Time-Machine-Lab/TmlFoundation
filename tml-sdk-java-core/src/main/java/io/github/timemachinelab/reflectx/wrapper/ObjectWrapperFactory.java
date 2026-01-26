package io.github.timemachinelab.reflectx.wrapper;

import io.github.timemachinelab.reflectx.MetaObject;

/**
 * 描述: 包装器工厂接口
 * @author suifeng
 * 日期: 2026/1/26 
 */
public interface ObjectWrapperFactory {

    /**
     * 是否有针对该对象的特殊包装器
     */
    boolean hasWrapperFor(Object object);

    /**
     * 获取包装器
     */
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
