package com.original.security.user.service.impl;

import com.original.security.user.entity.Permission;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.repository.UserRepository;
import com.original.security.user.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 *
 * <p>使用有界 LRU 缓存（最多 {@value #MAX_CACHE_SIZE} 个用户）存储权限信息，
 * 避免频繁查询数据库。当用户权限发生变更时，需调用 {@link #clearCache(String)}
 * 或 {@link #clearAllCache()} 使缓存失效。
 *
 * <p><b>缓存策略：</b> 仅缓存存在且已启用的用户的权限集合。
 * 用户不存在或已禁用时不缓存，以支持后续账户启用无需手动清除缓存。
 *
 * <p><b>线程安全：</b> 采用双重检查锁定（DCL）模式保证缓存填充的线程安全性。
 * DB 查询在锁外执行（避免锁竞争），put-if-absent 在 {@code synchronized(permissionCache)}
 * 块内完成（保证原子性）。在极端并发场景下，同一用户至多触发一次重复 DB 查询（正确性不受影响）。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    /** 最大缓存用户数，防止内存无限增长 */
    private static final int MAX_CACHE_SIZE = 1000;

    private final UserRepository userRepository;

    /**
     * 有界 LRU 权限缓存 (username -> 不可变权限名称集合)，最多缓存 MAX_CACHE_SIZE 个用户。
     * 使用 Collections.synchronizedMap 包装以保证单个 get/put 操作的线程安全性；
     * 复合的 get-or-load-and-put 操作通过 synchronized(permissionCache) 块保证原子性。
     */
    private final Map<String, Set<String>> permissionCache =
            Collections.synchronizedMap(new LinkedHashMap<String, Set<String>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Set<String>> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    /**
     * 构造器注入 (AC 3.1 强制要求)
     *
     * @param userRepository 用户数据访问接口
     */
    public PermissionServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permission) {
        if (username == null || permission == null || username.trim().isEmpty() || permission.trim().isEmpty()) {
            return false;
        }

        Set<String> userPermissions = getOrLoadPermissions(username);
        return userPermissions != null && userPermissions.contains(permission);
    }

    /**
     * 线程安全的缓存获取或加载方法（双重检查锁定模式）。
     *
     * <p>返回 null 表示用户不存在或已禁用（结果不缓存）。
     */
    private Set<String> getOrLoadPermissions(String username) {
        // Step 1: 快速路径 — 检查缓存（synchronizedMap 保证单方法线程安全）
        Set<String> cached = permissionCache.get(username);
        if (cached != null) {
            return cached;
        }

        // Step 2: 慢速路径 — 从数据库加载（在锁外执行，避免序列化所有权限检查）(AC 1.1, 2.3)
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent() || !userOpt.get().isEnabled()) {
            // 用户不存在或已禁用：不缓存，确保账户启用后无需手动清除缓存
            return null;
        }
        Set<String> loaded = Collections.unmodifiableSet(loadPermissionsFromUser(userOpt.get()));

        // Step 3: 原子性 put-if-absent — 在锁内双重检查，防止并发写入（AC 1.2）
        // Collections.synchronizedMap 的监视器即为 permissionCache 本身
        synchronized (permissionCache) {
            Set<String> existing = permissionCache.get(username);
            if (existing == null) {
                permissionCache.put(username, loaded);
                return loaded;
            }
            // 另一线程已先行写入，使用已缓存的结果
            return existing;
        }
    }

    /**
     * 从 User 实体中提取所有权限名称 (User -> Roles -> Permissions)
     */
    private Set<String> loadPermissionsFromUser(User user) {
        Set<String> permissions = new HashSet<>();
        for (Role role : user.getRoles()) {
            Set<Permission> rolePermissions = role.getPermissions();
            if (rolePermissions != null) {
                permissions.addAll(rolePermissions.stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet()));
            }
        }
        return permissions;
    }

    @Override
    public void clearCache(String username) {
        if (username != null) {
            permissionCache.remove(username);
        }
    }

    @Override
    public void clearAllCache() {
        permissionCache.clear();
    }
}
