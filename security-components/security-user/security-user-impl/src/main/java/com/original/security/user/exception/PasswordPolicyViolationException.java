package com.original.security.user.exception;

/**
 * 密码策略违规异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class PasswordPolicyViolationException extends RuntimeException {

    public PasswordPolicyViolationException(String message) {
        super(message);
    }

    public PasswordPolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}