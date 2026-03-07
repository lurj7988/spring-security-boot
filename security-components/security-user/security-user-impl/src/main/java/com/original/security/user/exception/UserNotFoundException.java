package com.original.security.user.exception;

/**
 * 用户不存在异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String identifier;

    public UserNotFoundException(String identifier) {
        super(String.format("用户 '%s' 不存在", identifier));
        this.identifier = identifier;
    }

    public UserNotFoundException(Long userId) {
        super(String.format("用户 ID %d 不存在", userId));
        this.identifier = userId.toString();
    }

    public String getIdentifier() {
        return identifier;
    }
}
