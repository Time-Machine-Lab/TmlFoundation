package io.github.timemachinelab.reflectx.wrapper;

import io.github.timemachinelab.reflectx.parser.PropertyTokenizer;

import java.util.List;
import java.util.Map;

/**
 * 描述: Map 包装器
 * 让 Map 像 Bean 一样被访问，key 即为属性名
 * @author suifeng
 * 日期: 2026/1/26 
 */
public class MapWrapper extends BaseWrapper {

    private final Map<String, Object> map;

    public MapWrapper(Map<String, Object> map) {
        super();
        this.map = map;
    }

    @Override
    public Object get(PropertyTokenizer prop) {
        // 处理 map['key']
        if (prop.getIndex() != null) {
            Object collection = resolveCollection(prop, map);
            return getCollectionValue(prop, collection);
        } else {
            return map.get(prop.getName());
        }
    }

    @Override
    public void set(PropertyTokenizer prop, Object value) {
        if (prop.getIndex() != null) {
            Object collection = resolveCollection(prop, map);
            setCollectionValue(prop, collection, value);
        } else {
            map.put(prop.getName(), value);
        }
    }

    private Object resolveCollection(PropertyTokenizer prop, Object object) {
        if ("".equals(prop.getName())) {
            return object;
        } else {
            return map.get(prop.getName());
        }
    }

    @Override public String[] getGetterNames() { return map.keySet().toArray(new String[0]); }
    @Override public String[] getSetterNames() { return map.keySet().toArray(new String[0]); }
    @Override public Class<?> getSetterType(String name) { return Object.class; }
    @Override public Class<?> getGetterType(String name) { return Object.class; }
    @Override public boolean hasSetter(String name) { return true; }
    @Override public boolean hasGetter(String name) { return true; }
    @Override public void add(Object element) { throw new UnsupportedOperationException(); }
    @Override public <E> void addAll(List<E> element) { throw new UnsupportedOperationException(); }
}
