package com.original.security.logging.config;

import com.original.security.logging.DefaultSecurityLogger;
import com.original.security.logging.SensitiveDataMasker;
import com.original.security.logging.SecurityLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityLoggingAutoConfiguration 测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DisplayName("SecurityLoggingAutoConfiguration Tests")
class SecurityLoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    SecurityLoggingAutoConfiguration.class
            ));

    @Nested
    @DisplayName("Auto-configuration tests")
    class AutoConfigurationTests {

        @Test
        @DisplayName("Should configure SecurityLogger when enabled by default")
        void testAutoConfiguration_DefaultEnabled_ConfiguresLogger() {
            contextRunner.run(context -> {
                assertThat(context).hasSingleBean(SecurityLogger.class);
                assertThat(context.getBean(SecurityLogger.class)).isInstanceOf(DefaultSecurityLogger.class);
            });
        }

        @Test
        @DisplayName("Should not configure SecurityLogger when disabled")
        void testAutoConfiguration_Disabled_DoesNotConfigureLogger() {
            contextRunner
                    .withPropertyValues("security.logging.enabled=false")
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(SecurityLogger.class);
                    });
        }
    }

    @Nested
    @DisplayName("Properties binding tests")
    class PropertiesBindingTests {

        @Test
        @DisplayName("Should bind security.logging properties")
        void testPropertiesBinding_BindsCorrectly() {
            contextRunner
                    .withPropertyValues(
                            "security.logging.enabled=true",
                            "security.logging.json-output=false",
                            "security.logging.include-stack-trace=false",
                            "security.logging.include-client-ip=false"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(SecurityLoggingProperties.class);
                        SecurityLoggingProperties props = context.getBean(SecurityLoggingProperties.class);
                        assertThat(props.isEnabled()).isTrue();
                        assertThat(props.isJsonOutput()).isFalse();
                        assertThat(props.isIncludeStackTrace()).isFalse();
                        assertThat(props.isIncludeClientIp()).isFalse();
                    });
        }

        @Test
        @DisplayName("Should bind masking mode property")
        void testPropertiesBinding_MaskingMode_BindsCorrectly() {
            contextRunner
                    .withPropertyValues("security.logging.masking-mode=FULL")
                    .run(context -> {
                        SecurityLoggingProperties props = context.getBean(SecurityLoggingProperties.class);
                        assertThat(props.getMaskingMode()).isEqualTo(SensitiveDataMasker.MaskingMode.FULL);
                    });
        }

        @Test
        @DisplayName("Should use default masking mode PARTIAL")
        void testPropertiesBinding_DefaultMaskingMode_IsPartial() {
            contextRunner.run(context -> {
                SecurityLoggingProperties props = context.getBean(SecurityLoggingProperties.class);
                assertThat(props.getMaskingMode()).isEqualTo(SensitiveDataMasker.MaskingMode.PARTIAL);
            });
        }
    }

    @Nested
    @DisplayName("Logger configuration tests")
    class LoggerConfigurationTests {

        @Test
        @DisplayName("Should configure logger with JSON output by default")
        void testLoggerConfiguration_DefaultJsonOutput_IsTrue() {
            contextRunner.run(context -> {
                DefaultSecurityLogger logger = (DefaultSecurityLogger) context.getBean(SecurityLogger.class);
                assertThat(logger.isJsonOutput()).isTrue();
            });
        }

        @Test
        @DisplayName("Should configure logger with plain output when disabled")
        void testLoggerConfiguration_PlainOutput_IsFalse() {
            contextRunner
                    .withPropertyValues("security.logging.json-output=false")
                    .run(context -> {
                        DefaultSecurityLogger logger = (DefaultSecurityLogger) context.getBean(SecurityLogger.class);
                        assertThat(logger.isJsonOutput()).isFalse();
                    });
        }

        @Test
        @DisplayName("Should configure logger with stack trace by default")
        void testLoggerConfiguration_DefaultIncludeStackTrace_IsTrue() {
            contextRunner.run(context -> {
                DefaultSecurityLogger logger = (DefaultSecurityLogger) context.getBean(SecurityLogger.class);
                assertThat(logger.isIncludeStackTrace()).isTrue();
            });
        }

        @Test
        @DisplayName("Should configure logger without stack trace when disabled")
        void testLoggerConfiguration_IncludeStackTraceFalse_IsFalse() {
            contextRunner
                    .withPropertyValues("security.logging.include-stack-trace=false")
                    .run(context -> {
                        DefaultSecurityLogger logger = (DefaultSecurityLogger) context.getBean(SecurityLogger.class);
                        assertThat(logger.isIncludeStackTrace()).isFalse();
                    });
        }
    }
}
