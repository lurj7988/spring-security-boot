package com.original.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.stereotype.Component;

/**
 * 授权审计监听器。
 * <p>
 * 监听并记录授权成功（AuthorizedEvent）和授权失败（AuthorizationFailureEvent）的审计日志。
 * 满足架构审计日志要求（FR16）。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class AuthorizationAuditListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationAuditListener.class);

    /**
     * 监听并记录框架自定义的授权失败事件。
     *
     * @param event 授权失败事件
     */
    @EventListener
    public void onCustomAuthorizationFailure(AuthorizationFailureEvent event) {
        log.warn("AUDIT [AUTHORIZATION_FAILURE]: User '{}' failed to access resource '{}'. Required: '{}'. Reason: '{}'",
                event.getUsername(),
                event.getResource(),
                event.getRequiredAuthority(),
                event.getDenialReason());
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
        String username = "anonymous";
        if (event.getAuthentication() != null) {
            if (event.getAuthentication() instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
                username = "anonymous";
            } else if (event.getAuthentication().getName() != null) {
                username = event.getAuthentication().getName();
            }
        }
        
        // SecureObject 是被调用的方法拦截等
        String resource = event.getSource() != null ? event.getSource().toString() : "Unknown Resource";
        String configAttributes = event.getConfigAttributes() != null ? event.getConfigAttributes().toString() : "None";

        log.info("AUDIT [AUTHORIZATION_SUCCESS]: User '{}' successfully accessed resource '{}' with required attributes '{}'",
                username,
                resource,
                configAttributes);
    }
}
