package com.original.security.tracing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.original.security.tracing.TracingConstants.*;

/**
 * 分布式追踪配置属性。
 * <p>
 * 配置前缀: security.tracing
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.tracing")
public class SecurityTracingProperties {

    /**
     * 是否启用分布式追踪。
     * 默认值: true
     */
    private boolean enabled = DEFAULT_TRACING_ENABLED;

    /**
     * 用户名脱敏长度。
     * 保留前 N 个字符，其余用 * 替换。
     * 默认值: 3
     */
    private int usernameMaskLength = DEFAULT_USERNAME_MASK_LENGTH;

    /**
     * Token 脱敏长度。
     * 保留前 N 个字符，其余用 * 替换。
     * 默认值: 8
     */
    private int tokenMaskLength = DEFAULT_TOKEN_MASK_LENGTH;

    /**
     * 是否在认证失败时记录详细信息。
     * 默认值: false（安全考虑）
     */
    private boolean recordAuthFailureDetails = false;

    /**
     * 是否传播追踪上下文到 Feign 调用。
     * 默认值: true
     */
    private boolean propagateToFeign = true;

    /**
     * 是否记录请求路径。
     * 默认值: true
     */
    private boolean recordRequestPath = true;

    /**
     * 是否记录客户端 IP。
     * 默认值: false（隐私考虑）
     */
    private boolean recordClientIp = false;

    /**
     * 采样率 (0.0 - 1.0)。
     * 默认值: 1.0 (100% 采样)
     */
    private float samplingRate = DEFAULT_SAMPLING_RATE;

    /**
     * 不需要追踪的路径列表。
     * 使用 ArrayList 以支持动态添加。
     */
    private List<String> ignoredPaths = new ArrayList<>(Arrays.asList(
            "/actuator/health", "/actuator/prometheus", "/static/", "/favicon.ico"));

    /**
     * 登录路径列表。
     * 用于识别登录请求以记录认证成功事件。
     */
    private List<String> loginPaths = new ArrayList<>(Arrays.asList("/login", "/api/login", "/auth/login", "/api/auth/login"));

    // ========== Getters and Setters ==========

    /**
     * 获取不需要追踪的路径列表。
     *
     * @return 不可修改的路径列表
     */
    public List<String> getIgnoredPaths() {
        return Collections.unmodifiableList(ignoredPaths);
    }

    public void setIgnoredPaths(List<String> ignoredPaths) {
        this.ignoredPaths = ignoredPaths != null ? new ArrayList<>(ignoredPaths) : new ArrayList<>();
    }

    /**
     * 获取登录路径列表。
     *
     * @return 不可修改的登录路径列表
     */
    public List<String> getLoginPaths() {
        return Collections.unmodifiableList(loginPaths);
    }

    public void setLoginPaths(List<String> loginPaths) {
        this.loginPaths = loginPaths != null ? new ArrayList<>(loginPaths) : new ArrayList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getUsernameMaskLength() {
        return usernameMaskLength;
    }

    public void setUsernameMaskLength(int usernameMaskLength) {
        this.usernameMaskLength = usernameMaskLength;
    }

    public int getTokenMaskLength() {
        return tokenMaskLength;
    }

    public void setTokenMaskLength(int tokenMaskLength) {
        this.tokenMaskLength = tokenMaskLength;
    }

    public boolean isRecordAuthFailureDetails() {
        return recordAuthFailureDetails;
    }

    public void setRecordAuthFailureDetails(boolean recordAuthFailureDetails) {
        this.recordAuthFailureDetails = recordAuthFailureDetails;
    }

    public boolean isPropagateToFeign() {
        return propagateToFeign;
    }

    public void setPropagateToFeign(boolean propagateToFeign) {
        this.propagateToFeign = propagateToFeign;
    }

    public boolean isRecordRequestPath() {
        return recordRequestPath;
    }

    public void setRecordRequestPath(boolean recordRequestPath) {
        this.recordRequestPath = recordRequestPath;
    }

    public boolean isRecordClientIp() {
        return recordClientIp;
    }

    public void setRecordClientIp(boolean recordClientIp) {
        this.recordClientIp = recordClientIp;
    }

    public float getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(float samplingRate) {
        this.samplingRate = Math.max(0.0f, Math.min(1.0f, samplingRate));
    }
}
