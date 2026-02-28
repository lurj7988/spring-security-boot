package com.original.security.plugin.username;

import com.original.security.core.authentication.AuthenticationProvider;
import com.original.security.plugin.AuthenticationPlugin;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * 基于用户名和密码的认证插件。
 * <p>
 * 该插件实现了 {@link AuthenticationPlugin} 接口，提供用户名密码认证功能。
 * 使用 {@link DaoAuthenticationProvider} 作为底层认证提供者。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 该插件会自动被 Spring Boot 自动配置注册
 * // 支持的认证类型：UsernamePasswordAuthenticationToken
 * }</pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see AuthenticationPlugin
 * @see DaoAuthenticationProvider
 * @see UsernamePasswordAuthenticationToken
 */
@Component
@Order(1)
public class UsernamePasswordAuthenticationPlugin implements AuthenticationPlugin {

    private final DaoAuthenticationProvider authenticationProvider;

    /**
     * 构造一个新的用户名密码认证插件。
     *
     * @param authenticationProvider DAO 认证提供者，用于执行实际的用户认证
     */
    public UsernamePasswordAuthenticationPlugin(DaoAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * 返回插件的名称标识。
     *
     * @return 插件名称 "username-password"
     */
    @Override
    public String getName() {
        return "username-password";
    }

    /**
     * 返回底层认证提供者。
     *
     * @return {@link DaoAuthenticationProvider} 实例
     */
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    /**
     * 检查插件是否支持指定的认证类型。
     * <p>
     * 该插件仅支持 {@link UsernamePasswordAuthenticationToken} 类型。
     * </p>
     *
     * @param authenticationType 要检查的认证类型
     * @return 如果支持该类型返回 true，否则返回 false
     */
    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType != null && UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
