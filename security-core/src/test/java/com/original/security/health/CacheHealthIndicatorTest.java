package com.original.security.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CacheHealthIndicator 单元测试。
 *
 * @author bmad
 * @since 0.1.0
 */
class CacheHealthIndicatorTest {

    private CacheManager cacheManager;
    private Cache cache;
    private CacheHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        healthIndicator = new CacheHealthIndicator(cacheManager);
    }

    @Test
    @DisplayName("health_缓存服务正常_返回UP状态")
    void health_CacheAvailable_ReturnsUpStatus() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(java.util.Collections.singletonList("testCache"));
        when(cacheManager.getCache("testCache")).thenReturn(cache);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("status"));
    }

    @Test
    @DisplayName("health_缓存管理器为null_返回UP状态(缓存为可选)")
    void health_CacheManagerNull_ReturnsUpStatus() {
        // Given - 缓存是可选组件，没有缓存管理器时应该是 UP
        CacheHealthIndicator indicatorWithNullCacheManager = new CacheHealthIndicator(null);

        // When
        Health health = indicatorWithNullCacheManager.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("status"));
        assertEquals("not_configured", health.getDetails().get("status"));
    }

    @Test
    @DisplayName("health_缓存为空_返回UP状态")
    void health_NoCaches_ReturnsUpStatus() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(java.util.Collections.emptyList());

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("cacheCount"));
        assertEquals(0, health.getDetails().get("cacheCount"));
    }

    @Test
    @DisplayName("health_缓存不可用_返回DOWN状态")
    void health_CacheUnavailable_ReturnsDownStatus() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(java.util.Collections.singletonList("testCache"));
        when(cacheManager.getCache("testCache")).thenReturn(null);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_检查执行时间_小于50毫秒")
    void health_CheckExecutionTime_LessThan50Ms() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(java.util.Collections.singletonList("testCache"));
        when(cacheManager.getCache("testCache")).thenReturn(cache);

        // When
        long startTime = System.currentTimeMillis();
        healthIndicator.health();
        long executionTime = System.currentTimeMillis() - startTime;

        // Then
        assertTrue(executionTime < 50, "Cache health check should complete within 50ms, took: " + executionTime + "ms");
    }

    @Test
    @DisplayName("health_包含缓存数量信息")
    void health_ContainsCacheCount() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(java.util.Arrays.asList("cache1", "cache2", "cache3"));
        when(cacheManager.getCache("cache1")).thenReturn(cache);
        when(cacheManager.getCache("cache2")).thenReturn(cache);
        when(cacheManager.getCache("cache3")).thenReturn(cache);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals(3, health.getDetails().get("cacheCount"));
    }
}
