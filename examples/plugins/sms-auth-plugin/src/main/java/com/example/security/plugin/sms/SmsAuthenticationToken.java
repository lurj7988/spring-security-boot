package com.example.security.plugin.sms;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 短信验证码认证令牌。
 * <p>
 * 封装手机号和验证码信息，用于短信认证流程。
 * 继承 {@link UsernamePasswordAuthenticationToken} 以复用 Spring Security 的认证机制。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 创建未认证的令牌（用于认证请求）
 * SmsAuthenticationToken token = new SmsAuthenticationToken("13800138000", "123456");
 *
 * // 认证成功后创建已认证的令牌
 * SmsAuthenticationToken authenticated = new SmsAuthenticationToken(
 *     userDetails, null, authorities);
 * }</pre>
 *
 * @author Example Team
 * @since 1.0.0
 * @see SmsAuthenticationProvider
 * @see SmsAuthenticationPlugin
 */
public class SmsAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final String phoneNumber;
    private final String verifyCode;

    /**
     * 创建未认证的令牌。
     * <p>
     * 用于认证请求阶段，包含手机号和验证码。
     * </p>
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     */
    public SmsAuthenticationToken(String phoneNumber, String verifyCode) {
        super(phoneNumber, verifyCode);
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
        setAuthenticated(false);
    }

    /**
     * 创建已认证的令牌。
     * <p>
     * 用于认证成功后，包含用户信息和权限列表。
     * </p>
     *
     * @param principal   用户主体（通常为 UserDetails）
     * @param credentials 凭证（认证后通常为 null）
     * @param authorities 权限列表
     */
    public SmsAuthenticationToken(Object principal, Object credentials,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.phoneNumber = (String) principal;
        this.verifyCode = null;
        setAuthenticated(true);
    }

    /**
     * 获取手机号。
     *
     * @return 手机号
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 获取验证码。
     * <p>
     * 仅在未认证令牌中有值，认证成功后为 null。
     * </p>
     *
     * @return 验证码，可能为 null
     */
    public String getVerifyCode() {
        return verifyCode;
    }

    @Override
    public Object getPrincipal() {
        return phoneNumber;
    }

    @Override
    public Object getCredentials() {
        return verifyCode;
    }
}
