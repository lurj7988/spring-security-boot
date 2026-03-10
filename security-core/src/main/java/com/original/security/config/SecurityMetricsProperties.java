package com.original.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 安全 Metrics 配置属性。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.metrics")
public class SecurityMetricsProperties {

    private static final Logger log = LoggerFactory.getLogger(SecurityMetricsProperties.class);

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
                } else {
                    log.warn("Invalid percentile value {} ignored, must be between 0.0 and 1.0", percentile);
                }
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
}
