package com.original.security.core.authentication;

import com.original.security.core.authentication.user.SecurityUser;

import java.util.Map;

/**
 * 认证结果
 * <p>
 * 封装了认证操作的结果信息，包括：
 * - 认证是否成功
 * - 认证用户信息
 * - 认证相关的数据
 * - 错误信息（如果认证失败）
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuthenticationResult {

    /**
     * 认证是否成功
     */
    private final boolean success;

    /**
     * 认证用户信息
     */
    private final SecurityUser user;

    /**
     * 认证相关的附加数据
     */
    private final Map<String, Object> details;

    /**
     * 错误信息（仅在认证失败时使用）
     */
    private final String errorMessage;

    /**
     * 错误代码
     */
    private final String errorCode;

    private AuthenticationResult(Builder builder) {
        this.success = builder.success;
        this.user = builder.user;
        this.details = builder.details;
        this.errorMessage = builder.errorMessage;
        this.errorCode = builder.errorCode;
    }

    /**
     * 创建成功的认证结果
     *
     * @param user 认证用户
     * @param details 附加数据
     * @return 认证结果实例
     */
    public static AuthenticationResult success(SecurityUser user, Map<String, Object> details) {
        return new AuthenticationResult.Builder()
                .success(true)
                .user(user)
                .details(details)
                .build();
    }

    /**
     * 创建失败的认证结果
     *
     * @param errorMessage 错误信息
     * @param errorCode 错误代码
     * @return 认证结果实例
     */
    public static AuthenticationResult failure(String errorMessage, String errorCode) {
        return new AuthenticationResult.Builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public SecurityUser getUser() {
        return user;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Builder for AuthenticationResult
     */
    public static class Builder {
        private boolean success;
        private SecurityUser user;
        private Map<String, Object> details;
        private String errorMessage;
        private String errorCode;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder user(SecurityUser user) {
            this.user = user;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public AuthenticationResult build() {
            return new AuthenticationResult(this);
        }
    }
}