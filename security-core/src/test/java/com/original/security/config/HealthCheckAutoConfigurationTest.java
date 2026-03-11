package com.original.security.config;

import com.original.security.health.CacheHealthIndicator;
import com.original.security.health.DatabaseHealthIndicator;
import com.original.security.health.JwtValidatorHealthIndicator;
import com.original.security.health.SecurityHealthIndicator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * HealthCheckAutoConfiguration 测试类。
 *
 * @author bmad
 * @since 0.1.0
 */
class HealthCheckAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HealthCheckAutoConfiguration.class));

    @Test
    @DisplayName("autoConfiguration_默认启用_创建所有HealthIndicator")
    void autoConfiguration_DefaultEnabled_CreatesAllHealthIndicators() {
        contextRunner
                .withBean(DataSource.class, () -> mock(DataSource.class))
                .withBean(JwtProperties.class, () -> {
                    JwtProperties props = new JwtProperties();
                    props.setSecret("dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdmFsaWRhdGlvbi1wdXJwb3Nl");
                    return props;
                })
                .withBean("cacheManager", ConcurrentMapCacheManager.class, ConcurrentMapCacheManager::new)
                .run(context -> {
                    assertThat(context).hasBean("databaseHealthIndicator");
                    assertThat(context).hasBean("jwtValidatorHealthIndicator");
                    assertThat(context).hasBean("cacheHealthIndicator");
                    assertThat(context).hasBean("securityHealthIndicator");
                });
    }

    @Test
    @DisplayName("autoConfiguration_显式禁用_不创建HealthIndicator")
    void autoConfiguration_ExplicitlyDisabled_DoesNotCreateHealthIndicators() {
        contextRunner
                .withPropertyValues("security.health.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DatabaseHealthIndicator.class);
                    assertThat(context).doesNotHaveBean(JwtValidatorHealthIndicator.class);
                    assertThat(context).doesNotHaveBean(CacheHealthIndicator.class);
                    assertThat(context).doesNotHaveBean(SecurityHealthIndicator.class);
                });
    }

    @Test
    @DisplayName("autoConfiguration_无DataSource_仍创建其他HealthIndicator")
    void autoConfiguration_NoDataSource_StillCreatesOtherHealthIndicators() {
        contextRunner
                .withBean(JwtProperties.class, () -> {
                    JwtProperties props = new JwtProperties();
                    props.setSecret("dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdmFsaWRhdGlvbi1wdXJwb3Nl");
                    return props;
                })
                .run(context -> {
                    assertThat(context).hasBean("databaseHealthIndicator");
                    assertThat(context).hasBean("jwtValidatorHealthIndicator");
                    assertThat(context).hasBean("cacheHealthIndicator");
                    assertThat(context).hasBean("securityHealthIndicator");
                });
    }

    @Test
    @DisplayName("autoConfiguration_HealthCheckProperties_正确绑定配置")
    void autoConfiguration_HealthCheckProperties_BindsConfiguration() {
        contextRunner
                .withPropertyValues(
                        "security.health.enabled=true",
                        "security.health.check-timeout-ms=3000"
                )
                .run(context -> {
                    HealthCheckProperties properties = context.getBean(HealthCheckProperties.class);
                    assertTrue(properties.isEnabled());
                    assertEquals(3000, properties.getCheckTimeoutMs());
                });
    }
}
