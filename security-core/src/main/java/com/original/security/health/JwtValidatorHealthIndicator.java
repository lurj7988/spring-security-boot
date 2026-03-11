package com.original.security.health;

import com.original.security.config.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.security.Key;

/**
 * JWT 验证器健康检查指示器。
 * <p>
 * 检查 JWT 配置是否正确，验证密钥是否有效且满足安全要求。
 * 如果 JWT 配置未设置或密钥无效，则返回 DOWN 状态。
 *
 * @author bmad
 * @since 0.1.0
 */
public class JwtValidatorHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(JwtValidatorHealthIndicator.class);

    /**
     * JWT 密钥最小长度（位数）。
     */
    private static final int MIN_KEY_LENGTH_BITS = 256;

    @Nullable
    private final JwtProperties jwtProperties;
    
    private final Object healthLock = new Object();
    private Health cachedHealth;

    /**
     * 创建 JwtValidatorHealthIndicator 实例。
     *
     * @param jwtProperties JWT 配置属性（可以为 null）
     */
    public JwtValidatorHealthIndicator(@Nullable JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Health health() {
        // 使用双重检查锁定模式确保线程安全
        if (cachedHealth == null) {
            synchronized (healthLock) {
                if (cachedHealth == null) {
                    cachedHealth = buildHealth();
                }
            }
        }
        return cachedHealth;
    }

    /**
     * 构建健康检查结果（提取为单独方法便于测试）。
     */
    private Health buildHealth() {

        if (jwtProperties == null) {
            log.debug("JWT health check: JwtProperties not configured");
            return Health.down()
                    .withDetail("error", "JWT properties not configured")
                    .build();
        }

        String secret = jwtProperties.getSecret();
        if (!StringUtils.hasText(secret)) {
            log.debug("JWT health check: Secret not configured");
            return Health.down()
                    .withDetail("error", "JWT secret is not configured. Please set 'security.jwt.secret' property.")
                    .build();
        }

        try {
            // 尝试解码和验证密钥
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            Key key = Keys.hmacShaKeyFor(keyBytes);

            // 验证密钥长度
            if (keyBytes.length * 8 < MIN_KEY_LENGTH_BITS) {
                log.warn("JWT health check: Secret key too short ({} bits, minimum {} bits)",
                        keyBytes.length * 8, MIN_KEY_LENGTH_BITS);
                return Health.down()
                        .withDetail("error", "JWT secret key too short. Minimum " + MIN_KEY_LENGTH_BITS + " bits required.")
                        .build();
            }

            log.debug("JWT health check: Configuration valid");
            return Health.up()
                    .withDetail("configured", true)
                    .withDetail("algorithm", key.getAlgorithm())
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("JWT health check: Invalid secret format - {}", e.getMessage());
            return Health.down()
                    .withDetail("error", "Invalid JWT secret format: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.warn("JWT health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", "JWT validation failed: " + e.getMessage())
                    .build();
        }
    }
}
