package io.github.timemachinelab.autoconfigure.retry.strategy;

import io.github.timemachinelab.autoconfigure.retry.annotation.BackoffType;
import io.github.timemachinelab.autoconfigure.retry.annotation.JitterType;

/**
 * 延迟计算器
 * 组合退避策略和抖动算法，计算最终的重试延迟
 * 
 * @author TimeMachineLab
 */
public class DelayCalculator {

    private final BackoffType backoffType;
    private final JitterType jitterType;
    private final long baseDelay;
    private final long maxDelay;

    public DelayCalculator(BackoffType backoffType, JitterType jitterType, 
                          long baseDelay, long maxDelay) {
        this.backoffType = backoffType;
        this.jitterType = jitterType;
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    /**
     * 计算延迟时间
     * 
     * @param attempt 当前重试次数（从 1 开始）
     * @param previousDelay 上一次的延迟（用于 Decorrelated Jitter）
     * @return 最终延迟时间（毫秒）
     */
    public long calculateDelay(int attempt, long previousDelay) {
        // 1. 根据退避策略计算基准延迟
        long backoffDelay = calculateBackoff(attempt);
        
        // 2. 根据抖动算法添加随机性
        long finalDelay = applyJitter(backoffDelay, previousDelay);
        
        return finalDelay;
    }

    /**
     * 计算退避延迟（基准值）
     */
    private long calculateBackoff(int attempt) {
        long delay;
        
        switch (backoffType) {
            case FIXED:
                // 固定延迟
                delay = baseDelay;
                break;
                
            case LINEAR:
                // 线性退避：baseDelay × attempt
                delay = baseDelay * attempt;
                break;
                
            case EXPONENTIAL:
                // 指数退避：baseDelay × 2^(attempt-1)
                delay = baseDelay * (long) Math.pow(2, attempt - 1);
                break;
                
            default:
                delay = baseDelay;
        }
        
        // 限制在 [baseDelay, maxDelay] 范围内
        delay = Math.max(delay, baseDelay);
        delay = Math.min(delay, maxDelay);
        
        return delay;
    }

    /**
     * 应用抖动算法
     */
    private long applyJitter(long backoffDelay, long previousDelay) {
        long delay;
        
        switch (jitterType) {
            case FULL:
                // 全抖动：random(0, backoff)
                delay = (long) (Math.random() * backoffDelay);
                break;
                
            case EQUAL:
                // 均等抖动：backoff/2 + random(0, backoff/2)
                delay = backoffDelay / 2 + (long) (Math.random() * (backoffDelay / 2));
                break;
                
            case DECORRELATED:
                // 去相关抖动：random(baseDelay, previousDelay * 3)
                long min = baseDelay;
                long max = Math.min(previousDelay * 3, maxDelay);
                delay = min + (long) (Math.random() * (max - min));
                break;
                
            case NONE:
                // 无抖动：使用原始退避时间
                delay = backoffDelay;
                break;
                
            default:
                delay = backoffDelay;
        }
        
        return delay;
    }
}
