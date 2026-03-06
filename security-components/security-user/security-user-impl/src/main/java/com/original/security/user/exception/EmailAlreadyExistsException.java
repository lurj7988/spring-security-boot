package com.original.security.user.exception;

/**
 * 邮箱已存在异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class EmailAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super(String.format("邮箱 '%s' 已存在", email));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
