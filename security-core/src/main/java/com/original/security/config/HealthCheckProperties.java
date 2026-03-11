package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 健康检查配置属性。
 * <p>
 * 可通过 {@code application.yml} 或 {@code application.properties} 配置：
 * <pre>
 * security:
 *   health:
 *     enabled: true                    # 是否启用健康检查 (默认: true)
 *     check-timeout-ms: 5000           # 健康检查超时时间（毫秒）(默认: 5000)
 * </pre>
 *
 * @author bmad
 * @since 0.1.0
 */
@ConfigurationProperties(prefix = "security.health")
public class HealthCheckProperties {

    /**
     * 是否启用安全组件健康检查。
     */
    private boolean enabled = true;

    /**
     * 健康检查超时时间（毫秒）。
     */
    private int checkTimeoutMs = 5000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCheckTimeoutMs() {
        return checkTimeoutMs;
    }

    public void setCheckTimeoutMs(int checkTimeoutMs) {
        this.checkTimeoutMs = checkTimeoutMs;
    }
}
