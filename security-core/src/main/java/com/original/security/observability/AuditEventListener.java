package com.original.security.observability;

import com.original.security.event.AuditEvent;
import com.original.security.event.AuthenticationFailureEvent;
import com.original.security.event.AuthenticationSuccessEvent;
import com.original.security.event.AuthorizationFailureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 审计事件监听器。
 * <p>
 * 统一处理并记录所有审计事件的结构化日志，包括自定义审计事件和 Spring Security 标准事件。
 * 敏感信息（密码、token 等）会在 {@link AuditEvent} 基类中被自动过滤。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    @EventListener
    public void onAuditEvent(AuditEvent event) {
        if (event instanceof AuthenticationSuccessEvent) {
            AuthenticationSuccessEvent successEvent = (AuthenticationSuccessEvent) event;
            log.info("AUDIT [AUTHENTICATION_SUCCESS]: user='{}', method='{}', details={}",
                successEvent.getUsername(),
                successEvent.getAuthenticationMethod(),
                successEvent.getDetails());
        } else if (event instanceof AuthenticationFailureEvent) {
            AuthenticationFailureEvent failureEvent = (AuthenticationFailureEvent) event;
            log.warn("AUDIT [AUTHENTICATION_FAILURE]: user='{}', reason='{}', details={}",
                failureEvent.getUsername(),
                failureEvent.getFailureReason(),
                failureEvent.getDetails());
        } else if (event instanceof AuthorizationFailureEvent) {
            AuthorizationFailureEvent authFailureEvent = (AuthorizationFailureEvent) event;
            log.warn("AUDIT [AUTHORIZATION_FAILURE]: user='{}', resource='{}', required='{}', details={}",
                authFailureEvent.getUsername(),
                authFailureEvent.getResource(),
                authFailureEvent.getRequiredPermission(),
                authFailureEvent.getDetails());
        }
    }

    /**
     * 监听并记录 Spring Security 标准的授权成功事件。
     * <p>
     * 注：@PreAuthorize 等机制在授权成功时会通过 MethodSecurityInterceptor 等组件发布 AuthorizedEvent。
     *
     * @param event 授权成功事件
     */
    @EventListener
    public void onAuthorizedEvent(AuthorizedEvent event) {
        String username = extractUsername(event.getAuthentication());

        // SecureObject 是被调用的方法拦截等
        String resource = event.getSource() != null ? event.getSource().toString() : "Unknown Resource";
        String configAttributes = event.getConfigAttributes() != null ? event.getConfigAttributes().toString() : "None";

        log.info("AUDIT [AUTHORIZATION_SUCCESS]: user='{}', resource='{}', attributes={}",
                username,
                resource,
                configAttributes);
    }

    /**
     * 从 Authentication 对象中提取用户名。
     *
     * @param authentication 认证对象
     * @return 用户名，如果未认证则返回 "anonymous"
     */
    private String extractUsername(Authentication authentication) {
        return AuthorizationFailureEvent.extractUsername(authentication);
    }
}