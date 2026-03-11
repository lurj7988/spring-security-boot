package com.original.security.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * 缓存健康检查指示器。
 * <p>
 * 检查缓存服务状态，验证缓存管理器和缓存是否可用。
 * 缓存是可选组件，如果没有配置缓存管理器，则返回 UP 状态并标记为未配置。
 *
 * @author bmad
 * @since 0.1.0
 */
public class CacheHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(CacheHealthIndicator.class);

    @Nullable
    private final CacheManager cacheManager;

    /**
     * 创建 CacheHealthIndicator 实例。
     *
     * @param cacheManager 缓存管理器（可以为 null）
     */
    public CacheHealthIndicator(@Nullable CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Health health() {
        if (cacheManager == null) {
            log.debug("Cache health check: CacheManager not configured");
            return Health.up()
                    .withDetail("status", "not_configured")
                    .withDetail("cacheCount", 0)
                    .build();
        }

        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            if (cacheNames.isEmpty()) {
                log.debug("Cache health check: No caches configured");
                return Health.up()
                        .withDetail("status", "no_caches")
                        .withDetail("cacheCount", 0)
                        .build();
            }

            int availableCaches = 0;
            int unavailableCaches = 0;

            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    try {
                        // Perform a minimal check to verify the cache is actually reachable
                        cache.get("spring-security-health-check-ping");
                        availableCaches++;
                    } catch (Exception e) {
                        unavailableCaches++;
                        log.warn("Cache health check: Cache '{}' is not reachable: {}", cacheName, e.getMessage());
                    }
                } else {
                    unavailableCaches++;
                    log.warn("Cache health check: Cache '{}' is not available", cacheName);
                }
            }

            if (unavailableCaches > 0) {
                log.warn("Cache health check: {} cache(s) unavailable out of {}", unavailableCaches, cacheNames.size());
                return Health.down()
                        .withDetail("status", "degraded")
                        .withDetail("cacheCount", cacheNames.size())
                        .withDetail("availableCaches", availableCaches)
                        .withDetail("unavailableCaches", unavailableCaches)
                        .withDetail("error", "Some caches are unavailable")
                        .build();
            }

            log.debug("Cache health check: All {} cache(s) available", availableCaches);
            return Health.up()
                    .withDetail("status", "available")
                    .withDetail("cacheCount", cacheNames.size())
                    .build();

        } catch (Exception e) {
            log.warn("Cache health check failed: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("status", "error")
                    .withDetail("error", "Failed to communicate with cache manager: " + e.getMessage())
                    .build();
        }
    }
}
