package com.original.security.user.event;

import com.original.security.user.service.PermissionService;
import com.original.security.user.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 角色缓存失效监听器
 *
 * <p>在角色权限变更事务提交后清理角色缓存和权限缓存，确保后续查询读取到最新数据。
 * 与 {@link RolePermissionAssignedEventListener}（审计日志）分离，遵循单一职责原则。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class RoleCacheEvictionListener {

    private static final Logger log = LoggerFactory.getLogger(RoleCacheEvictionListener.class);

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final com.original.security.user.repository.UserRepository userRepository;

    public RoleCacheEvictionListener(RoleService roleService,
                                     PermissionService permissionService,
                                     com.original.security.user.repository.UserRepository userRepository) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.userRepository = userRepository;
    }

    /**
     * 事务提交后清理受影响用户的权限/角色缓存。
     *
     * <p>不再调用 clearAllCache() 以避免 Thunder Herd 效益。
     * 只查找并清理属于该角色的特定用户的缓存。
     *
     * @param event 角色权限分配事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPermissionAssigned(RolePermissionAssignedEvent event) {
        String roleName = event.getRoleName();
        log.debug("Finding users affected by role: {} for granular cache eviction", roleName);
        
        java.util.List<com.original.security.user.entity.User> affectedUsers = userRepository.findByRoles_Name(roleName);
        
        for (com.original.security.user.entity.User user : affectedUsers) {
            String username = user.getUsername();
            log.trace("Evicting cache for user: {}", username);
            roleService.clearCache(username);
            permissionService.clearCache(username);
        }
        
        log.debug("Granular eviction complete. {} users affected.", affectedUsers.size());
    }
}
