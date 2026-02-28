package com.original.security.dto;

/**
 * Token 刷新请求数据传输对象。
 * <p>
 * 用于请求刷新 JWT Token。
 * 当前实现使用当前有效的 JWT access token 来获取新的 token。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
public class RefreshRequest {
    /**
     * 用于刷新的 JWT Token。
     * 当前实现使用 access token 进行刷新。
     */
    private String token;

    /**
     * 获取 JWT Token。
     *
     * @return JWT Token 字符串
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置 JWT Token。
     *
     * @param token JWT Token 字符串
     */
    public void setToken(String token) {
        this.token = token;
    }
}
