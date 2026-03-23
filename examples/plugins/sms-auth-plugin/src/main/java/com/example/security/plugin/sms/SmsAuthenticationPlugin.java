package com.example.security.plugin.sms;

import com.original.security.plugin.AuthenticationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Component;

/**
 * 短信验证码认证插件。
 * <p>
 * 实现 {@link AuthenticationPlugin} 接口，提供短信验证码认证能力。
 * 该插件会自动被 Spring Boot 自动配置注册到认证系统。
 * </p>
 *
 * <p>功能特性：</p>
 * <ul>
 *     <li>支持手机号 + 验证码登录</li>
 *     <li>验证码有效期可配置（默认 5 分钟）</li>
 *     <li>支持验证码尝试次数限制</li>
 *     <li>日志中自动脱敏手机号</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 1. 实现 SmsVerifyCodeService 接口
 * @Service
 * public class MySmsVerifyCodeService implements SmsVerifyCodeService {
 *     // 实现验证码发送和验证逻辑
 * }
 *
 * // 2. 确保 UserDetailsService 支持手机号查询
 * @Service
 * public class PhoneUserDetailsService implements UserDetailsService {
 *     @Override
 *     public UserDetails loadUserByUsername(String phone) {
 *         // 根据手机号加载用户
 *     }
 * }
 *
 * // 3. 插件会自动注册，无需额外配置
 * }</pre>
 *
 * <p>配置项：</p>
 * <pre>
 * # application.properties
 * security.sms.expire-seconds=300
 * security.sms.max-attempts=5
 * security.sms.enabled=true
 * </pre>
 *
 * @author Example Team
 * @since 1.0.0
 * @see AuthenticationPlugin
 * @see SmsAuthenticationProvider
 * @see SmsVerifyCodeService
 */
@Component
@Order(3)  // 在用户名密码认证之后
public class SmsAuthenticationPlugin implements AuthenticationPlugin {

    private static final Logger log = LoggerFactory.getLogger(SmsAuthenticationPlugin.class);

    /**
     * 插件名称标识。
     */
    public static final String PLUGIN_NAME = "sms-authentication";

    private final SmsAuthenticationProvider authenticationProvider;

    /**
     * 构造短信认证插件。
     *
     * @param authenticationProvider 短信认证提供者，通过构造器注入
     */
    public SmsAuthenticationPlugin(SmsAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        log.info("SMS authentication plugin initialized");
    }

    /**
     * 返回插件的名称标识。
     *
     * @return 插件名称 "sms-authentication"
     */
    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    /**
     * 返回底层认证提供者。
     *
     * @return {@link SmsAuthenticationProvider} 实例
     */
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    /**
     * 检查插件是否支持指定的认证类型。
     * <p>
     * 该插件仅支持 {@link SmsAuthenticationToken} 类型。
     * </p>
     *
     * @param authenticationType 要检查的认证类型
     * @return 如果是 {@link SmsAuthenticationToken} 类型返回 true
     */
    @Override
    public boolean supports(Class<?> authenticationType) {
        boolean supported = authenticationType != null
            && SmsAuthenticationToken.class.isAssignableFrom(authenticationType);

        if (log.isDebugEnabled() && authenticationType != null) {
            log.debug("SMS authentication support check for {}: {}",
                authenticationType.getSimpleName(), supported);
        }

        return supported;
    }
}
