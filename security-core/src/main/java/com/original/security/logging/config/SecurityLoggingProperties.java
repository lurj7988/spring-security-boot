package com.original.security.logging.config;

import com.original.security.logging.SensitiveDataMasker;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全日志配置属性。
 * <p>
 * 配置前缀：security.logging
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.logging")
public class SecurityLoggingProperties {

    /**
     * 是否启用结构化日志。默认 true。
     */
    private boolean enabled = true;

    /**
     * 是否使用 JSON 格式输出。默认 true。
     */
    private boolean jsonOutput = true;

    /**
     * 是否包含堆栈跟踪（仅在 ERROR 级别）。默认 true。
     */
    private boolean includeStackTrace = true;

    /**
     * 是否包含客户端 IP。默认 true。
     * <p>
     * 注意：此功能需要集成 Web 容器上下文，将在后续版本中完全实现。
     */
    private boolean includeClientIp = true;

    /**
     * 是否包含用户代理。默认 false。
     * <p>
     * 注意：此功能需要集成 Web 容器上下文，将在后续版本中完全实现。
     */
    private boolean includeUserAgent = false;

    /**
     * 是否包含请求 ID。默认 true。
     * <p>
     * 注意：此功能需要集成请求追踪机制（如 Spring Cloud Sleuth），将在后续版本中完全实现。
     */
    private boolean includeRequestId = true;

    /**
     * 是否包含会话 ID。默认 false。
     * <p>
     * 注意：此功能需要集成会话管理模块，将在后续版本中完全实现。
     */
    private boolean includeSessionId = false;

    /**
     * 默认日志级别。默认 INFO。
     */
    private String defaultLevel = "INFO";

    /**
     * 敏感字段脱敏模式。
     * <ul>
     *   <li>FULL - 完全隐藏敏感字段</li>
     *   <li>PARTIAL - 部分显示（如 JWT Token 显示前 10 字符）</li>
     *   <li>NONE - 不脱敏（不推荐，仅用于调试）</li>
     * </ul>
     */
    private SensitiveDataMasker.MaskingMode maskingMode = SensitiveDataMasker.MaskingMode.PARTIAL;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    public void setJsonOutput(boolean jsonOutput) {
        this.jsonOutput = jsonOutput;
    }

    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }

    public void setIncludeStackTrace(boolean includeStackTrace) {
        this.includeStackTrace = includeStackTrace;
    }

    public boolean isIncludeClientIp() {
        return includeClientIp;
    }

    public void setIncludeClientIp(boolean includeClientIp) {
        this.includeClientIp = includeClientIp;
    }

    public boolean isIncludeUserAgent() {
        return includeUserAgent;
    }

    public void setIncludeUserAgent(boolean includeUserAgent) {
        this.includeUserAgent = includeUserAgent;
    }

    public boolean isIncludeRequestId() {
        return includeRequestId;
    }

    public void setIncludeRequestId(boolean includeRequestId) {
        this.includeRequestId = includeRequestId;
    }

    public boolean isIncludeSessionId() {
        return includeSessionId;
    }

    public void setIncludeSessionId(boolean includeSessionId) {
        this.includeSessionId = includeSessionId;
    }

    public String getDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(String defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    public SensitiveDataMasker.MaskingMode getMaskingMode() {
        return maskingMode;
    }

    public void setMaskingMode(SensitiveDataMasker.MaskingMode maskingMode) {
        this.maskingMode = maskingMode;
    }
}
