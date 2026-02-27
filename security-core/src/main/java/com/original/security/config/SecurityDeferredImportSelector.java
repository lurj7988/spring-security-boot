package com.original.security.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link SecurityAutoConfiguration} 的延迟导入选择器 (Deferred Import Selector)。
 * <p>
 * 该类负责在 Spring Boot 处理普通的 {@code @Configuration} 类之后
 * 再加载 {@code SecurityAutoConfiguration}。
 * 这样做可以确保用户在上下文中自定义注入的安全相关 Bean 能够先于框架的自动配置生效，
 * 从而使 {@link org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean}
 * 条件注解能够按照预期正常工作，防止 Bean 定义被过早加载而导致冲突或短路失效。
 *
 * @author Naulu
 * @since 0.1.0
 */
public class SecurityDeferredImportSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{SecurityAutoConfiguration.class.getName()};
    }
}
