package com.original.security.user.api.dto.response;

/**
 * 密码重置响应 DTO
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class PasswordResetResponse {

    /**
     * 新生成的临时密码
     */
    private String newPassword;

    /**
     * 操作状态消息
     */
    private String message;

    /**
     * 是否已通过其他方式（如邮件）通知用户
     */
    private boolean notified = true;

    public PasswordResetResponse() {
    }

    public PasswordResetResponse(String newPassword, String message) {
        this.newPassword = newPassword;
        this.message = message;
    }

    public PasswordResetResponse(String newPassword, String message, boolean notified) {
        this.newPassword = newPassword;
        this.message = message;
        this.notified = notified;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}