package com.original.security.event;

import java.util.Map;

/**
 * 认证成功审计事件。
 * <p>
 * 当用户成功完成身份认证时发布此事件，包含用户名、认证方式和相关详情。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuthenticationSuccessEvent extends AuditEvent {

    private static final long serialVersionUID = 1L;

    private final String authenticationMethod;

    public AuthenticationSuccessEvent(Object source, String username, String authenticationMethod, Map<String, Object> details) {
        super(source, username, details);
        this.authenticationMethod = authenticationMethod;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    @Override
    public String getEventType() {
        return "AUTHENTICATION_SUCCESS";
    }
}