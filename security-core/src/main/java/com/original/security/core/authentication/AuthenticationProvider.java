package com.original.security.core.authentication;

import com.original.security.core.authentication.token.Token;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.lang.Nullable;

/**
 * 认证提供者接口
 * <p>
 * AuthenticationProvider 定义了认证系统的核心功能，
 * 负责具体的认证逻辑实现
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface AuthenticationProvider {

    /**
     * 认证用户凭据
     *
     * @param credentials 用户凭据（如用户名密码、Token等）
     * @param authenticationType 认证类型
     * @return 认证结果，包含用户信息和认证状态
     * @throws AuthenticationException 当认证失败时抛出
     */
    AuthenticationResult authenticate(Object credentials, String authenticationType) throws AuthenticationException;

    /**
     * 验证 Token 的有效性
     *
     * @param token 要验证的 Token
     * @return true 表示 Token 有效，false 表示无效
     */
    boolean validateToken(Token token);

    /**
     * 刷新过期或即将过期的 Token
     *
     * @param token 原始 Token
     * @return 新的 Token，或 null 表示无法刷新
     */
    @Nullable
    Token refreshToken(Token token);

    /**
     * 验证用户名和密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证结果
     * @throws AuthenticationException 认证失败时抛出
     */
    AuthenticationResult authenticate(String username, String password) throws AuthenticationException;

    /**
     * 获取用户详细信息
     *
     * @param username 用户名
     * @return 用户详细信息
     * @throws AuthenticationException 用户不存在时抛出
     */
    UserDetails loadUserByUsername(String username) throws AuthenticationException;
}