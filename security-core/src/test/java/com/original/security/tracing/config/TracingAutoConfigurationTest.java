package com.original.security.tracing.config;

import com.original.security.tracing.DefaultSecurityTracer;
import com.original.security.tracing.SecurityTracer;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * TracingAutoConfiguration 测试类。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class TracingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TracingAutoConfiguration.class));

    @Nested
    @DisplayName("Auto-Configuration Activation Tests")
    class AutoConfigurationActivationTests {

        @Test
        @DisplayName("testAutoConfiguration_WhenEnabled_CreatesNoOpTracer")
        void testAutoConfiguration_WhenEnabled_CreatesNoOpTracer() {
            contextRunner
                    .withPropertyValues("security.tracing.enabled=true")
                    .run(context -> {
                        assertThat(context).hasSingleBean(SecurityTracer.class);
                        // When no Micrometer Tracer is available, DefaultSecurityTracer(null) is used as NoOp
                        assertThat(context.getBean(SecurityTracer.class).isAvailable()).isFalse();
                    });
        }

        @Test
        @DisplayName("testAutoConfiguration_WhenDisabled_DoesNotCreateTracer")
        void testAutoConfiguration_WhenDisabled_DoesNotCreateTracer() {
            contextRunner
                    .withPropertyValues("security.tracing.enabled=false")
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(SecurityTracer.class);
                    });
        }

        @Test
        @DisplayName("testAutoConfiguration_DefaultEnabled_CreatesTracer")
        void testAutoConfiguration_DefaultEnabled_CreatesTracer() {
            contextRunner
                    .run(context -> {
                        assertThat(context).hasSingleBean(SecurityTracer.class);
                    });
        }
    }

    @Nested
    @DisplayName("Tracer Bean Creation Tests")
    class TracerBeanCreationTests {

        @Test
        @DisplayName("testSecurityTracer_WhenMicrometerTracerAvailable_CreatesDefaultSecurityTracer")
        void testSecurityTracer_WhenMicrometerTracerAvailable_CreatesDefaultSecurityTracer() {
            contextRunner
                    .withUserConfiguration(TracerMockConfiguration.class)
                    .run(context -> {
                        assertThat(context).hasSingleBean(SecurityTracer.class);
                        assertThat(context.getBean(SecurityTracer.class)).isInstanceOf(DefaultSecurityTracer.class);
                        assertThat(context.getBean(SecurityTracer.class).isAvailable()).isTrue();
                    });
        }

        @Test
        @DisplayName("testSecurityTracer_WhenMicrometerTracerNotAvailable_CreatesNoOpTracer")
        void testSecurityTracer_WhenMicrometerTracerNotAvailable_CreatesNoOpTracer() {
            contextRunner
                    .run(context -> {
                        assertThat(context).hasSingleBean(SecurityTracer.class);
                        assertThat(context.getBean(SecurityTracer.class).isAvailable()).isFalse();
                    });
        }
    }

    @Nested
    @DisplayName("Configuration Properties Tests")
    class ConfigurationPropertiesTests {

        @Test
        @DisplayName("testProperties_CustomMaskLengths_AppliedCorrectly")
        void testProperties_CustomMaskLengths_AppliedCorrectly() {
            contextRunner
                    .withUserConfiguration(TracerMockConfiguration.class)
                    .withPropertyValues(
                            "security.tracing.enabled=true",
                            "security.tracing.username-mask-length=5",
                            "security.tracing.token-mask-length=10"
                    )
                    .run(context -> {
                        SecurityTracer tracer = context.getBean(SecurityTracer.class);
                        assertThat(tracer).isInstanceOf(DefaultSecurityTracer.class);
                        // Verify masking with custom lengths
                        assertThat(tracer.maskUsername("testuser")).isEqualTo("testu***");
                        assertThat(tracer.maskToken("1234567890123456")).isEqualTo("1234567890******");
                    });
        }

        @Test
        @DisplayName("testProperties_SamplingRate_ConfiguredCorrectly")
        void testProperties_SamplingRate_ConfiguredCorrectly() {
            contextRunner
                    .withPropertyValues(
                            "security.tracing.enabled=true",
                            "security.tracing.sampling-rate=0.5"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(SecurityTracingProperties.class);
                        SecurityTracingProperties properties = context.getBean(SecurityTracingProperties.class);
                        assertThat(properties.getSamplingRate()).isEqualTo(0.5f);
                    });
        }
    }

    @Nested
    @DisplayName("Conditional Bean Tests")
    class ConditionalBeanTests {

        @Test
        @DisplayName("testNoOpTracer_WhenCustomTracerExists_DoesNotOverride")
        void testNoOpTracer_WhenCustomTracerExists_DoesNotOverride() {
            contextRunner
                    .withUserConfiguration(CustomTracerConfiguration.class)
                    .run(context -> {
                        assertThat(context).hasSingleBean(SecurityTracer.class);
                        assertThat(context.getBean(SecurityTracer.class))
                                .isInstanceOf(DefaultSecurityTracer.class);
                    });
        }
    }

    /**
     * Mock configuration providing a Micrometer Tracer.
     */
    @Configuration
    static class TracerMockConfiguration {
        @Bean
        Tracer tracer() {
            return mock(Tracer.class);
        }
    }

    /**
     * Custom tracer configuration for testing @ConditionalOnMissingBean.
     */
    @Configuration
    static class CustomTracerConfiguration {
        @Bean
        SecurityTracer customSecurityTracer() {
            // 使用 DefaultSecurityTracer(null) 作为自定义无操作实现
            return new DefaultSecurityTracer(null);
        }
    }
}
