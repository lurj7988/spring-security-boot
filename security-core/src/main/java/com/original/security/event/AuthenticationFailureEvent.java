package com.original.security.event;

import java.util.Map;

/**
 * 认证失败审计事件。
 * <p>
 * 当用户身份认证失败时发布此事件，用于安全监控和异常检测。
 * 包含失败原因、用户名（如可用）和相关详情。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuthenticationFailureEvent extends AuditEvent {

    private static final long serialVersionUID = 1L;

    private final String failureReason;

    public AuthenticationFailureEvent(Object source, String username, String failureReason, Map<String, Object> details) {
        super(source, username, details);
        this.failureReason = failureReason;
    }

    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String getEventType() {
        return "AUTHENTICATION_FAILURE";
    }
}