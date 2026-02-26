package com.original.security.plugin.impl;

import com.original.security.core.authentication.AuthenticationProvider;
import com.original.security.core.authentication.JwtAuthenticationToken;
import com.original.security.plugin.AuthenticationPlugin;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * 默认认证插件实现
 * <p>
 * 提供基本的认证功能实现，用户可以根据需要扩展此实现
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class DefaultAuthenticationPlugin implements AuthenticationPlugin {

    private final String name;
    private final AuthenticationProvider authenticationProvider;

    /**
     * 构造函数
     *
     * @param name 插件名称
     * @param authenticationProvider 认证提供者
     */
    public DefaultAuthenticationPlugin(String name, AuthenticationProvider authenticationProvider) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Authentication plugin name cannot be empty");
        }
        if (authenticationProvider == null) {
            throw new IllegalArgumentException("Authentication provider cannot be null");
        }

        this.name = name;
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    /**
     * 检查是否支持指定的认证类型
     * <p>
     * 该方法根据 Spring Security 的 Authentication 类型来判断是否支持。
     * 当前支持以下类型：
     * - UsernamePasswordAuthenticationToken：用户名密码认证
     * - JwtAuthenticationToken：JWT Token 认证
     *
     * @param authenticationType 认证类型，必须是 Spring Security 的 Authentication 实现类
     * @return 如果支持该认证类型返回 true，否则返回 false
     */
    @Override
    public boolean supports(Class<?> authenticationType) {
        if (authenticationType == null) {
            return false;
        }

        // 支持多种认证类型
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType)
                || JwtAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}