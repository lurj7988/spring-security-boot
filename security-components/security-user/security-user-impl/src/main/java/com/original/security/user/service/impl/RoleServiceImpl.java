package com.original.security.user.service.impl;

import com.original.security.user.api.dto.request.PermissionAssignRequest;
import com.original.security.user.api.dto.request.RoleCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.PermissionDTO;
import com.original.security.user.api.dto.response.RoleDTO;
import com.original.security.user.entity.Permission;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.event.RolePermissionAssignedEvent;
import com.original.security.user.repository.PermissionRepository;
import com.original.security.user.repository.RoleRepository;
import com.original.security.user.repository.UserRepository;
import com.original.security.user.service.RoleService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role service implementation
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final int MAX_CACHE_SIZE = 1000;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleHierarchy roleHierarchy;
    private final ApplicationEventPublisher eventPublisher;

    private final Map<String, Set<String>> roleCache =
            Collections.synchronizedMap(new LinkedHashMap<String, Set<String>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Set<String>> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });

    public RoleServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           ApplicationEventPublisher eventPublisher,
                           @Nullable RoleHierarchy roleHierarchy) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.eventPublisher = eventPublisher;
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

    private Set<String> getOrLoadRoles(String username) {
        Set<String> cached = roleCache.get(username);
        if (cached != null) {
            return cached;
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent() || !userOpt.get().isEnabled()) {
            return null;
        }
        Set<String> loaded = Collections.unmodifiableSet(loadRolesFromUser(userOpt.get()));

        synchronized (roleCache) {
            Set<String> existing = roleCache.get(username);
            if (existing == null) {
                roleCache.put(username, loaded);
                return loaded;
            }
            return existing;
        }
    }

    private boolean matchesRole(Set<String> userRoles, String role) {
        if (userRoles.contains(role)) {
            return true;
        }

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

    @Override
    @Transactional
    public RoleDTO createRole(RoleCreateRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Role name already exists: " + request.getName());
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        // createdAt 由 Role 实体的 @PrePersist 钩子自动设置，此处无需手动赋值（MEDIUM-4）
        role = roleRepository.save(role);
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, PermissionAssignRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        // NEW-MEDIUM-1: 先对 permissionIds 去重，防止重复 ID 导致 size 比较误判
        List<Long> requestedIds = request.getPermissionIds().stream()
                .distinct()
                .collect(Collectors.toList());

        // 校验请求的 permissionIds 是否全部存在于数据库，防止静默忽略无效 ID
        List<Permission> foundPermissions = new ArrayList<>(permissionRepository.findAllById(requestedIds));
        if (foundPermissions.size() != requestedIds.size()) {
            Set<Long> foundIds = foundPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());
            List<Long> missingIds = requestedIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("Permission IDs not found: " + missingIds);
        }

        // 增量追加权限，而非全量替换，保留角色已有权限
        Set<Permission> permissionsToAdd = new HashSet<>(foundPermissions);
        role.getPermissions().addAll(permissionsToAdd);
        roleRepository.save(role);

        // NEW-HIGH-2: 缓存清理和事件发布必须在事务提交后执行，
        // 防止其他线程在事务提交前读回未提交的旧数据并刷新缓存（数据竞争）。
        // 通过发布内部事件，由 @TransactionalEventListener(AFTER_COMMIT) 处理后置逻辑。
        eventPublisher.publishEvent(new RolePermissionAssignedEvent(this, role.getName(), requestedIds));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        return convertToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<RoleDTO> listRoles(int page, int size) {
        // R3-MEDIUM-2: 分页参数边界校验，防止无效输入穿透至 Spring Data 抛出不友好的 500 异常
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero, got: " + page);
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100, got: " + size);
        }
        Page<Role> rolePage = roleRepository.findAll(PageRequest.of(page, size));
        List<RoleDTO> content = rolePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageDTO<>(content, rolePage.getTotalElements(), rolePage.getTotalPages(), 
                             rolePage.getSize(), rolePage.getNumber());
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setCreatedAt(role.getCreatedAt());
        if (role.getPermissions() != null) {
            List<PermissionDTO> pDtos = role.getPermissions().stream().map(p -> {
                PermissionDTO pd = new PermissionDTO();
                pd.setId(p.getId());
                pd.setName(p.getName());
                pd.setDescription(p.getDescription());
                pd.setCreatedAt(p.getCreatedAt());
                return pd;
            }).collect(Collectors.toList());
            dto.setPermissions(pDtos);
        }
        return dto;
    }
}