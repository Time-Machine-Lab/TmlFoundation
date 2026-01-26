package io.github.timemachinelab.autoconfigure.retry.annotation;

/**
 * 退避策略类型枚举
 * 
 * @author TimeMachineLab
 */
public enum BackoffType {
    
    /**
     * 固定延迟
     * delay = baseDelay
     */
    FIXED,
    
    /**
     * 线性退避
     * delay = baseDelay × attempt
     */
    LINEAR,
    
    /**
     * 指数退避（推荐）
     * delay = baseDelay × 2^(attempt-1)
     */
    EXPONENTIAL
}
