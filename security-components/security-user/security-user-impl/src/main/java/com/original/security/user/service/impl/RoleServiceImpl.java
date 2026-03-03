package com.original.security.user.service.impl;

import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.repository.UserRepository;
import com.original.security.user.service.RoleService;
import org.springframework.lang.Nullable;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * <p>使用有界 LRU 缓存（最多 {@value #MAX_CACHE_SIZE} 个用户）存储角色信息，
 * 避免频繁查询数据库。当用户角色发生变更时，需调用 {@link #clearCache(String)}
 * 或 {@link #clearAllCache()} 使缓存失效。
 *
 * <p>支持通过可选的 {@link RoleHierarchy} Bean 进行角色继承解析（AC 2.1）。
 * 注意：缓存存储的是用户直接拥有的角色名称集合，{@link RoleHierarchy} 的继承解析
 * 在每次 {@link #hasRole} 调用时实时执行，因此运行时修改 {@code RoleHierarchy}
 * 无需清除此缓存。
 *
 * <p><b>缓存策略：</b> 仅缓存存在且已启用的用户的角色集合。
 * 用户不存在或已禁用时不缓存，以支持后续账户启用无需手动清除缓存。
 *
 * <p><b>线程安全：</b> 采用双重检查锁定（DCL）模式保证缓存填充的线程安全性。
 * DB 查询在锁外执行（避免锁竞争），put-if-absent 在 {@code synchronized(roleCache)}
 * 块内完成（保证原子性）。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Service
public class RoleServiceImpl implements RoleService {

    /** 最大缓存用户数，防止内存无限增长 */
    private static final int MAX_CACHE_SIZE = 1000;

    private final UserRepository userRepository;
    private final RoleHierarchy roleHierarchy;

    /**
     * 有界 LRU 角色缓存 (username -> 不可变角色名称集合)，最多缓存 MAX_CACHE_SIZE 个用户。
     * 使用 Collections.synchronizedMap 包装以保证单个 get/put 操作的线程安全性；
     * 复合的 get-or-load-and-put 操作通过 synchronized(roleCache) 块保证原子性。
     */
    private final Map<String, Set<String>> roleCache =
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
     * @param roleHierarchy  角色继承关系（可选，为 null 时禁用角色继承）
     */
    public RoleServiceImpl(UserRepository userRepository,
                           @Nullable RoleHierarchy roleHierarchy) {
        this.userRepository = userRepository;
        this.roleHierarchy = roleHierarchy;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(String username, String role) {
        if (username == null || role == null || username.trim().isEmpty() || role.trim().isEmpty()) {
            return false;
        }

        Set<String> userRoles = getOrLoadRoles(username);
        return userRoles != null && matchesRole(userRoles, role);
    }

    /**
     * 线程安全的缓存获取或加载方法（双重检查锁定模式）。
     *
     * <p>返回 null 表示用户不存在或已禁用（结果不缓存）。
     */
    private Set<String> getOrLoadRoles(String username) {
        // Step 1: 快速路径 — 检查缓存（synchronizedMap 保证单方法线程安全）
        Set<String> cached = roleCache.get(username);
        if (cached != null) {
            return cached;
        }

        // Step 2: 慢速路径 — 从数据库加载（在锁外执行，避免序列化所有角色检查）
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent() || !userOpt.get().isEnabled()) {
            // 用户不存在或已禁用：不缓存，确保账户启用后无需手动清除缓存
            return null;
        }
        Set<String> loaded = Collections.unmodifiableSet(loadRolesFromUser(userOpt.get()));

        // Step 3: 原子性 put-if-absent — 在锁内双重检查，防止并发写入
        synchronized (roleCache) {
            Set<String> existing = roleCache.get(username);
            if (existing == null) {
                roleCache.put(username, loaded);
                return loaded;
            }
            return existing;
        }
    }

    /**
     * 检查角色集合中是否包含指定角色（含继承关系匹配）。
     *
     * <p>注意：{@link RoleHierarchy} 的继承解析实时执行，不受角色缓存影响，
     * 因此修改角色层级关系无需清除角色缓存。
     */
    private boolean matchesRole(Set<String> userRoles, String role) {
        // 直接匹配 (AC 2.1)
        if (userRoles.contains(role)) {
            return true;
        }

        // 角色继承匹配 (AC 2.1)
        if (roleHierarchy != null) {
            Collection<GrantedAuthority> authorities = userRoles.stream()
                    .map(r -> new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r))
                    .collect(Collectors.toList());

            Collection<? extends GrantedAuthority> reachableAuthorities =
                    roleHierarchy.getReachableGrantedAuthorities(authorities);
            String searchRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

            return reachableAuthorities.stream()
                    .anyMatch(a -> a.getAuthority().equals(searchRole));
        }

        return false;
    }

    /**
     * 从 User 实体中提取所有角色名称 (User -> Roles)
     */
    private Set<String> loadRolesFromUser(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public void clearCache(String username) {
        if (username != null) {
            roleCache.remove(username);
        }
    }

    @Override
    public void clearAllCache() {
        roleCache.clear();
    }
}
