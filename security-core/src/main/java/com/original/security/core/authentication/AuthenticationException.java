package com.original.security.core.authentication;

/**
 * 认证异常
 * <p>
 * 在认证过程中发生错误时抛出此异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuthenticationException extends RuntimeException {

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param errorCode 错误代码
     */
    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param errorCode 错误代码
     * @param cause 原因异常
     */
    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误代码
     *
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
}