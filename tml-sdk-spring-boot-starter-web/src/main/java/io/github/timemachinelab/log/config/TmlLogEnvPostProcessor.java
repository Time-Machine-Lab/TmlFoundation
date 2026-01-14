package io.github.timemachinelab.log.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class TmlLogEnvPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 从application.yml中读取日志相关配置
        TmlLogProperties tmlLogProperties = Binder.get(environment)
                .bind(TmlLog.PREFIX, TmlLogProperties.class)
                .orElseGet(TmlLogProperties::new);
        // 加载数据到环境变量
        if (tmlLogProperties == null || !tmlLogProperties.isEnable()) {
            // 不启用日志则不加载数据，且关闭日志功能
            System.setProperty("logging.config", "classpath:log4j2-noop.xml");
            return;
        }
        // env从spring.profiles.active获取
        String env = environment.getProperty("spring.profiles.active");
        tmlLogProperties.setEnv(env);
        tmlLogProperties.apply();
    }

    @Override
    public int getOrder() {
        // 设置为 ConfigFileApplicationListener 之后执行，确保 application.yml 已经加载
        // ConfigFileApplicationListener 的优先级是 HIGHEST_PRECEDENCE + 10
        return Ordered.HIGHEST_PRECEDENCE + 11;
    }
}
