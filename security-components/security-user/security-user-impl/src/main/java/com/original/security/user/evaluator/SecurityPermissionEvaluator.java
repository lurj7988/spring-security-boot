package com.original.security.user.evaluator;

import com.original.security.user.service.PermissionService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Spring Security 权限评估器实现
 *
 * <p>将 Spring Security 的 SpEL {@code hasPermission()} 表达式桥接到
 * {@link PermissionService} 服务，使 {@code @PreAuthorize("hasPermission(...)")}
 * 注解可正常工作。
 *
 * <p>目前实现基于用户全局权限进行判断，忽略 {@code targetDomainObject} 和
 * {@code targetId/targetType} 的具体值。若需要对象级细粒度权限控制，
 * 可在此类中进行扩展。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class SecurityPermissionEvaluator implements PermissionEvaluator {

    private final PermissionService permissionService;

    /**
     * 构造器注入 (AC 3.1)
     *
     * @param permissionService 权限服务
     */
    public SecurityPermissionEvaluator(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }

        String username = extractUsername(authentication);
        if (username == null) {
            return false;
        }

        return permissionService.hasPermission(username, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }

        String username = extractUsername(authentication);
        if (username == null) {
            return false;
        }

        return permissionService.hasPermission(username, permission.toString());
    }

    /**
     * 从 Authentication 中提取用户名
     */
    private String extractUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        if (principal instanceof String) {
            return (String) principal;
        }
        return authentication.getName();
    }
}
