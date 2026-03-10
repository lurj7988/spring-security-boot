package com.original.security.config;

import com.original.security.observability.SecurityMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 安全 Metrics 自动配置类。
 * <p>
 * 配置 Micrometer MeterRegistry 并注册安全相关的 Metrics。
 * 通过 {@code security.metrics.enabled} 属性控制是否启用。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "security.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SecurityMetricsProperties.class)
public class SecurityMetricsConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityMetricsConfig.class);

    /**
     * 配置 MeterRegistry 添加全局标签。
     *
     * @return MeterRegistryCustomizer 用于添加通用标签
     */
    @Bean
    @ConditionalOnMissingBean
    public MeterRegistryCustomizer<MeterRegistry> securityMetricsCommonTags() {
        return registry -> {
            log.info("SecurityMetricsConfig: Registering common tags for security metrics");
            registry.config().commonTags("component", "security");
        };
    }

    /**
     * 安全 Metrics 注册表 Bean。
     * <p>
     * 仅当 MeterRegistry 可用时才创建此 Bean。
     *
     * @param meterRegistry Micrometer MeterRegistry（可选）
     * @param properties 安全 Metrics 配置属性
     * @return SecurityMetrics 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public SecurityMetrics securityMetrics(MeterRegistry meterRegistry, SecurityMetricsProperties properties) {
        log.info("SecurityMetricsConfig: Creating SecurityMetrics bean with properties");
        return new SecurityMetrics(meterRegistry, properties);
    }

    /**
     * 创建无 MeterRegistry 时的 SecurityMetrics Bean。
     * <p>
     * 当 Actuator 未启用时提供空实现。
     *
     * @param properties 安全 Metrics 配置属性
     * @return SecurityMetrics 实例（不记录 metrics）
     */
    @Bean
    @ConditionalOnMissingBean({SecurityMetrics.class, MeterRegistry.class})
    public SecurityMetrics securityMetricsNoOp(SecurityMetricsProperties properties) {
        log.warn("SecurityMetricsConfig: MeterRegistry not available, creating no-op SecurityMetrics");
        return new SecurityMetrics(null, properties);
    }
}
