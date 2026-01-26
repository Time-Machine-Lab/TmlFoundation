package io.github.timemachinelab.reflectx.exception;

/**
 * 描述: 反射调用相关异常
 * @author suifeng
 * 日期: 2026/1/26
 */
public class ReflectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(Throwable cause) {
        super(cause);
    }
}
