package io.github.timemachinelab.reflectx;

import io.github.timemachinelab.reflectx.factory.ObjectFactory;
import io.github.timemachinelab.reflectx.parser.PropertyTokenizer;
import io.github.timemachinelab.reflectx.wrapper.BeanWrapper;
import io.github.timemachinelab.reflectx.wrapper.MapWrapper;
import io.github.timemachinelab.reflectx.wrapper.ObjectWrapper;
import io.github.timemachinelab.reflectx.wrapper.ObjectWrapperFactory;

import java.util.Collection;
import java.util.Map;

/**
 * 元对象 (Facade)
 * <p>
 * 核心职责：
 * 1. 递归导航：解析 a.b.c 这种深层路径。
 * 2. 自动创建：路径中有 null 时自动修补。
 * 3. 统一入口：屏蔽 Bean/Map/List 差异。
 * </p>
 * @author suifeng
 * 日期: 2026/1/26
 */
public class MetaObject {

    private final Object originalObject;
    private final ObjectWrapper objectWrapper;
    private final ObjectFactory objectFactory;
    private final ObjectWrapperFactory objectWrapperFactory;

    private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        this.originalObject = object;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;

        // 识别并包装对象 (策略模式)
        if (object instanceof ObjectWrapper) {
            this.objectWrapper = (ObjectWrapper) object;
        } else if (objectWrapperFactory.hasWrapperFor(object)) {
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
        } else if (object instanceof Map) {
            this.objectWrapper = new MapWrapper((Map) object);
        } else if (object instanceof Collection) {
             // 简单处理集合，BeanWrapper 足以应付 add 操作以外的读取
             this.objectWrapper = new BeanWrapper(object);
        } else {
            this.objectWrapper = new BeanWrapper(object);
        }
    }

    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        if (object == null) {
            return SystemMetaObject.NULL_META_OBJECT;
        }
        return new MetaObject(object, objectFactory, objectWrapperFactory);
    }

    // ================== 递归取值 ==================
    public Object getValue(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        
        if (prop.hasNext()) {
            // 贪吃蛇逻辑：
            // 1. 获取当前节点 (如 user.address.city，先切出 user)
            // 2. 为 user 创建 MetaObject
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                // 如果中间断了（user是null），直接返回 null
                return null;
            } else {
                // 3. 递归调用：让 user 去找 address.city
                return metaValue.getValue(prop.getChildren());
            }
        } else {
            // 终点逻辑：没有子节点了，直接通过 Wrapper 取值
            return objectWrapper.get(prop);
        }
    }

    // ================== 递归赋值 (含自动创建) ==================
    public void setValue(String name, Object value) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        
        if (prop.hasNext()) {
            // 1. 尝试获取下一层的 MetaObject
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                // 如果路径断了，且 value 不是 null（说明用户想赋值），则自动创建路径
                if (value == null && prop.getChildren() != null) {
                    // 如果要赋的值本身就是 null，且还有子路径，那就没必要创建中间对象了
                    return;
                } else {
                    // 2. 委托 Wrapper 自动 new 一个对象填坑
                    // BeanWrapper 会创建 Bean，MapWrapper 会创建 HashMap
                    metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
                }
            }
            // 3. 递归继续：让新创建的对象去处理剩下的路径
            metaValue.setValue(prop.getChildren(), value);
        } else {
            // 终点逻辑
            objectWrapper.set(prop, value);
        }
    }

    /**
     * 辅助方法：获取属性的 MetaObject 包装
     */
    public MetaObject metaObjectForProperty(String name) {
        Object value = getValue(name);
        return MetaObject.forObject(value, objectFactory, objectWrapperFactory);
    }

    public Object getOriginalObject() { return originalObject; }
    public String[] getGetterNames() { return objectWrapper.getGetterNames(); }
    public String[] getSetterNames() { return objectWrapper.getSetterNames(); }
    public boolean hasSetter(String name) { return objectWrapper.hasSetter(name); }
    public boolean hasGetter(String name) { return objectWrapper.hasGetter(name); }
}