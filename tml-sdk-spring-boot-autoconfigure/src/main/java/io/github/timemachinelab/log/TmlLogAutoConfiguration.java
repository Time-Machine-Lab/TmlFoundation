package io.github.timemachinelab.log;


import io.github.timemachinelab.constant.TmlConstant;
import io.github.timemachinelab.log.config.TmlLogConstant;
import io.github.timemachinelab.log.config.TmlLogProperties;
import io.github.timemachinelab.log.context.TraceContext;
import io.github.timemachinelab.log.interceptor.TmlLogScheduleTrace;
import io.github.timemachinelab.log.interceptor.TmlLogWebTrace;
import io.github.timemachinelab.log.mdc.TmlLogMdc;
import io.github.timemachinelab.log.mdc.TmlLogMdcAdapter;
import io.github.timemachinelab.log.mdc.TmlLogMdcInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 针对日志的自动配置类
 *
 * @author glser
 * @since 2026/01/16
 */
@Configuration
@EnableConfigurationProperties(TmlLogProperties.class)
public class TmlLogAutoConfiguration {

    @Autowired(required = false)
    private TraceContext traceContext;

    /**
     * 如果用户注册了自定义 TraceContext Bean，自动替换默认实现
     */
    @PostConstruct
    public void initTraceContext() {
        if (traceContext != null) {
            TraceContext.Holder.set(traceContext);
        }
    }

    /**
     * 注册默认的 TmlLogMdcAdapter
     */
    @Bean
    @ConditionalOnMissingBean(TmlLogMdc.class)
    @ConditionalOnProperty(prefix = TmlConstant.LOG, name = TmlLogConstant.TRACE_ID, havingValue = "true", matchIfMissing = true)
    public TmlLogMdc tmlLogMdcAdapter() {
        return new TmlLogMdcAdapter();
    }

    /**
     * 注册 MDCAdapter 初始化器
     */
    @Bean
    @ConditionalOnProperty(prefix = TmlConstant.LOG, name = TmlLogConstant.TRACE_ID, havingValue = "true", matchIfMissing = true)
    public TmlLogMdcInit mdcAdapterInitializer(List<TmlLogMdc> mdcAdapter) {
        return new TmlLogMdcInit(mdcAdapter);
    }

    /**
     * 注册web的 TraceId 过滤器
     * 通过 tml.log.trace=true 开启（默认开启）
     */
    @Bean
    @ConditionalOnProperty(prefix = TmlConstant.LOG, name = TmlLogConstant.TRACE_ID, havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TmlLogWebTrace> traceIdWebFilter() {
        FilterRegistrationBean<TmlLogWebTrace> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TmlLogWebTrace());
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
    @ConditionalOnProperty(prefix = TmlConstant.LOG, name = TmlLogConstant.TRACE_ID, havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(Scheduled.class)
    public TmlLogScheduleTrace traceIdScheduledAspect() {
        return new TmlLogScheduleTrace();
    }
}
