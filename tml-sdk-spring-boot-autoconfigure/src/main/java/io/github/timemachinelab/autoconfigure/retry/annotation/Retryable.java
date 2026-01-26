package io.github.timemachinelab.autoconfigure.retry.annotation;

import java.lang.annotation.*;

/**
 * 标记方法需要重试的注解
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @Retryable(
 *     backoffType = BackoffType.EXPONENTIAL,
 *     jitterType = JitterType.FULL,
 *     maxAttempts = 3,
 *     baseDelay = 100,
 *     maxDelay = 60000
 * )
 * public User getUser(Long id) {
 *     // 业务代码
 * }
 * }
 * </pre>
 * 
 * @author TimeMachineLab
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {

    /**
     * 退避策略类型
     * 默认 EXPONENTIAL（指数退避）
     */
    BackoffType backoffType() default BackoffType.EXPONENTIAL;

    /**
     * 抖动算法类型
     * 默认 FULL（全抖动）
     */
    JitterType jitterType() default JitterType.FULL;

    /**
     * 最大重试次数（包含首次调用）
     * 默认 3 次
     */
    int maxAttempts() default 3;

    /**
     * 基础延迟时间（毫秒）
     * 默认 100ms
     */
    long baseDelay() default 100;

    /**
     * 最大延迟时间（毫秒）
     * 默认 60000ms (60秒)
     */
    long maxDelay() default 60000;

    /**
     * 需要重试的异常类型（白名单）
     * 默认为空数组，表示重试所有异常
     */
    Class<? extends Throwable>[] include() default {};

    /**
     * 不需要重试的异常类型（黑名单）
     * 优先级高于 include
     */
    Class<? extends Throwable>[] exclude() default {};
}
