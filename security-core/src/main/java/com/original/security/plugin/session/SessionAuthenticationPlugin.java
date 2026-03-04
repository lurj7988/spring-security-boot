package com.original.security.plugin.session;

import com.original.security.core.authentication.AuthenticationProvider;
import com.original.security.plugin.AuthenticationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * 基于 Session 的有状态认证插件。
 * <p>
 * 该插件实现了 {@link AuthenticationPlugin} 接口，提供传统 Web 应用的 Session 认证功能。
 * Session 认证适用于需要服务器端保持用户状态的传统 Web 应用场景。
 * </p>
 *
 * <p>功能特性：</p>
 * <ul>
 *     <li>支持服务器端 Session 存储</li>
 *     <li>支持可配置的 Session 超时时间</li>
 *     <li>支持 Session 固定攻击防护</li>
 *     <li>支持并发 Session 控制</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 该插件会自动被 Spring Boot 自动配置注册
 * // 配置 application.properties:
 * // security.session.timeout=1800
 * // security.session.max-sessions=1
 * }</pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see AuthenticationPlugin
 * @see SessionProperties
 * @see UsernamePasswordAuthenticationToken
 */
@Component
@Order(2)
public class SessionAuthenticationPlugin implements AuthenticationPlugin {

    private static final Logger log = LoggerFactory.getLogger(SessionAuthenticationPlugin.class);

    private static final String PLUGIN_NAME = "session-authentication";

    /**
     * 返回插件的名称标识。
     *
     * @return 插件名称 "session-authentication"
     */
    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    /**
     * 返回底层认证提供者。
     * <p>
     * Session 认证模式下，实际的认证逻辑由 Spring Security 的
     * {@link org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider}
     * 处理，此插件主要提供 Session 管理配置而非独立的 AuthenticationProvider。
     * </p>
     *
     * @return null，因为 Session 认证依赖 Spring Security 内置机制
     */
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        log.debug("Session authentication relies on Spring Security built-in mechanisms");
        return null;
    }

    /**
     * 检查插件是否支持指定的认证类型。
     * <p>
     * 该插件支持 {@link UsernamePasswordAuthenticationToken} 类型，
     * 用于传统的用户名密码登录后创建 Session 的场景。
     * </p>
     *
     * @param authenticationType 要检查的认证类型
     * @return 如果支持该类型返回 true，否则返回 false
     */
    @Override
    public boolean supports(Class<?> authenticationType) {
        boolean supported = authenticationType != null
                && UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType);
        if (log.isDebugEnabled() && authenticationType != null) {
            log.debug("Session authentication support check for {}: {}",
                    authenticationType.getSimpleName(), supported);
        }
        return supported;
    }
}
