package com.original.security.event;

import org.springframework.security.core.Authentication;
import java.util.Map;

/**
 * 授权失败审计事件。
 * <p>
 * 当用户尝试访问无权限的资源时发布此事件，用于安全审计和访问监控。
 * 包含用户、资源路径、所需权限和拒绝原因。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuthorizationFailureEvent extends AuditEvent {

    private static final long serialVersionUID = 1L;

    private final String resource;
    private final String requiredPermission;
    private final String denialReason;

    /**
     * 基础构造函数。
     *
     * @param source 事件源
     * @param username 用户名
     * @param resource 被访问的资源路径
     * @param requiredPermission 所需权限
     * @param details 详细信息
     */
    public AuthorizationFailureEvent(Object source, String username, String resource, String requiredPermission, Map<String, Object> details) {
        super(source, username, details);
        this.resource = resource;
        this.requiredPermission = requiredPermission;
        this.denialReason = "Access Denied";
    }

    /**
     * 向后兼容的构造函数。
     *
     * @param source 事件源
     * @param authentication 认证信息
     * @param resource 被访问的资源路径
     * @param requiredAuthority 所需权限
     * @param denialReason 拒绝原因
     */
    public AuthorizationFailureEvent(Object source, Authentication authentication,
                                     String resource, String requiredAuthority, String denialReason) {
        super(source, extractUsername(authentication), null);
        this.resource = resource;
        this.requiredPermission = requiredAuthority;
        this.denialReason = denialReason;
    }

    /**
     * 从 Authentication 对象中提取用户名。
     *
     * @param authentication 认证对象
     * @return 用户名，如果未认证则返回 "anonymous"
     */
    public static String extractUsername(Authentication authentication) {
        if (authentication == null) {
            return ANONYMOUS_USER;
        } else if (authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return ANONYMOUS_USER;
        } else {
            return authentication.getName();
        }
    }

    public String getResource() {
        return resource;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    /**
     * 获取所需权限（向后兼容别名）。
     *
     * @return 所需权限
     * @deprecated 使用 {@link #getRequiredPermission()} 替代
     */
    @Deprecated
    public String getRequiredAuthority() {
        return requiredPermission;
    }

    public String getDenialReason() {
        return denialReason;
    }

    @Override
    public String getEventType() {
        return "AUTHORIZATION_FAILURE";
    }
}