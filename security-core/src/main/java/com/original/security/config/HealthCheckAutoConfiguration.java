package com.original.security.config;

import com.original.security.health.CacheHealthIndicator;
import com.original.security.health.DatabaseHealthIndicator;
import com.original.security.health.JwtValidatorHealthIndicator;
import com.original.security.health.SecurityHealthIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 健康检查自动配置类。
 * <p>
 * 配置安全组件的健康检查指示器，包括：
 * <ul>
 *     <li>{@link SecurityHealthIndicator} - 主要安全组件健康检查</li>
 *     <li>{@link DatabaseHealthIndicator} - 数据库连接检查</li>
 *     <li>{@link JwtValidatorHealthIndicator} - JWT 验证器检查</li>
 *     <li>{@link CacheHealthIndicator} - 缓存服务检查</li>
 * </ul>
 * <p>
 * 通过 {@code security.health.enabled} 属性控制是否启用。
 *
 * @author bmad
 * @since 0.1.0
 */
@Configuration
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnProperty(prefix = "security.health", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(HealthCheckProperties.class)
public class HealthCheckAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckAutoConfiguration.class);

    /**
     * 创建数据库健康检查指示器。
     *
     * @param dataSourceProvider 数据源提供者（可选）
     * @param properties 健康检查配置属性
     * @return DatabaseHealthIndicator 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DatabaseHealthIndicator databaseHealthIndicator(
            ObjectProvider<DataSource> dataSourceProvider,
            HealthCheckProperties properties) {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        log.debug("HealthCheckAutoConfiguration: Creating DatabaseHealthIndicator (dataSource: {}, timeout: {}ms)",
                dataSource != null ? "available" : "not available",
                properties.getCheckTimeoutMs());
        return new DatabaseHealthIndicator(dataSource, properties);
    }

    /**
     * 创建 JWT 验证器健康检查指示器。
     *
     * @param jwtPropertiesProvider JWT 配置属性提供者（可选）
     * @return JwtValidatorHealthIndicator 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtValidatorHealthIndicator jwtValidatorHealthIndicator(ObjectProvider<JwtProperties> jwtPropertiesProvider) {
        JwtProperties jwtProperties = jwtPropertiesProvider.getIfAvailable();
        log.debug("HealthCheckAutoConfiguration: Creating JwtValidatorHealthIndicator (jwtProperties: {})",
                jwtProperties != null ? "available" : "not available");
        return new JwtValidatorHealthIndicator(jwtProperties);
    }

    /**
     * 创建缓存健康检查指示器。
     *
     * @param cacheManagerProvider 缓存管理器提供者（可选）
     * @return CacheHealthIndicator 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheHealthIndicator cacheHealthIndicator(ObjectProvider<CacheManager> cacheManagerProvider) {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        log.debug("HealthCheckAutoConfiguration: Creating CacheHealthIndicator (cacheManager: {})",
                cacheManager != null ? "available" : "not available");
        return new CacheHealthIndicator(cacheManager);
    }

    /**
     * 创建安全组件健康检查指示器（主入口）。
     * <p>
     * Bean 名称为 "securityHealthIndicator"，Spring Boot Actuator 将在
     * /actuator/health 端点中自动包含此健康检查，路径为 /actuator/health/security。
     *
     * @param databaseHealthIndicator 数据库健康检查器
     * @param jwtValidatorHealthIndicator JWT 验证器健康检查器
     * @param cacheHealthIndicator 缓存健康检查器
     * @return SecurityHealthIndicator 实例
     */
    @Bean("securityHealthIndicator")
    @ConditionalOnMissingBean(name = "securityHealthIndicator")
    public SecurityHealthIndicator securityHealthIndicator(
            DatabaseHealthIndicator databaseHealthIndicator,
            JwtValidatorHealthIndicator jwtValidatorHealthIndicator,
            CacheHealthIndicator cacheHealthIndicator) {
        log.debug("HealthCheckAutoConfiguration: Creating SecurityHealthIndicator");
        return new SecurityHealthIndicator(
                databaseHealthIndicator,
                jwtValidatorHealthIndicator,
                cacheHealthIndicator
        );
    }
}
