package io.github.timemachinelab.log.config;

import io.github.timemachinelab.log.interceptor.TraceIdScheduledAspect;
import io.github.timemachinelab.log.interceptor.TraceIdWebFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableConfigurationProperties(TmlLogProperties.class)
public class TmlLogAutoConfiguration {

    /**
     * 注册web的 TraceId 过滤器
     * 通过 tml.log.trace=true 开启（默认开启）
     */
    @Bean
    @ConditionalOnProperty(prefix = TmlLog.PREFIX, name = "trace", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceIdWebFilter> traceIdWebFilter() {
        FilterRegistrationBean<TraceIdWebFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdWebFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceIdWebFilter");
        // 设置最高优先级，确保 traceId 在其他过滤器之前设置
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * 注册定时任务 TraceId 切面
     * 通过 tml.log.trace=true 开启（默认开启）
     * 仅当项目中存在 @Scheduled 注解时生效
     */
    @Bean
    @ConditionalOnProperty(prefix = TmlLog.PREFIX, name = "trace", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(Scheduled.class)
    public TraceIdScheduledAspect traceIdScheduledAspect() {
        return new TraceIdScheduledAspect();
    }
}
