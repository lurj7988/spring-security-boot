package com.original.security.user.event;

import com.original.security.user.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 角色缓存失效监听器
 *
 * <p>在角色权限变更事务提交后清理角色缓存，确保后续查询读取到最新数据。
 * 与 {@link RolePermissionAssignedEventListener}（审计日志）分离，遵循单一职责原则。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class RoleCacheEvictionListener {

    private static final Logger log = LoggerFactory.getLogger(RoleCacheEvictionListener.class);

    private final RoleService roleService;

    public RoleCacheEvictionListener(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 事务提交后清理角色缓存。
     *
     * <p>使用 {@code AFTER_COMMIT} 确保缓存清理发生在数据库变更持久化之后，
     * 避免其他线程在事务提交前读到旧数据并写回缓存。
     *
     * @param event 角色权限分配事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPermissionAssigned(RolePermissionAssignedEvent event) {
        log.debug("Evicting role cache after permission assignment for role: {}", event.getRoleName());
        roleService.clearAllCache();
    }
}
