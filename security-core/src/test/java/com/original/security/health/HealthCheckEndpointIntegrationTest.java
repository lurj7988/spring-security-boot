package com.original.security.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 健康检查端点集成测试。
 *
 * @author bmad
 * @since 0.1.0
 */
class HealthCheckEndpointIntegrationTest {

    @Test
    @DisplayName("integration_SecurityHealthIndicator完整流程")
    void integration_SecurityHealthIndicator_FullFlow() {
        // Given - 创建所有依赖的健康指示器
        DatabaseHealthIndicator dbIndicator = new DatabaseHealthIndicator(null);
        JwtValidatorHealthIndicator jwtIndicator = new JwtValidatorHealthIndicator(null);
        CacheHealthIndicator cacheIndicator = new CacheHealthIndicator(null);
        SecurityHealthIndicator securityIndicator = new SecurityHealthIndicator(
                dbIndicator, jwtIndicator, cacheIndicator);

        // When - 执行健康检查
        Health health = securityIndicator.health();

        // Then - 验证响应结构
        assertNotNull(health, "Health should not be null");
        assertEquals(Status.DOWN, health.getStatus(), "Status should be DOWN when no components configured");

        // 验证响应包含所有组件
        assertTrue(health.getDetails().containsKey("database"), "Should contain database");
        assertTrue(health.getDetails().containsKey("jwtValidator"), "Should contain jwtValidator");
        assertTrue(health.getDetails().containsKey("cache"), "Should contain cache");
        assertTrue(health.getDetails().containsKey("checkTimeMs"), "Should contain checkTimeMs");
    }

    @Test
    @DisplayName("integration_健康检查执行时间验证")
    void integration_HealthCheckExecutionTime_Validation() {
        // Given
        DatabaseHealthIndicator dbIndicator = new DatabaseHealthIndicator(null);
        JwtValidatorHealthIndicator jwtIndicator = new JwtValidatorHealthIndicator(null);
        CacheHealthIndicator cacheIndicator = new CacheHealthIndicator(null);
        SecurityHealthIndicator securityIndicator = new SecurityHealthIndicator(
                dbIndicator, jwtIndicator, cacheIndicator);

        // When - 多次执行健康检查
        long totalTime = 0;
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            securityIndicator.health();
            totalTime += System.currentTimeMillis() - start;
        }

        // Then - 平均执行时间应该非常快
        long avgTime = totalTime / iterations;
        assertTrue(avgTime < 50, "Average health check time should be < 50ms, was: " + avgTime + "ms");
    }

    @Test
    @DisplayName("integration_各组件状态独立检查")
    void integration_EachComponent_IndependentCheck() {
        // 测试数据库健康检查
        DatabaseHealthIndicator dbIndicator = new DatabaseHealthIndicator(null);
        Health dbHealth = dbIndicator.health();
        assertEquals(Status.DOWN, dbHealth.getStatus(), "Database should be DOWN when not configured");
        assertTrue(dbHealth.getDetails().containsKey("error"), "Should contain error detail");

        // 测试 JWT 健康检查
        JwtValidatorHealthIndicator jwtIndicator = new JwtValidatorHealthIndicator(null);
        Health jwtHealth = jwtIndicator.health();
        assertEquals(Status.DOWN, jwtHealth.getStatus(), "JWT should be DOWN when not configured");
        assertTrue(jwtHealth.getDetails().containsKey("error"), "Should contain error detail");

        // 测试缓存健康检查
        CacheHealthIndicator cacheIndicator = new CacheHealthIndicator(null);
        Health cacheHealth = cacheIndicator.health();
        assertEquals(Status.UP, cacheHealth.getStatus(), "Cache should be UP (optional component)");
        assertEquals("not_configured", cacheHealth.getDetails().get("status"), "Should show not_configured status");
    }
}
