package io.github.timemachinelab.reflectx.wrapper;

import io.github.timemachinelab.reflectx.parser.PropertyTokenizer;

import java.util.List;

/**
 * 描述: 对象包装器接口
 * <p>
 * 统一 Bean、Map、Collection 的访问方式，屏蔽底层差异。
 * </p>
 * @author suifeng
 * 日期: 2026/1/26 
 */
public interface ObjectWrapper {

    /**
     * 获取属性值
     * @param prop 分词器（包含了 index 信息，如 name[0]）
     */
    Object get(PropertyTokenizer prop);

    /**
     * 设置属性值
     */
    void set(PropertyTokenizer prop, Object value);

    String[] getGetterNames();
    String[] getSetterNames();
    Class<?> getSetterType(String name);
    Class<?> getGetterType(String name);
    boolean hasSetter(String name);
    boolean hasGetter(String name);

    /**
     * 向集合添加元素
     */
    void add(Object element);

    /**
     * 向集合添加一组元素
     */
    <E> void addAll(List<E> element);
}
