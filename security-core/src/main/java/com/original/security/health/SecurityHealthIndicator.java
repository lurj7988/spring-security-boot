package com.original.security.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全组件健康检查指示器。
 * <p>
 * 作为安全组件的主要健康检查入口，聚合各个子组件的健康状态：
 * <ul>
 *     <li>database - 数据库连接状态</li>
 *     <li>jwtValidator - JWT 验证器配置状态</li>
 *     <li>cache - 缓存服务状态</li>
 * </ul>
 * <p>
 * 只有当所有组件状态为 UP 时，整体状态才为 UP。
 * 任一组件状态为 DOWN，整体状态即为 DOWN。
 * <p>
 * Spring Boot Actuator 将在 /actuator/health 端点中暴露此健康检查，
 * 路径为 /actuator/health/security。
 *
 * @author bmad
 * @since 0.1.0
 */
public class SecurityHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(SecurityHealthIndicator.class);

    private final DatabaseHealthIndicator databaseHealthIndicator;
    private final JwtValidatorHealthIndicator jwtValidatorHealthIndicator;
    private final CacheHealthIndicator cacheHealthIndicator;

    /**
     * 创建 SecurityHealthIndicator 实例。
     *
     * @param databaseHealthIndicator 数据库健康检查器
     * @param jwtValidatorHealthIndicator JWT 验证器健康检查器
     * @param cacheHealthIndicator 缓存健康检查器
     */
    public SecurityHealthIndicator(
            DatabaseHealthIndicator databaseHealthIndicator,
            JwtValidatorHealthIndicator jwtValidatorHealthIndicator,
            CacheHealthIndicator cacheHealthIndicator) {
        this.databaseHealthIndicator = databaseHealthIndicator;
        this.jwtValidatorHealthIndicator = jwtValidatorHealthIndicator;
        this.cacheHealthIndicator = cacheHealthIndicator;
    }

    @Override
    public Health health() {
        long startTime = System.currentTimeMillis();

        Map<String, Object> details = new HashMap<>();
        Status overallStatus = Status.UP;

        // 检查数据库
        Health databaseHealth = databaseHealthIndicator.health();
        Map<String, Object> dbDetails = new HashMap<>(databaseHealth.getDetails());
        dbDetails.put("status", databaseHealth.getStatus().getCode());
        details.put("database", dbDetails);

        if (databaseHealth.getStatus() == Status.DOWN) {
            overallStatus = Status.DOWN;
        }

        // 检查 JWT 验证器
        Health jwtHealth = jwtValidatorHealthIndicator.health();
        Map<String, Object> jwtDetails = new HashMap<>(jwtHealth.getDetails());
        jwtDetails.put("status", jwtHealth.getStatus().getCode());
        details.put("jwtValidator", jwtDetails);

        if (jwtHealth.getStatus() == Status.DOWN) {
            overallStatus = Status.DOWN;
        }

        // 检查缓存
        Health cacheHealth = cacheHealthIndicator.health();
        Map<String, Object> cacheDetails = new HashMap<>(cacheHealth.getDetails());
        cacheDetails.put("status", cacheHealth.getStatus().getCode());
        details.put("cache", cacheDetails);

        if (cacheHealth.getStatus() == Status.DOWN) {
            overallStatus = Status.DOWN;
        }

        long executionTime = System.currentTimeMillis() - startTime;
        details.put("checkTimeMs", executionTime);

        log.info("Security health check completed in {}ms with status: {}", executionTime, overallStatus);

        Health.Builder builder = overallStatus == Status.UP ? Health.up() : Health.down();
        return builder.withDetails(details).build();
    }
}
