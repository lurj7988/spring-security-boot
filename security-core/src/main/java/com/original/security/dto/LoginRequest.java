package com.original.security.dto;

/**
 * 登录请求数据传输对象。
 * <p>
 * 包含用户名、密码和记住我字段，用于用户登录请求。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
public class LoginRequest {
    private String username;
    private String password;
    private boolean rememberMe;

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码。
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码。
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取是否记住我。
     *
     * @return 记住我
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * 设置是否记住我。
     *
     * @param rememberMe 记住我
     */
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
