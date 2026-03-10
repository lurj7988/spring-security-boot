package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 安全 Metrics 配置属性。
 * <p>
 * 可通过 {@code application.yml} 或 {@code application.properties} 配置：
 * <pre>
 * security:
 *   metrics:
 *     enabled: true                              # 是否启用安全 Metrics (默认: true)
 *     authentication-success-enabled: true          # 是否记录认证成功 (默认: true)
 *     authentication-failure-enabled: true          # 是否记录认证失败 (默认: true)
 *     authentication-duration-enabled: true         # 是否记录认证耗时 (默认: true)
 *     duration-percentiles: 0.5,0.95,0.99       # 百分位数配置，范围 0.0-1.0 (默认: 0.5,0.95,0.99)
 *     auth-paths: /api/auth/login,/login          # 认证路径列表 (默认: /api/auth/login,/login)
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.metrics")
public class SecurityMetricsProperties {

    /**
     * 是否启用安全 Metrics。
     */
    private boolean enabled = true;

    /**
     * 是否记录认证成功事件。
     */
    private boolean authenticationSuccessEnabled = true;

    /**
     * 是否记录认证失败事件。
     */
    private boolean authenticationFailureEnabled = true;

    /**
     * 是否记录认证耗时。
     */
    private boolean authenticationDurationEnabled = true;

    /**
     * 认证耗时 Timer 的百分位数配置。
     * <p>
     * 默认值：[0.5, 0.95, 0.99]，表示计算 P50、P95、P99
     * 可以通过配置文件自定义，例如：security.metrics.duration-percentiles=0.5,0.9,0.95,0.99
     * 每个百分位数必须在 0.0 到 1.0 之间
     */
    private List<Double> durationPercentiles = Arrays.asList(0.5, 0.95, 0.99);

    /**
     * 认证路径列表。
     * <p>
     * 只有匹配这些路径的请求才会记录认证耗时 Metrics。
     * 默认值：["/api/auth/login", "/login"]
     * 可以通过配置文件自定义，例如：security.metrics.auth-paths=/api/auth/login,/api/v1/auth,/login
     */
    private List<String> authPaths = Arrays.asList("/api/auth/login", "/login");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAuthenticationSuccessEnabled() {
        return authenticationSuccessEnabled;
    }

    public void setAuthenticationSuccessEnabled(boolean authenticationSuccessEnabled) {
        this.authenticationSuccessEnabled = authenticationSuccessEnabled;
    }

    public boolean isAuthenticationFailureEnabled() {
        return authenticationFailureEnabled;
    }

    public void setAuthenticationFailureEnabled(boolean authenticationFailureEnabled) {
        this.authenticationFailureEnabled = authenticationFailureEnabled;
    }

    public boolean isAuthenticationDurationEnabled() {
        return authenticationDurationEnabled;
    }

    public void setAuthenticationDurationEnabled(boolean authenticationDurationEnabled) {
        this.authenticationDurationEnabled = authenticationDurationEnabled;
    }

    public List<Double> getDurationPercentiles() {
        if (durationPercentiles == null || durationPercentiles.isEmpty()) {
            return Collections.singletonList(0.5);
        }
        // 防御性拷贝，确保不可变
        return new ArrayList<>(durationPercentiles);
    }

    public void setDurationPercentiles(List<Double> durationPercentiles) {
        // 验证百分位数范围
        if (durationPercentiles != null) {
            List<Double> validatedPercentiles = new ArrayList<>();
            for (Double percentile : durationPercentiles) {
                if (percentile != null && percentile >= 0.0 && percentile <= 1.0) {
                    validatedPercentiles.add(percentile);
                }
                // 无效的百分位数被静默忽略，避免日志污染
            }
            if (!validatedPercentiles.isEmpty()) {
                this.durationPercentiles = validatedPercentiles;
            } else {
                // 如果没有有效的百分位数，使用默认值
                this.durationPercentiles = Arrays.asList(0.5, 0.95, 0.99);
            }
        } else {
            // 如果为 null，使用默认值
            this.durationPercentiles = Arrays.asList(0.5, 0.95, 0.99);
        }
    }

    public List<String> getAuthPaths() {
        if (authPaths == null || authPaths.isEmpty()) {
            return Collections.unmodifiableList(Arrays.asList("/api/auth/login", "/login"));
        }
        // 防御性拷贝，确保不可变
        return Collections.unmodifiableList(new ArrayList<>(authPaths));
    }

    public void setAuthPaths(List<String> authPaths) {
        if (authPaths != null && !authPaths.isEmpty()) {
            // 过滤空字符串并去除前后空格
            List<String> filteredPaths = new ArrayList<>();
            for (String path : authPaths) {
                if (path != null && !path.trim().isEmpty()) {
                    String trimmedPath = path.trim();
                    // 确保路径以 / 开头
                    if (!trimmedPath.startsWith("/")) {
                        trimmedPath = "/" + trimmedPath;
                    }
                    filteredPaths.add(trimmedPath);
                }
            }
            if (!filteredPaths.isEmpty()) {
                this.authPaths = filteredPaths;
            }
        }
    }
}
