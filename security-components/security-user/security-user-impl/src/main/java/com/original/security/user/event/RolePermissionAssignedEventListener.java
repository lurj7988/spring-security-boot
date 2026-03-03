package com.original.security.user.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 角色权限分配事件监听器
 *
 * <p>监听 {@link RolePermissionAssignedEvent}，记录审计日志，满足 FR15 要求。
 * 使用异步处理避免阻塞业务主线程；如未来引入持久化审计日志表，可在此扩展。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class RolePermissionAssignedEventListener {

    private static final Logger log = LoggerFactory.getLogger(RolePermissionAssignedEventListener.class);

    /**
     * 处理角色权限分配事件，记录审计日志（FR15）。
     *
     * @param event 角色权限分配事件
     */
    @EventListener
    @Async
    public void onRolePermissionAssigned(RolePermissionAssignedEvent event) {
        log.info("[AUDIT] Role permission assignment: roleName={}, permissionIds={}, timestamp={}",
                event.getRoleName(),
                event.getPermissionIds(),
                event.getTimestamp());
    }
}
