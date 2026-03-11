package com.original.security.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SecurityHealthIndicator 单元测试。
 *
 * @author bmad
 * @since 0.1.0
 */
class SecurityHealthIndicatorTest {

    private DatabaseHealthIndicator databaseHealthIndicator;
    private JwtValidatorHealthIndicator jwtValidatorHealthIndicator;
    private CacheHealthIndicator cacheHealthIndicator;
    private SecurityHealthIndicator securityHealthIndicator;

    @BeforeEach
    void setUp() {
        databaseHealthIndicator = mock(DatabaseHealthIndicator.class);
        jwtValidatorHealthIndicator = mock(JwtValidatorHealthIndicator.class);
        cacheHealthIndicator = mock(CacheHealthIndicator.class);

        securityHealthIndicator = new SecurityHealthIndicator(
                databaseHealthIndicator,
                jwtValidatorHealthIndicator,
                cacheHealthIndicator
        );
    }

    @Test
    @DisplayName("health_所有组件正常_返回UP状态")
    void health_AllComponentsUp_ReturnsUpStatus() {
        // Given
        when(databaseHealthIndicator.health()).thenReturn(Health.up().withDetail("connection", "valid").build());
        when(jwtValidatorHealthIndicator.health()).thenReturn(Health.up().withDetail("configured", true).build());
        when(cacheHealthIndicator.health()).thenReturn(Health.up().withDetail("status", "available").build());

        // When
        Health health = securityHealthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        Map<String, Object> details = health.getDetails();
        assertTrue(details.containsKey("database"));
        assertTrue(details.containsKey("jwtValidator"));
        assertTrue(details.containsKey("cache"));
    }

    @Test
    @DisplayName("health_数据库连接失败_返回DOWN状态")
    void health_DatabaseDown_ReturnsDownStatus() {
        // Given
        when(databaseHealthIndicator.health())
                .thenReturn(Health.down().withDetail("error", "Connection refused").build());
        when(jwtValidatorHealthIndicator.health())
                .thenReturn(Health.up().withDetail("configured", true).build());
        when(cacheHealthIndicator.health())
                .thenReturn(Health.up().withDetail("status", "available").build());

        // When
        Health health = securityHealthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        Map<String, Object> details = health.getDetails();
        assertTrue(details.containsKey("database"));
    }

    @Test
    @DisplayName("health_JWT验证器配置错误_返回DOWN状态")
    void health_JwtValidatorDown_ReturnsDownStatus() {
        // Given
        when(databaseHealthIndicator.health())
                .thenReturn(Health.up().withDetail("connection", "valid").build());
        when(jwtValidatorHealthIndicator.health())
                .thenReturn(Health.down().withDetail("error", "Invalid secret configuration").build());
        when(cacheHealthIndicator.health())
                .thenReturn(Health.up().withDetail("status", "available").build());

        // When
        Health health = securityHealthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        Map<String, Object> details = health.getDetails();
        assertTrue(details.containsKey("jwtValidator"));
    }

    @Test
    @DisplayName("health_缓存服务不可用_返回DOWN状态")
    void health_CacheDown_ReturnsDownStatus() {
        // Given
        when(databaseHealthIndicator.health())
                .thenReturn(Health.up().withDetail("connection", "valid").build());
        when(jwtValidatorHealthIndicator.health())
                .thenReturn(Health.up().withDetail("configured", true).build());
        when(cacheHealthIndicator.health())
                .thenReturn(Health.down().withDetail("error", "Cache service unavailable").build());

        // When
        Health health = securityHealthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        Map<String, Object> details = health.getDetails();
        assertTrue(details.containsKey("cache"));
    }

    @Test
    @DisplayName("health_检查执行时间_小于50毫秒")
    void health_CheckExecutionTime_LessThan50Ms() {
        // Given
        when(databaseHealthIndicator.health())
                .thenReturn(Health.up().withDetail("connection", "valid").build());
        when(jwtValidatorHealthIndicator.health())
                .thenReturn(Health.up().withDetail("configured", true).build());
        when(cacheHealthIndicator.health())
                .thenReturn(Health.up().withDetail("status", "available").build());

        // When
        long startTime = System.currentTimeMillis();
        Health health = securityHealthIndicator.health();
        long executionTime = System.currentTimeMillis() - startTime;

        // Then
        assertTrue(executionTime < 50, "Health check should complete within 50ms, took: " + executionTime + "ms");
    }

    @Test
    @DisplayName("health_包含所有组件状态详情")
    void health_ContainsAllComponentDetails() {
        // Given
        when(databaseHealthIndicator.health())
                .thenReturn(Health.up().withDetail("connection", "valid").build());
        when(jwtValidatorHealthIndicator.health())
                .thenReturn(Health.up().withDetail("configured", true).build());
        when(cacheHealthIndicator.health())
                .thenReturn(Health.up().withDetail("status", "available").build());

        // When
        Health health = securityHealthIndicator.health();

        // Then
        Map<String, Object> details = health.getDetails();
        @SuppressWarnings("unchecked")
        Map<String, Object> dbDetails = (Map<String, Object>) details.get("database");
        @SuppressWarnings("unchecked")
        Map<String, Object> jwtDetails = (Map<String, Object>) details.get("jwtValidator");
        @SuppressWarnings("unchecked")
        Map<String, Object> cacheDetails = (Map<String, Object>) details.get("cache");

        assertNotNull(dbDetails);
        assertNotNull(jwtDetails);
        assertNotNull(cacheDetails);
    }
}
