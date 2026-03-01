package com.original.security.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.Authentication;

import java.time.Instant;

/**
 * 授权失败审计事件。
 * <p>
 * 当用户访问受保护资源但权限不足时发布此事件。
 * 可用于审计日志、安全监控和告警。
 *
 * <h3>使用示例：</h3>
 * <pre>
 * &#064;Component
 * public class AuthorizationAuditListener {
 *
 *     &#064;EventListener
 *     public void onAuthorizationFailure(AuthorizationFailureEvent event) {
 *         log.warn("Authorization failed: user={}, resource={}, required={}",
 *             event.getUsername(),
 *             event.getResource(),
 *             event.getRequiredAuthority());
 *     }
 * }
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuthorizationFailureEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String resource;
    private final String requiredAuthority;
    private final String denialReason;
    private final Instant timestamp;

    /**
     * 创建授权失败事件。
     *
     * @param source 事件源对象
     * @param authentication 当前认证信息（可能为 null）
     * @param resource 被访问的资源路径
     * @param requiredAuthority 需要的权限或角色
     * @param denialReason 拒绝原因
     */
    public AuthorizationFailureEvent(Object source, Authentication authentication,
                                     String resource, String requiredAuthority, String denialReason) {
        super(source);
        if (authentication == null) {
            this.username = "anonymous";
        } else if (authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            this.username = "anonymous";
        } else {
            this.username = authentication.getName();
        }
        this.resource = resource;
        this.requiredAuthority = requiredAuthority;
        this.denialReason = denialReason;
        this.timestamp = Instant.now();
    }

    /**
     * 获取当前用户名。
     *
     * @return 用户名，如果未认证则返回 "anonymous"
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取被访问的资源路径。
     *
     * @return 资源路径（如 URL 或方法名）
     */
    public String getResource() {
        return resource;
    }

    /**
     * 获取需要的权限或角色。
     *
     * @return 需要的权限（如 "ROLE_ADMIN" 或 "user:write"）
     */
    public String getRequiredAuthority() {
        return requiredAuthority;
    }

    /**
     * 获取拒绝原因。
     *
     * @return 拒绝原因描述
     */
    public String getDenialReason() {
        return denialReason;
    }

    /**
     * 获取事件发生时间。
     *
     * @return 事件时间戳
     */
    public Instant getEventTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AuthorizationFailureEvent{" +
                "username='" + username + '\'' +
                ", resource='" + resource + '\'' +
                ", requiredAuthority='" + requiredAuthority + '\'' +
                ", denialReason='" + denialReason + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
