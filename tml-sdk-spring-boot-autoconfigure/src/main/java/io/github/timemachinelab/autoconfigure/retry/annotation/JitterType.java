package io.github.timemachinelab.autoconfigure.retry.annotation;

/**
 * 抖动类型枚举
 * 
 * @author TimeMachineLab
 */
public enum JitterType {
    
    /**
     * 全抖动：random(0, backoff)
     * AWS 推荐，最大程度避免共振
     */
    FULL,
    
    /**
     * 均等抖动：backoff/2 + random(0, backoff/2)
     * 保证最小延迟，同时添加随机性
     */
    EQUAL,
    
    /**
     * 去相关抖动：random(base, previous * 3)
     * 考虑上次延迟，适合长时间重试场景
     */
    DECORRELATED,
    
    /**
     * 无抖动：使用原始退避时间
     */
    NONE
}
