package io.github.timemachinelab.autoconfigure.retry.aop;

import io.github.timemachinelab.autoconfigure.retry.annotation.Retryable;
import io.github.timemachinelab.autoconfigure.retry.strategy.DelayCalculator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 重试拦截器
 * 拦截 @Retryable 注解的方法，应用重试逻辑
 * 
 * @author TimeMachineLab
 */
@Aspect
public class RetryInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RetryInterceptor.class);

    /**
     * 拦截所有标注了 @Retryable 的方法
     */
    @Around("@annotation(io.github.timemachinelab.autoconfigure.retry.annotation.Retryable)")
    public Object aroundRetryable(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Retryable retryable = method.getAnnotation(Retryable.class);

        if (retryable == null) {
            return joinPoint.proceed();
        }

        // 从注解中读取配置
        int maxAttempts = retryable.maxAttempts();
        Class<? extends Throwable>[] includeExceptions = retryable.include();
        Class<? extends Throwable>[] excludeExceptions = retryable.exclude();

        // 创建延迟计算器（组合退避策略和抖动算法）
        DelayCalculator delayCalculator = new DelayCalculator(
            retryable.backoffType(),
            retryable.jitterType(),
            retryable.baseDelay(),
            retryable.maxDelay()
        );

        // TODO: 这里需要调用你在 core 模块实现的 RetryExecutor 和 RetryBudget
        // 目前先实现一个简单的重试逻辑作为占位符
        
        Throwable lastException = null;
        long previousDelay = retryable.baseDelay();
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Executing method {} - attempt {}/{} (backoff={}, jitter={})", 
                    method.getName(), attempt, maxAttempts, 
                    retryable.backoffType(), retryable.jitterType());
                
                return joinPoint.proceed();
                
            } catch (Throwable ex) {
                lastException = ex;
                
                // 检查是否应该重试此异常
                if (!shouldRetry(ex, includeExceptions, excludeExceptions)) {
                    log.debug("Exception {} is not retryable, throwing immediately", 
                        ex.getClass().getName());
                    throw ex;
                }
                
                // 如果是最后一次尝试，直接抛出异常
                if (attempt >= maxAttempts) {
                    log.warn("Retry exhausted after {} attempts for method {}", 
                        maxAttempts, method.getName());
                    throw ex;
                }
                
                // 使用延迟计算器计算延迟时间（组合退避 + 抖动）
                long delay = delayCalculator.calculateDelay(attempt, previousDelay);
                previousDelay = delay;
                
                log.debug("Method {} failed on attempt {}, retrying after {}ms (backoff={}, jitter={})", 
                    method.getName(), attempt, delay, 
                    retryable.backoffType(), retryable.jitterType());
                
                Thread.sleep(delay);
            }
        }
        
        throw lastException;
    }

    /**
     * 判断异常是否应该重试
     */
    private boolean shouldRetry(Throwable ex, 
                                Class<? extends Throwable>[] includeExceptions,
                                Class<? extends Throwable>[] excludeExceptions) {
        
        // 先检查黑名单（优先级更高）
        for (Class<? extends Throwable> excludeType : excludeExceptions) {
            if (excludeType.isAssignableFrom(ex.getClass())) {
                return false;
            }
        }
        
        // 如果没有配置白名单，默认重试所有异常
        if (includeExceptions.length == 0) {
            return true;
        }
        
        // 检查白名单
        for (Class<? extends Throwable> includeType : includeExceptions) {
            if (includeType.isAssignableFrom(ex.getClass())) {
                return true;
            }
        }
        
        return false;
    }
}
