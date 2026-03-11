package com.original.security.health;

import com.original.security.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtValidatorHealthIndicator 单元测试。
 *
 * @author bmad
 * @since 0.1.0
 */
class JwtValidatorHealthIndicatorTest {

    private JwtProperties jwtProperties;
    private JwtValidatorHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        jwtProperties = mock(JwtProperties.class);
        healthIndicator = new JwtValidatorHealthIndicator(jwtProperties);
    }

    @Test
    @DisplayName("health_JWT配置正确_返回UP状态")
    void health_JwtConfigured_ReturnsUpStatus() {
        // Given - 有效的 Base64 编码密钥 (至少 256 bits)
        String validSecret = "dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdmFsaWRhdGlvbi1wdXJwb3Nl";
        when(jwtProperties.getSecret()).thenReturn(validSecret);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("configured"));
        assertEquals(true, health.getDetails().get("configured"));
    }

    @Test
    @DisplayName("health_JWT密钥为空_返回DOWN状态")
    void health_JwtSecretEmpty_ReturnsDownStatus() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(null);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_JWT密钥格式错误_返回DOWN状态")
    void health_JwtSecretInvalidFormat_ReturnsDownStatus() {
        // Given - 无效的 Base64 字符串
        when(jwtProperties.getSecret()).thenReturn("not-valid-base64!!!");

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_JWT密钥太短_返回DOWN状态")
    void health_JwtSecretTooShort_ReturnsDownStatus() {
        // Given - 太短的密钥（少于 256 bits）
        when(jwtProperties.getSecret()).thenReturn("dG9vLXNob3J0");

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_JWT配置为null_返回DOWN状态")
    void health_JwtPropertiesNull_ReturnsDownStatus() {
        // Given
        JwtValidatorHealthIndicator indicatorWithNullProperties = new JwtValidatorHealthIndicator(null);

        // When
        Health health = indicatorWithNullProperties.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_检查执行时间_小于50毫秒")
    void health_CheckExecutionTime_LessThan50Ms() {
        // Given
        String validSecret = "dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdmFsaWRhdGlvbi1wdXJwb3Nl";
        when(jwtProperties.getSecret()).thenReturn(validSecret);

        // When
        long startTime = System.currentTimeMillis();
        healthIndicator.health();
        long executionTime = System.currentTimeMillis() - startTime;

        // Then
        assertTrue(executionTime < 50, "JWT health check should complete within 50ms, took: " + executionTime + "ms");
    }
}
