package com.original.security.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 无效的旧密码异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "INVALID_OLD_PASSWORD")
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}