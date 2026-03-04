package com.original.security.user.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.original.security.config.SecurityProperties;
import com.original.security.user.entity.Permission;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.repository.UserRepository;
import com.original.security.user.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;

    /**
     * 用户权限缓存
     */
    private final Cache<String, Set<String>> permissionCache;

    public PermissionServiceImpl(UserRepository userRepository, SecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.securityProperties = securityProperties;
        
        SecurityProperties.Cache cacheConfig = securityProperties.getCache();
        this.permissionCache = Caffeine.newBuilder()
                .maximumSize(cacheConfig.getMaximumSize())
                .expireAfterWrite(cacheConfig.getTtlMinutes(), java.util.concurrent.TimeUnit.MINUTES)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permission) {
        if (username == null || permission == null || username.trim().isEmpty() || permission.trim().isEmpty()) {
            return false;
        }

        Set<String> userPermissions = getOrLoadPermissions(username);
        return userPermissions.contains(permission);
    }

    private Set<String> getOrLoadPermissions(String username) {
        return permissionCache.get(username, key -> {
            Optional<User> userOpt = userRepository.findByUsername(key);
            // 负向缓存：如果用户不存在或已禁用，返回一个空集（不可变），而不是 null。
            // Caffeine 会缓存此空集，从而避免频繁查库。
            if (!userOpt.isPresent() || !userOpt.get().isEnabled()) {
                return Collections.emptySet();
            }
            return Collections.unmodifiableSet(loadPermissionsFromUser(userOpt.get()));
        });
    }

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
            permissionCache.invalidate(username);
        }
    }

    @Override
    public void clearAllCache() {
        permissionCache.invalidateAll();
    }
}