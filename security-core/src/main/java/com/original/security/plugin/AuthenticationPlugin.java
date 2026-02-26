package com.original.security.plugin;

import com.original.security.core.authentication.AuthenticationProvider;
import org.springframework.lang.Nullable;

/**
 * 认证插件接口
 * <p>
 * AuthenticationPlugin 是插件化认证系统的核心接口，
 * 允许用户自定义认证逻辑并通过 Spring Security 框架集成
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface AuthenticationPlugin {

    /**
     * 获取认证插件的名称
     *
     * @return 插件名称，用于标识不同的认证实现
     */
    String getName();

    /**
     * 获取认证提供者实例
     *
     * @return AuthenticationProvider 实现，提供实际的认证逻辑
     */
    @Nullable
    AuthenticationProvider getAuthenticationProvider();

    /**
     * 检查该插件是否支持指定的认证类型
     *
     * @param authenticationType 认证类型，可以为 null
     * @return true 表示支持该认证类型，false 表示不支持或类型为 null
     */
    boolean supports(@Nullable Class<?> authenticationType);
}