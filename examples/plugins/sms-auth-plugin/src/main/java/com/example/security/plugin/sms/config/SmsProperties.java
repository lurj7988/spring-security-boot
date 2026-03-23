package com.example.security.plugin.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信认证配置属性。
 *
 * @author Example Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.sms")
public class SmsProperties {

    /**
     * 是否启用短信认证
     */
    private boolean enabled = true;

    /**
     * 验证码有效期（秒）
     */
    private int expireSeconds = 300;

    /**
     * 最大验证尝试次数
     */
    private int maxAttempts = 5;

    /**
     * 验证码长度
     */
    private int codeLength = 6;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }
}
