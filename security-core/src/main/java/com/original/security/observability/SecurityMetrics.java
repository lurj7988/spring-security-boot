package com.original.security.observability;

import com.original.security.config.SecurityMetricsProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 安全 Metrics 注册表。
 * <p>
 * 提供认证成功/失败计数器和认证耗时 Timer。
 * 支持按 authentication_type 和 failure_reason 标签分类。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SecurityMetrics {

    private static final Logger log = LoggerFactory.getLogger(SecurityMetrics.class);

    private static final String METRIC_PREFIX = "security.authentication";
    private static final String TAG_AUTH_TYPE = "authentication_type";
    private static final String TAG_FAILURE_REASON = "failure_reason";
    private static final String DEFAULT_AUTH_TYPE = "unknown";
    private static final String DEFAULT_FAILURE_REASON = "unknown";

    private final MeterRegistry meterRegistry;
    private final SecurityMetricsProperties properties;

    /**
     * 创建 SecurityMetrics 实例。
     *
     * @param meterRegistry Micrometer MeterRegistry，可以为 null（此时 metrics 功能禁用）
     * @param properties 安全 Metrics 配置属性
     */
    public SecurityMetrics(@Nullable MeterRegistry meterRegistry, SecurityMetricsProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
        if (meterRegistry != null) {
            log.info("SecurityMetrics initialized with metrics: {}.success, {}.failure, {}.duration",
                    METRIC_PREFIX, METRIC_PREFIX, METRIC_PREFIX);
            log.info("Duration percentiles configured: {}", properties.getDurationPercentiles());
        } else {
            log.warn("SecurityMetrics initialized without MeterRegistry - metrics recording disabled");
        }
    }

    /**
     * 记录认证成功。
     *
     * @param authenticationType 认证类型（如 username-password, jwt, oauth2）
     */
    public void recordAuthenticationSuccess(String authenticationType) {
        if (meterRegistry == null) {
            return;
        }
        String authType = sanitizeTag(authenticationType, DEFAULT_AUTH_TYPE);
        Counter.builder(METRIC_PREFIX + ".success")
                .description("Number of successful authentications")
                .tag(TAG_AUTH_TYPE, authType)
                .register(meterRegistry)
                .increment();
        log.debug("Recorded authentication success for type: {}", authType);
    }

    /**
     * 记录认证失败。
     *
     * @param authenticationType 认证类型
     * @param failureReason      失败原因（异常类名）
     */
    public void recordAuthenticationFailure(String authenticationType, String failureReason) {
        if (meterRegistry == null) {
            return;
        }
        String authType = sanitizeTag(authenticationType, DEFAULT_AUTH_TYPE);
        String reason = sanitizeTag(failureReason, DEFAULT_FAILURE_REASON);
        Counter.builder(METRIC_PREFIX + ".failure")
                .description("Number of failed authentications")
                .tag(TAG_AUTH_TYPE, authType)
                .tag(TAG_FAILURE_REASON, reason)
                .register(meterRegistry)
                .increment();
        log.debug("Recorded authentication failure for type: {}, reason: {}", authType, reason);
    }

    /**
     * 记录认证耗时。
     *
     * @param authenticationType 认证类型
     * @param durationNanos      耗时（纳秒）
     */
    public void recordAuthenticationDuration(String authenticationType, long durationNanos) {
        if (meterRegistry == null) {
            return;
        }
        String authType = sanitizeTag(authenticationType, DEFAULT_AUTH_TYPE);
        Timer.builder(METRIC_PREFIX + ".duration")
                .description("Authentication duration in milliseconds")
                .tag(TAG_AUTH_TYPE, authType)
                .publishPercentiles(getPercentileValues())
                .register(meterRegistry)
                .record(durationNanos, TimeUnit.NANOSECONDS);
        log.debug("Recorded authentication duration for type: {}: {}ms",
                authType, TimeUnit.NANOSECONDS.toMillis(durationNanos));
    }

    /**
     * 记录认证耗时。
     *
     * @param authenticationType 认证类型
     * @param duration           耗时
     * @param timeUnit           时间单位
     */
    public void recordAuthenticationDuration(String authenticationType, long duration, TimeUnit timeUnit) {
        if (meterRegistry == null) {
            return;
        }
        String authType = sanitizeTag(authenticationType, DEFAULT_AUTH_TYPE);
        Timer.builder(METRIC_PREFIX + ".duration")
                .description("Authentication duration in milliseconds")
                .tag(TAG_AUTH_TYPE, authType)
                .publishPercentiles(getPercentileValues())
                .register(meterRegistry)
                .record(duration, timeUnit);
        log.debug("Recorded authentication duration for type: {}: {} {}",
                authType, duration, timeUnit);
    }

    /**
     * 获取百分位数配置值。
     * <p>
     * 返回配置的百分位数，如果没有配置则使用默认值。
     * 百分位数必须在 0.0 到 1.0 之间。
     *
     * @return 百分位数数组
     */
    private double[] getPercentileValues() {
        List<Double> percentiles = properties.getDurationPercentiles();
        double[] values = new double[percentiles.size()];
        for (int i = 0; i < percentiles.size(); i++) {
            double percentile = percentiles.get(i);
            // 验证百分位数范围
            if (percentile < 0.0 || percentile > 1.0) {
                log.warn("Invalid percentile value {} detected, using default 0.95", percentile);
                values[i] = 0.95; // 使用默认值
            } else {
                values[i] = percentile;
            }
        }
        return values;
    }

    /**
     * 检查 Metrics 是否可用。
     *
     * @return 如果 MeterRegistry 已配置则返回 true
     */
    public boolean isMetricsEnabled() {
        return meterRegistry != null;
    }

    /**
     * 清理标签值，确保不为 null、空，符合标签格式要求。
     * <p>
     * 标签值规则：
     * - 长度限制：1-255 字符
     * - 只允许字母、数字、下划线、短横线、点和冒号
     * - 不能以点开头或结尾
     *
     * @param value 原始标签值
     * @param defaultValue 默认值（当 value 无效时使用）
     * @return 清理后的有效标签值
     */
    private String sanitizeTag(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        String trimmed = value.trim();

        // 检查长度限制
        if (trimmed.length() > 255) {
            log.warn("Tag value '{}' exceeds length limit (255 chars), truncating to: {}",
                    value, trimmed.substring(0, 255));
            trimmed = trimmed.substring(0, 255);
        }

        // 验证字符集和格式
        if (!isValidTagFormat(trimmed)) {
            log.warn("Invalid tag format '{}', using default: {}", value, defaultValue);
            return defaultValue;
        }

        return trimmed;
    }

    /**
     * 验证标签格式是否有效。
     * <p>
     * 有效格式：
     * - 只包含字母、数字、下划线(_)、短横线(-)、点(.)和冒号(:)
     * - 不能以点开头或结尾
     * - 长度 1-255 字符
     *
     * @param value 标签值
     * @return 如果格式有效返回 true
     */
    private boolean isValidTagFormat(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        // 检查长度限制
        if (value.length() > 255 || value.length() < 1) {
            return false;
        }

        // 检查开头和结尾不能是点
        if (value.startsWith(".") || value.endsWith(".")) {
            return false;
        }

        // 检查字符集：只允许字母、数字、下划线、短横线、点和冒号
        // 使用更严格的正则表达式，确保每个字符都符合要求
        return value.matches("^[a-zA-Z0-9_\\-:.]+$");
    }
}
