package io.github.timemachinelab.reflectx.invoker;

/**
 * 描述: 调用者接口
 * <p>将方法调用(Method)和字段访问(Field)统一封装为可执行对象。
 * <p>上层无需关心底层是调用方法还是直接操作字段。
 * @author suifeng
 * 日期: 2026/1/26
 */
public interface Invoker {

    /**
     * 执行调用
     *
     * @param target 目标对象实例
     * @param args   执行参数（Getter调用传 null）
     * @return 执行结果
     */
    Object invoke(Object target, Object[] args);

    /**
     * 获取调用者的具体类型
     * <p>
     * 如：Getter的返回值类型，Setter的参数类型，Field的字段类型
     * </p>
     *
     * @return 类型Class
     */
    Class<?> getType();
}
