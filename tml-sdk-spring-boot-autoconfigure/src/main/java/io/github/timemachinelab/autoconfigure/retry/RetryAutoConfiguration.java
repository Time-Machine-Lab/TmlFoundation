package io.github.timemachinelab.autoconfigure.retry;

import io.github.timemachinelab.autoconfigure.retry.aop.RetryInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 重试组件自动配置类
 * 
 * @author TimeMachineLab
 */
@Configuration
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@ConditionalOnProperty(prefix = "tml.retry", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RetryProperties.class)
@EnableAspectJAutoProxy
public class RetryAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RetryAutoConfiguration.class);

    public RetryAutoConfiguration() {
        log.info("TML Retry Component Auto Configuration initialized");
    }

    /**
     * 注册重试拦截器
     */
    @Bean
    public RetryInterceptor retryInterceptor() {
        log.info("Registering RetryInterceptor bean");
        return new RetryInterceptor();
    }

    // TODO: 当你实现了 core 模块后，可以在这里注册更多 Bean
    // 例如：
    // @Bean
    // public RetryExecutor retryExecutor(RetryProperties properties) {
    //     return new RetryExecutor(...);
    // }
    //
    // @Bean
    // public RetryBudget retryBudget(RetryProperties properties) {
    //     return new TokenBucketRetryBudget(...);
    // }
}
