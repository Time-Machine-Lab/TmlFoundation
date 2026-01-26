package io.github.timemachinelab.reflectx.wrapper;

import io.github.timemachinelab.reflectx.MetaObject;
import io.github.timemachinelab.reflectx.Reflector;
import io.github.timemachinelab.reflectx.exception.ReflectionException;
import io.github.timemachinelab.reflectx.factory.ObjectFactory;
import io.github.timemachinelab.reflectx.invoker.Invoker;
import io.github.timemachinelab.reflectx.parser.PropertyTokenizer;

import java.util.List;

/**
 * 描述: JavaBean 包装器
 * <p>
 * 核心逻辑：
 * 1. 组合了 Reflector
 * 2. 处理普通属性访问
 * 3. 委托 BaseWrapper 处理集合下标访问
 * </p>
 * @author suifeng
 * 日期: 2026/1/26 
 */
public class BeanWrapper extends BaseWrapper {

    private final Object object;
    private final Reflector reflector;

    public BeanWrapper(Object object) {
        super();
        this.object = object;
        // 利用 Reflector 分析类结构
        this.reflector = Reflector.forClass(object.getClass());
    }


    @Override
    public Object get(PropertyTokenizer prop) {
        // 如果有 index (如 items[0])，说明是集合访问
        if (prop.getIndex() != null) {
            // 1. 解析集合本身 (如 items)
            Object collection = resolveCollection(prop, object);
            // 2. 交给基类处理下标 (如 [0])
            return getCollectionValue(prop, collection);
        } else {
            // 普通属性访问，使用 Reflector
            return getBeanProperty(prop, object);
        }
    }

    @Override
    public void set(PropertyTokenizer prop, Object value) {
        if (prop.getIndex() != null) {
            Object collection = resolveCollection(prop, object);
            setCollectionValue(prop, collection, value);
        } else {
            setBeanProperty(prop, object, value);
        }
    }

    // 辅助方法：当遇到 items[0] 时，先获取 items 这个对象
    private Object resolveCollection(PropertyTokenizer prop, Object object) {
        if ("".equals(prop.getName())) {
            return object;
        }
        // 否则才是去取属性（例如 order.items[0]）
        return getBeanProperty(prop, object);
    }

    private Object getBeanProperty(PropertyTokenizer prop, Object object) {
        try {
            // prop.getName() 会返回 "items" (去掉了下标)
            Invoker invoker = reflector.getGetInvoker(prop.getName());
            return invoker.invoke(object, NO_ARGUMENTS);
        } catch (Throwable t) {
            throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass(), t);
        }
    }

    private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
        try {
            Invoker invoker = reflector.getSetInvoker(prop.getName());
            invoker.invoke(object, new Object[]{value});
        } catch (Throwable t) {
            throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass() + "' with value '" + value + "'", t);
        }
    }

    @Override
    public String[] getGetterNames() {
        return new String[0];
    }

    @Override
    public String[] getSetterNames() {
        return new String[0];
    }

    @Override
    public Class<?> getSetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            return null;
        } else {
            return reflector.getSetterType(name);
        }
    }

    @Override
    public Class<?> getGetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            return null;
        } else {
            return reflector.getGetterType(name);
        }
    }

    @Override
    public boolean hasSetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            return false;
        } else {
            return reflector.hasSetter(name);
        }
    }

    @Override
    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            return false;
        } else {
            return reflector.hasGetter(name);
        }
    }

    @Override
    public void add(Object element) {
        throw new UnsupportedOperationException("Bean is not a collection");
    }

    @Override
    public <E> void addAll(List<E> element) {
        throw new UnsupportedOperationException("Bean is not a collection");
    }

    @Override
    public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
        // 1. 获取 setter 参数类型 (例如: order 属性的类型是 Order.class)
        Class<?> type = getSetterType(prop.getName());
        try {
            // 2. 只有工厂知道怎么创建这个类 (处理接口、私有构造等)
            Object newObject = objectFactory.create(type);

            // 3. 设值回去 (user.setOrder(newObject))
            set(prop, newObject);

            // 4. 返回包装后的 MetaObject，以便递归继续
            return MetaObject.forObject(newObject, objectFactory, new DefaultObjectWrapperFactory());
        } catch (Exception e) {
            throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
        }
    }
}
