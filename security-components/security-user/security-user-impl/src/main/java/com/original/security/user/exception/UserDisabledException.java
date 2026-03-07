package com.original.security.user.exception;

/**
 * 用户已被禁用异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class UserDisabledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String username;

    public UserDisabledException(String username) {
        super(String.format("用户 '%s' 已被禁用", username));
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
