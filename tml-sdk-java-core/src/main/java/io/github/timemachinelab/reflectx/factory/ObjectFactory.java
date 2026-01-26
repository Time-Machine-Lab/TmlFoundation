package io.github.timemachinelab.reflectx.factory;

import java.util.List;

/**
 * 描述: 对象工厂接口
 * <p>
 * 职责：负责实例化对象。
 * 扩展性：用户可以实现此接口，接管对象的创建逻辑（例如对接 Spring 容器）。
 * </p>
 * @author suifeng
 * 日期: 2026/1/26
 */
public interface ObjectFactory {

    /**
     * 使用默认构造函数创建对象
     * @param type 目标类型
     * @return 实例
     */
    <T> T create(Class<T> type);

    /**
     * 使用指定构造函数创建对象
     *
     * @param type                目标类型
     * @param constructorArgTypes 构造函数参数类型列表
     * @param constructorArgs     构造函数参数值列表
     * @return 实例
     */
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

    /**
     * 判断是否为集合类型
     * (用于辅助Wrapper层判断是否需要进行集合操作)
     */
    <T> boolean isCollection(Class<T> type);
}
