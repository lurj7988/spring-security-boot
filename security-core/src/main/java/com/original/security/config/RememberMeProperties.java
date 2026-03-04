package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Remember Me 认证配置属性。
 * <p>
 * 绑定以 "security.remember-me" 为前缀的配置项，用于配置 Remember Me 相关参数。
 * </p>
 *
 * <p>配置示例 (application.properties)：</p>
 * <pre>
 * # 是否启用 Remember Me 功能，默认 true
 * security.remember-me.enabled=true
 * # Token 有效期（秒），默认 604800 秒（7天）
 * security.remember-me.token-validity-seconds=604800
 * # 安全密钥，用于生成 Token 签名
 * security.remember-me.key=my-secure-key
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.remember-me")
public class RememberMeProperties {

    /**
     * 默认 Token 有效期：7 天（604800 秒）
     */
    public static final int DEFAULT_TOKEN_VALIDITY_SECONDS = 604800;

    /**
     * 默认 Cookie 名称
     */
    public static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "remember-me";

    /**
     * 是否启用 Remember Me 功能
     */
    private boolean enabled = true;

    /**
     * Token 有效期（秒）
     */
    private int tokenValiditySeconds = DEFAULT_TOKEN_VALIDITY_SECONDS;

    /**
     * Remember Me 安全密钥
     * 如果未配置，框架应该在初始化时生成一个随机密钥或使用默认密钥
     */
    private String key;
    
    /**
     * Remember Me Cookie 名称
     */
    private String cookieName = DEFAULT_REMEMBER_ME_COOKIE_NAME;

    /**
     * 获取是否启用 Remember Me 功能。
     *
     * @return 如果启用返回 true，否则返回 false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 Remember Me 功能。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取 Token 有效期（秒）。
     *
     * @return Token 有效期
     */
    public int getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    /**
     * 设置 Token 有效期（秒）。
     *
     * @param tokenValiditySeconds Token 有效期，必须大于 0
     */
    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        if (tokenValiditySeconds <= 0) {
            throw new IllegalArgumentException("Remember me token validity seconds must be greater than 0");
        }
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    /**
     * 获取 Remember Me 安全密钥。
     *
     * @return 安全密钥
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置 Remember Me 安全密钥。
     *
     * @param key 安全密钥
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 获取 Cookie 名称。
     *
     * @return Cookie 名称
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * 设置 Cookie 名称。
     *
     * @param cookieName Cookie 名称，不能为空
     */
    public void setCookieName(String cookieName) {
        if (cookieName == null || cookieName.trim().isEmpty()) {
            throw new IllegalArgumentException("Remember me cookie name cannot be empty");
        }
        this.cookieName = cookieName;
    }
}
