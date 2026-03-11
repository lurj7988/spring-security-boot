package com.original.security.health;

import com.original.security.config.HealthCheckProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库健康检查指示器。
 * <p>
 * 检查数据库连接是否正常，通过执行简单的连接验证来确认数据库可用性。
 * 如果数据源未配置，则返回 DOWN 状态。
 *
 * @author bmad
 * @since 0.1.0
 */
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private static final int DEFAULT_TIMEOUT_MS = 2000;

    /**
     * 日志消息 - 数据库连接验证成功。
     */
    private static final String MSG_CONNECTION_VALID = "Database health check: Connection valid";

    /**
     * 日志消息 - 数据库连接验证失败。
     */
    private static final String MSG_CONNECTION_FAILED = "Database health check: Connection validation failed";

    /**
     * 日志消息 - 连接超时。
     */
    private static final String MSG_TIMEOUT = "Database health check: Validation timed out";

    @Nullable
    private final DataSource dataSource;

    private final int timeoutMs;

    /**
     * 创建 DatabaseHealthIndicator 实例。
     *
     * @param dataSource 数据源（可以为 null）
     */
    public DatabaseHealthIndicator(@Nullable DataSource dataSource) {
        this(dataSource, null);
    }

    /**
     * 创建 DatabaseHealthIndicator 实例（带配置）。
     *
     * @param dataSource 数据源（可以为 null）
     * @param properties 健康检查配置属性（可以为 null）
     */
    public DatabaseHealthIndicator(@Nullable DataSource dataSource, @Nullable HealthCheckProperties properties) {
        this.dataSource = dataSource;
        this.timeoutMs = properties != null && properties.getCheckTimeoutMs() > 0
                ? properties.getCheckTimeoutMs()
                : DEFAULT_TIMEOUT_MS;
    }

    @Override
    public Health health() {
        if (dataSource == null) {
            log.debug("Database health check: DataSource not configured");
            return Health.down()
                    .withDetail("error", "DataSource not configured")
                    .build();
        }

        try (Connection connection = dataSource.getConnection()) {
            // 使用同步方式检查连接，避免异步复杂性
            int seconds = Math.max(1, timeoutMs / 1000);
            boolean isValid = connection.isValid(seconds);

            if (isValid) {
                log.debug(MSG_CONNECTION_VALID);
                return Health.up()
                        .withDetail("database", "Connection valid")
                        .build();
            } else {
                log.warn(MSG_CONNECTION_FAILED);
                return Health.down()
                        .withDetail("error", "Connection validation failed")
                        .build();
            }
        } catch (SQLException e) {
            log.warn("Database health check: Connection failed - {}", e.getMessage());
            return Health.down()
                    .withDetail("error", "Connection failed: " + e.getMessage())
                    .build();
        }
    }
}