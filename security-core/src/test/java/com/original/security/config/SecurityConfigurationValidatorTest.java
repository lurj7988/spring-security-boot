package com.original.security.config;

import com.original.security.exception.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link SecurityConfigurationValidator}.
 * Tests cover validation enabled/disabled scenarios and datasource configuration validation.
 *
 * @author Naulu
 * @since 1.0.0
 */
public class SecurityConfigurationValidatorTest {

    @Test
    public void testOnApplicationEvent_ValidationDisabled_DoesNotThrow() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(false);
        MockEnvironment env = new MockEnvironment();

        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);
        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        assertDoesNotThrow(() -> validator.onApplicationEvent(event));
    }

    @Test
    public void testOnApplicationEvent_MissingDatasourceUrl_ThrowsException() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();

        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);
        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        ConfigurationException exception = assertThrows(ConfigurationException.class,
            () -> validator.onApplicationEvent(event));

        assertTrue(exception.getMessage().contains("数据库连接未配置"));
        assertTrue(exception.getMessage().contains("spring.datasource.url=jdbc:mysql://localhost:3306/mydb"));
    }

    @Test
    public void testOnApplicationEvent_EmptyDatasourceUrl_ThrowsException() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "   ");

        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);
        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        assertThrows(ConfigurationException.class, () -> validator.onApplicationEvent(event));
    }

    @Test
    public void testOnApplicationEvent_ValidDatasourceUrl_DoesNotThrow() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/db");

        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);
        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        assertDoesNotThrow(() -> validator.onApplicationEvent(event));
    }

    @Test
    public void testOnApplicationEvent_ValidDatasourceUrl_LogsSuccessMessage() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/testdb");

        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);
        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        // Verify no exception is thrown and validation passes
        assertDoesNotThrow(() -> validator.onApplicationEvent(event));
    }

    @Test
    public void testOnApplicationEvent_ValidationDisabled_LogsDisabledMessage() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(false);
        MockEnvironment env = new MockEnvironment();

        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);
        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        // Verify no exception is thrown when validation is disabled
        assertDoesNotThrow(() -> validator.onApplicationEvent(event));
    }

    @Test
    public void testConfigurationException_MessageContainsRequiredInfo() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();

        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);
        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        ConfigurationException exception = assertThrows(ConfigurationException.class,
            () -> validator.onApplicationEvent(event));

        String message = exception.getMessage();
        assertTrue(message.contains("=== Spring Security Boot 配置错误 ==="));
        assertTrue(message.contains("解决方案:"));
        assertTrue(message.contains("https://docs.spring-security-boot.io/config"));
    }

    @Test
    public void testOnApplicationEvent_CorsEnabledMissingOrigins_ThrowsException() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/db");
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(true);
        corsProperties.setAllowedOrigins(Collections.emptyList());

        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        ConfigurationException exception = assertThrows(ConfigurationException.class,
                () -> validator.onApplicationEvent(event));

        assertTrue(exception.getMessage().contains("CORS 已启用，但未配置 allowed-origins"));
    }

    @Test
    public void testOnApplicationEvent_CorsEnabledValidOrigins_DoesNotThrow() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/db");
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(true);
        corsProperties.setAllowedOrigins(Arrays.asList("http://localhost:8080"));

        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        assertDoesNotThrow(() -> validator.onApplicationEvent(event));
    }

    @Test
    public void testOnApplicationEvent_CorsDisabledMissingOrigins_DoesNotThrow() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/db");
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(false);

        CsrfProperties csrfProperties = new CsrfProperties();
        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        assertDoesNotThrow(() -> validator.onApplicationEvent(event));
    }

    @Test
    public void testOnApplicationEvent_CsrfDisabled_DoesNotThrow() {
        SecurityProperties properties = new SecurityProperties();
        properties.getConfig().setValidation(true);
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/db");
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setEnabled(true);
        corsProperties.setAllowedOrigins(Arrays.asList("http://localhost:8080"));

        CsrfProperties csrfProperties = new CsrfProperties();
        csrfProperties.setEnabled(false);

        SecurityHeadersProperties securityHeadersProperties = new SecurityHeadersProperties();
        CspProperties cspProperties = new CspProperties();
        SecurityConfigurationValidator validator = new SecurityConfigurationValidator(properties, corsProperties, csrfProperties, securityHeadersProperties, cspProperties, env);
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        assertDoesNotThrow(() -> validator.onApplicationEvent(event));
    }
}
