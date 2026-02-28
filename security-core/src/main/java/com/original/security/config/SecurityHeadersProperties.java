package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全响应头相关的配置属性。
 * <p>
 * 支持配置以下安全响应头：
 * <ul>
 *     <li>X-Frame-Options - 防止点击劫持攻击</li>
 *     <li>X-Content-Type-Options - 防止 MIME 类型嗅探</li>
 *     <li>X-XSS-Protection - 启用浏览器 XSS 过滤器</li>
 *     <li>Strict-Transport-Security (HSTS) - 强制使用 HTTPS</li>
 * </ul>
 * <p>
 * 默认情况下所有安全响应头均启用。
 * <p>
 * 配置示例：
 * <pre>
 * security.network.headers.enabled=true
 * security.network.headers.frame-options=DENY
 * security.network.headers.content-type-options=true
 * security.network.headers.xss-protection=true
 * security.network.headers.hsts-max-age=31536000
 * </pre>
 *
 * @author Naulu
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.network.headers")
public class SecurityHeadersProperties {

    /**
     * 是否启用安全响应头
     */
    private boolean enabled = true;

    /**
     * X-Frame-Options 值 (DENY, SAMEORIGIN)
     * <p>
     * DENY: 完全禁止页面被嵌入
     * SAMEORIGIN: 仅允许同源页面嵌入
     */
    private String frameOptions = "DENY";

    /**
     * 是否启用 X-Content-Type-Options: nosniff
     */
    private boolean contentTypeOptions = true;

    /**
     * 是否启用 X-XSS-Protection (1; mode=block)
     */
    private boolean xssProtection = true;

    /**
     * HSTS 的 max-age 值（秒）
     * <p>
     * 注意：HSTS 仅应在 HTTPS 环境下使用。如果应用未配置 SSL/TLS，
     * 建议将此值设置为 0 或禁用该功能。
     * </p>
     */
    private int hstsMaxAge = 31536000;

    /**
     * 是否在 HSTS 中包含子域名
     * <p>
     * 如果为 true，HSTS 策略将应用于当前域名及其所有子域名。
     * </p>
     */
    private boolean hstsIncludeSubDomains = true;

    /**
     * 是否启用 HSTS preload
     * <p>
     * 如果为 true，浏览器可以将域名添加到 HSTS 预加载列表中。
     * 注意：此操作不可逆，需要通过浏览器厂商的流程移除。
     * </p>
     */
    private boolean hstsPreload = false;

    /**
     * 获取是否启用安全响应头。
     *
     * @return true 如果启用，false 如果禁用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用安全响应头。
     *
     * @param enabled true 启用，false 禁用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取 X-Frame-Options 值。
     *
     * @return X-Frame-Options 值（DENY 或 SAMEORIGIN）
     */
    public String getFrameOptions() {
        return frameOptions;
    }

    /**
     * 设置 X-Frame-Options 值。
     * <p>
     * null 值将被重置为默认值 "DENY"。如果需要禁用帧保护，
     * 请通过设置 {@code enabled=false} 来完全禁用安全响应头功能。
     * </p>
     * <p>
     * 注意：无论输入的大小写如何，值都会被统一转换为大写格式（DENY 或 SAMEORIGIN），
     * 以符合 HTTP 响应头的标准格式。
     * </p>
     *
     * @param frameOptions 帧选项值，必须是 "DENY" 或 "SAMEORIGIN"（不区分大小写），null 表示重置为默认值
     * @throws IllegalArgumentException 如果 frameOptions 不是 "DENY" 或 "SAMEORIGIN" 且不为 null
     */
    public void setFrameOptions(String frameOptions) {
        if (frameOptions == null) {
            this.frameOptions = "DENY";
            return;
        }
        if (!"DENY".equalsIgnoreCase(frameOptions) && !"SAMEORIGIN".equalsIgnoreCase(frameOptions)) {
            throw new IllegalArgumentException("frameOptions must be either 'DENY' or 'SAMEORIGIN', but was: " + frameOptions);
        }
        // 统一转换为大写，符合 HTTP 响应头标准
        this.frameOptions = frameOptions.toUpperCase();
    }

    /**
     * 获取是否启用 X-Content-Type-Options: nosniff。
     *
     * @return true 如果启用，false 如果禁用
     */
    public boolean isContentTypeOptions() {
        return contentTypeOptions;
    }

    /**
     * 设置是否启用 X-Content-Type-Options: nosniff。
     *
     * @param contentTypeOptions true 启用，false 禁用
     */
    public void setContentTypeOptions(boolean contentTypeOptions) {
        this.contentTypeOptions = contentTypeOptions;
    }

    /**
     * 获取是否启用 X-XSS-Protection: 1; mode=block。
     *
     * @return true 如果启用，false 如果禁用
     */
    public boolean isXssProtection() {
        return xssProtection;
    }

    /**
     * 设置是否启用 X-XSS-Protection: 1; mode=block。
     *
     * @param xssProtection true 启用，false 禁用
     */
    public void setXssProtection(boolean xssProtection) {
        this.xssProtection = xssProtection;
    }

    /**
     * 获取 HSTS 的 max-age 值（秒）。
     *
     * @return HSTS max-age 值（秒）
     */
    public int getHstsMaxAge() {
        return hstsMaxAge;
    }

    /**
     * 设置 HSTS 的 max-age 值（秒）。
     *
     * @param hstsMaxAge HSTS max-age 值（秒），必须非负
     * @throws IllegalArgumentException 如果 hstsMaxAge 为负数
     */
    public void setHstsMaxAge(int hstsMaxAge) {
        if (hstsMaxAge < 0) {
            throw new IllegalArgumentException("hstsMaxAge must be non-negative, but was: " + hstsMaxAge);
        }
        this.hstsMaxAge = hstsMaxAge;
    }

    /**
     * 获取是否在 HSTS 中包含子域名。
     *
     * @return true 如果包含子域名，false 如果不包含
     */
    public boolean isHstsIncludeSubDomains() {
        return hstsIncludeSubDomains;
    }

    /**
     * 设置是否在 HSTS 中包含子域名。
     *
     * @param hstsIncludeSubDomains true 包含子域名，false 不包含
     */
    public void setHstsIncludeSubDomains(boolean hstsIncludeSubDomains) {
        this.hstsIncludeSubDomains = hstsIncludeSubDomains;
    }

    /**
     * 获取是否启用 HSTS preload。
     *
     * @return true 如果启用 preload，false 如果不启用
     */
    public boolean isHstsPreload() {
        return hstsPreload;
    }

    /**
     * 设置是否启用 HSTS preload。
     *
     * @param hstsPreload true 启用 preload，false 不启用
     */
    public void setHstsPreload(boolean hstsPreload) {
        this.hstsPreload = hstsPreload;
    }
}
