package com.original.security.user.event;

import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 角色权限分配事件（FR15 审计日志触发源）
 */
public class RolePermissionAssignedEvent extends ApplicationEvent {

    private final String roleName;
    private final List<Long> permissionIds;
    private final LocalDateTime assignedAt;

    public RolePermissionAssignedEvent(Object source, String roleName, List<Long> permissionIds) {
        super(source);
        this.roleName = roleName;
        this.permissionIds = Collections.unmodifiableList(permissionIds);
        this.assignedAt = LocalDateTime.now();
    }

    public String getRoleName() {
        return roleName;
    }

    /** 返回不可变权限 ID 列表，防止外部篡改事件状态 */
    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}