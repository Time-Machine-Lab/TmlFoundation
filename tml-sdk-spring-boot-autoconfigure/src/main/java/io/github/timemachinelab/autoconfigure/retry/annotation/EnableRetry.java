package io.github.timemachinelab.autoconfigure.retry.annotation;

import io.github.timemachinelab.autoconfigure.retry.RetryAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用重试功能的注解
 * 在 Spring Boot 配置类上使用
 * 
 * @author TimeMachineLab
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RetryAutoConfiguration.class)
public @interface EnableRetry {
}
