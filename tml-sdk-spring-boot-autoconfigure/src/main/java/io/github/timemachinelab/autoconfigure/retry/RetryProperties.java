package io.github.timemachinelab.autoconfigure.retry;

import io.github.timemachinelab.autoconfigure.retry.annotation.JitterType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 重试组件配置属性
 * 
 * @author TimeMachineLab
 */
@ConfigurationProperties(prefix = "tml.retry")
public class RetryProperties {

    /**
     * 是否启用重试功能
     */
    private boolean enabled = true;

    /**
     * 默认配置
     */
    private DefaultConfig defaultConfig = new DefaultConfig();

    /**
     * 重试预算配置
     */
    private BudgetConfig budget = new BudgetConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(DefaultConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public BudgetConfig getBudget() {
        return budget;
    }

    public void setBudget(BudgetConfig budget) {
        this.budget = budget;
    }

    /**
     * 默认重试配置
     */
    public static class DefaultConfig {
        
        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;

        /**
         * 基础延迟时间（毫秒）
         */
        private long baseDelay = 100;

        /**
         * 最大延迟时间（毫秒）
         */
        private long maxDelay = 60000;

        /**
         * 抖动类型
         */
        private JitterType jitterType = JitterType.FULL;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBaseDelay() {
            return baseDelay;
        }

        public void setBaseDelay(long baseDelay) {
            this.baseDelay = baseDelay;
        }

        public long getMaxDelay() {
            return maxDelay;
        }

        public void setMaxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
        }

        public JitterType getJitterType() {
            return jitterType;
        }

        public void setJitterType(JitterType jitterType) {
            this.jitterType = jitterType;
        }
    }

    /**
     * 重试预算配置
     */
    public static class BudgetConfig {
        
        /**
         * 是否启用重试预算
         */
        private boolean enabled = true;

        /**
         * 滑动窗口大小（秒）
         */
        private int windowSize = 10;

        /**
         * 最大重试比例（0.0 - 1.0）
         */
        private double maxRetryRatio = 0.1;

        /**
         * 最小请求数阈值
         */
        private int minRequestsThreshold = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(int windowSize) {
            this.windowSize = windowSize;
        }

        public double getMaxRetryRatio() {
            return maxRetryRatio;
        }

        public void setMaxRetryRatio(double maxRetryRatio) {
            this.maxRetryRatio = maxRetryRatio;
        }

        public int getMinRequestsThreshold() {
            return minRequestsThreshold;
        }

        public void setMinRequestsThreshold(int minRequestsThreshold) {
            this.minRequestsThreshold = minRequestsThreshold;
        }
    }
}
