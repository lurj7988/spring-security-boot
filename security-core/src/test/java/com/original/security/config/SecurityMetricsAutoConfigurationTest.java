package com.original.security.config;

import com.original.security.observability.SecurityMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link SecurityMetricsConfig} integration.
 */
public class SecurityMetricsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class, SecurityMetricsConfig.class));

    @Test
    public void testSecurityMetricsBeanIsRegisteredWhenMeterRegistryIsPresent() {
        contextRunner
                .withUserConfiguration(MeterRegistryConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityMetrics.class);
                    assertThat(context.getBean(SecurityMetrics.class).isMetricsEnabled()).isTrue();
                });
    }

    @Test
    public void testSecurityMetricsBeanIsRegisteredWithCustomPercentiles() {
        contextRunner
                .withUserConfiguration(MeterRegistryConfig.class)
                .withPropertyValues("security.metrics.duration-percentiles=0.5,0.9,0.95")
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityMetrics.class);
                    assertThat(context).hasSingleBean(SecurityMetricsProperties.class);

                    SecurityMetricsProperties properties = context.getBean(SecurityMetricsProperties.class);
                    assertThat(properties.getDurationPercentiles()).containsExactly(0.5, 0.9, 0.95);
                });
    }

    @Test
    public void testSecurityMetricsNoOpBeanIsRegisteredWhenMeterRegistryIsMissing() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityMetrics.class);
                    assertThat(context.getBean(SecurityMetrics.class).isMetricsEnabled()).isFalse();
                });
    }

    @Test
    public void testSecurityMetricsBeanWithProperties() {
        contextRunner
                .withUserConfiguration(MeterRegistryConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityMetrics.class);
                    assertThat(context).hasSingleBean(SecurityMetricsProperties.class);

                    SecurityMetricsProperties properties = context.getBean(SecurityMetricsProperties.class);
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getDurationPercentiles()).containsExactly(0.5, 0.95, 0.99);
                });
    }

    @Test
    public void testSecurityMetricsDisabledViaProperty() {
        contextRunner
                .withPropertyValues("security.metrics.enabled=false")
                .withUserConfiguration(MeterRegistryConfig.class)
                .run(context -> {
                    // When disabled via property, the config class is not loaded,
                    // and since nothing else provides SecurityMetrics, it should be missing
                    assertThat(context).doesNotHaveBean(SecurityMetrics.class);
                });
    }

    @Configuration
    static class MeterRegistryConfig {
        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
