package com.original.security.user.exception;

/**
 * 用户名已存在异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String username;

    public UserAlreadyExistsException(String username) {
        super(String.format("用户名 '%s' 已存在", username));
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
