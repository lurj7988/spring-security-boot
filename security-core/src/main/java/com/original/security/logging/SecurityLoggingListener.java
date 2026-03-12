package com.original.security.logging;

import com.original.security.event.AuthenticationFailureEvent;
import com.original.security.event.AuthenticationSuccessEvent;
import com.original.security.event.AuthorizationFailureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 安全事件日志监听器。
 * <p>
 * 监听框架发布的各类安全事件，并将其转化为结构化日志记录。
 * 使用 @Async 异步记录日志，避免影响业务主流程。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class SecurityLoggingListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityLoggingListener.class);

    /**
     * 事件类型前缀。
     */
    private static final String EVENT_TYPE_PREFIX = "SECURITY_";

    private final SecurityLogger securityLogger;

    public SecurityLoggingListener(SecurityLogger securityLogger) {
        this.securityLogger = securityLogger;
    }

    /**
     * 监听并记录认证成功事件。
     *
     * @param event 认证成功事件
     */
    @Async("securityLoggingTaskExecutor")
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            SecurityLogEvent logEvent = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + "AUTHENTICATION_SUCCESS")
                    .level(SecurityLogLevel.INFO)
                    .username(event.getUsername())
                    .success(true)
                    .message("User authenticated successfully")
                    .field(SecurityLogField.AUTH_TYPE, event.getAuthenticationMethod())
                    .fields(event.getDetails())
                    .build();

            securityLogger.info(logEvent);
        } catch (Exception e) {
            log.warn("Failed to log authentication success event: {}", e.getMessage());
        }
    }

    /**
     * 监听并记录认证失败事件。
     *
     * @param event 认证失败事件
     */
    @Async("securityLoggingTaskExecutor")
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureEvent event) {
        try {
            SecurityLogEvent logEvent = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + "AUTHENTICATION_FAILURE")
                    .level(SecurityLogLevel.WARN)
                    .username(event.getUsername())
                    .success(false)
                    .message("User authentication failed")
                    .errorMessage(event.getFailureReason())
                    .fields(event.getDetails())
                    .build();

            securityLogger.warn(logEvent);
        } catch (Exception e) {
            log.warn("Failed to log authentication failure event: {}", e.getMessage());
        }
    }

    /**
     * 监听并记录授权失败事件。
     *
     * @param event 授权失败事件
     */
    @Async("securityLoggingTaskExecutor")
    @EventListener
    public void onAuthorizationFailure(AuthorizationFailureEvent event) {
        try {
            SecurityLogEvent logEvent = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + "AUTHORIZATION_FAILURE")
                    .level(SecurityLogLevel.WARN)
                    .username(event.getUsername())
                    .success(false)
                    .message("User authorization failed")
                    .field(SecurityLogField.RESOURCE, event.getResource())
                    .field(SecurityLogField.PERMISSION, event.getRequiredPermission())
                    .errorMessage(event.getDenialReason())
                    .fields(event.getDetails())
                    .build();

            securityLogger.warn(logEvent);
        } catch (Exception e) {
            log.warn("Failed to log authorization failure event: {}", e.getMessage());
        }
    }
}
