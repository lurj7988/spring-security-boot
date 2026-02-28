package com.original.security.dto;

/**
 * 认证响应数据传输对象。
 * <p>
 * 包含用户信息和认证令牌。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
public class AuthResponse {
    private Object user;
    private String token;
    private boolean jwtEnabled;

    /**
     * 创建认证响应。
     *
     * @param user 用户信息
     * @param token JWT 令牌（如果 JWT 认证已启用）
     * @param jwtEnabled JWT 认证是否启用
     */
    public AuthResponse(Object user, String token, boolean jwtEnabled) {
        this.user = user;
        this.token = token;
        this.jwtEnabled = jwtEnabled;
    }

    /**
     * 默认构造函数。
     */
    public AuthResponse() {
    }

    /**
     * 获取用户信息。
     *
     * @return 用户信息对象
     */
    public Object getUser() {
        return user;
    }

    /**
     * 设置用户信息。
     *
     * @param user 用户信息对象
     */
    public void setUser(Object user) {
        this.user = user;
    }

    /**
     * 获取 JWT 令牌。
     *
     * @return JWT 令牌字符串，如果 JWT 认证未启用则为 null
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置 JWT 令牌。
     *
     * @param token JWT 令牌字符串
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 检查 JWT 认证是否启用。
     *
     * @return 如果 JWT 认证启用返回 true，否则返回 false
     */
    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    /**
     * 设置 JWT 认证启用状态。
     *
     * @param jwtEnabled JWT 认证是否启用
     */
    public void setJwtEnabled(boolean jwtEnabled) {
        this.jwtEnabled = jwtEnabled;
    }
}
