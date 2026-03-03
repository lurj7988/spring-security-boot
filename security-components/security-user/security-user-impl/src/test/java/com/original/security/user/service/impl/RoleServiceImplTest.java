package com.original.security.user.service.impl;

import com.original.security.config.SecurityProperties;
import com.original.security.user.api.dto.request.PermissionAssignRequest;
import com.original.security.user.api.dto.request.RoleCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.RoleDTO;
import com.original.security.user.entity.Permission;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.event.RolePermissionAssignedEvent;
import com.original.security.user.repository.PermissionRepository;
import com.original.security.user.repository.RoleRepository;
import com.original.security.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RoleHierarchy roleHierarchy;

    @Mock
    private SecurityProperties securityProperties;

    private RoleServiceImpl roleServiceWithHierarchy;
    private RoleServiceImpl roleServiceWithoutHierarchy;

    @BeforeEach
    void setUp() {
        when(securityProperties.getCache()).thenReturn(new SecurityProperties.Cache());
        roleServiceWithHierarchy = new RoleServiceImpl(userRepository, roleRepository, permissionRepository, eventPublisher, securityProperties, roleHierarchy);
        roleServiceWithoutHierarchy = new RoleServiceImpl(userRepository, roleRepository, permissionRepository, eventPublisher, securityProperties, null);
    }

    // NEW-MEDIUM-2: 所有测试方法遵循 test{MethodName}_{Scenario}_{ExpectedResult} 命名约定

    @Test
    void testHasRole_UserHasDirectRole_ReturnsTrue() {
        User user = new User("admin", "pwd", "admin@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        user.addRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertTrue(roleServiceWithoutHierarchy.hasRole("admin", "ADMIN"));
        // 验证缓存生效：第二次调用不应再查询 DB
        assertTrue(roleServiceWithoutHierarchy.hasRole("admin", "ADMIN"));
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    void testHasRole_UserDoesNotHaveRole_ReturnsFalse() {
        User user = new User("user", "pwd", "user@test.com");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        assertFalse(roleServiceWithoutHierarchy.hasRole("user", "ADMIN"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHasRole_UserHasInheritedRole_ReturnsTrue() {
        User user = new User("admin", "pwd", "admin@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        user.addRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(roleHierarchy).getReachableGrantedAuthorities(any());

        assertTrue(roleServiceWithHierarchy.hasRole("admin", "USER"));
    }

    @Test
    void testCreateRole_UniqueRoleName_ReturnsCreatedRole() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("NEW_ROLE");
        request.setDescription("Desc");

        when(roleRepository.findByName("NEW_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        RoleDTO result = roleServiceWithoutHierarchy.createRole(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("NEW_ROLE", result.getName());
        assertEquals("Desc", result.getDescription());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testCreateRole_DuplicateRoleName_ThrowsException() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("EXISTING_ROLE");

        when(roleRepository.findByName("EXISTING_ROLE")).thenReturn(Optional.of(new Role("EXISTING_ROLE", "")));

        assertThrows(IllegalArgumentException.class, () -> {
            roleServiceWithoutHierarchy.createRole(request);
        });
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testAssignPermissions_ValidPermissions_AppendsToPreviousAndPublishesEvent() {
        Role role = new Role("TEST_ROLE", "Desc");
        role.setId(1L);
        // 角色已有权限 P_EXISTING，验证增量追加不会覆盖
        Permission existing = new Permission("P_EXISTING", "Existing");
        existing.setId(99L);
        role.getPermissions().add(existing);

        Permission p1 = new Permission("P1", "D1");
        p1.setId(10L);

        PermissionAssignRequest request = new PermissionAssignRequest();
        request.setPermissionIds(Collections.singletonList(10L));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(request.getPermissionIds())).thenReturn(Collections.singletonList(p1));

        roleServiceWithoutHierarchy.assignPermissions(1L, request);

        verify(roleRepository).save(role);
        // 验证增量追加：新权限已添加，原有权限保留
        assertEquals(2, role.getPermissions().size());
        assertTrue(role.getPermissions().stream().anyMatch(p -> p.getName().equals("P1")));
        assertTrue(role.getPermissions().stream().anyMatch(p -> p.getName().equals("P_EXISTING")));

        // 验证事件发布（缓存清理由 @TransactionalEventListener 处理，不在此验证）
        ArgumentCaptor<RolePermissionAssignedEvent> eventCaptor = ArgumentCaptor.forClass(RolePermissionAssignedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        RolePermissionAssignedEvent event = eventCaptor.getValue();
        assertEquals("TEST_ROLE", event.getRoleName());
        assertEquals(1, event.getPermissionIds().size());
        assertEquals(10L, event.getPermissionIds().get(0));
    }

    @Test
    void testAssignPermissions_DuplicatePermissionIds_DeduplicatesAndSucceeds() {
        // NEW-MEDIUM-1: 重复 ID 应被去重，不应误触 "Permission IDs not found" 异常
        Role role = new Role("TEST_ROLE", "Desc");
        role.setId(1L);

        Permission p1 = new Permission("P1", "D1");
        p1.setId(10L);

        PermissionAssignRequest request = new PermissionAssignRequest();
        request.setPermissionIds(Arrays.asList(10L, 10L)); // 重复 ID

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        // findAllById 对重复 ID 只返回一个实体
        when(permissionRepository.findAllById(any())).thenReturn(Collections.singletonList(p1));

        // 去重后 requestedIds = [10L]，findAllById 返回 1 个，size 匹配，不应抛异常
        assertDoesNotThrow(() -> roleServiceWithoutHierarchy.assignPermissions(1L, request));
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    void testAssignPermissions_PermissionIdsNotFound_ThrowsException() {
        // 请求的 permissionId 部分不存在时，应抛出异常而非静默忽略
        Role role = new Role("TEST_ROLE", "Desc");
        role.setId(1L);

        PermissionAssignRequest request = new PermissionAssignRequest();
        request.setPermissionIds(Arrays.asList(10L, 999L)); // 999L 不存在

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        Permission p1 = new Permission("P1", "D1");
        p1.setId(10L);
        when(permissionRepository.findAllById(any())).thenReturn(Collections.singletonList(p1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                roleServiceWithoutHierarchy.assignPermissions(1L, request));
        assertTrue(ex.getMessage().contains("999"));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testGetRole_ExistingRole_ReturnsRoleDetails() {
        Role role = new Role("TEST_ROLE", "Desc");
        role.setId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleDTO result = roleServiceWithoutHierarchy.getRole(1L);

        assertNotNull(result);
        assertEquals("TEST_ROLE", result.getName());
        assertEquals("Desc", result.getDescription());
    }

    @Test
    void testListRoles_ValidPagination_ReturnsPaginatedResults() {
        Role r1 = new Role("R1", "D1");
        r1.setId(1L);
        Role r2 = new Role("R2", "D2");
        r2.setId(2L);
        Page<Role> mockPage = new PageImpl<>(Arrays.asList(r1, r2), PageRequest.of(0, 10), 2);

        when(roleRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        PageDTO<RoleDTO> result = roleServiceWithoutHierarchy.listRoles(0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("R1", result.getContent().get(0).getName());
        assertEquals("R2", result.getContent().get(1).getName());
    }

    @Test
    void testListRoles_NegativePage_ThrowsException() {
        // R3-MEDIUM-2: 负数 page 应返回清晰错误，而非穿透到 Spring Data 抛 500
        assertThrows(IllegalArgumentException.class, () ->
                roleServiceWithoutHierarchy.listRoles(-1, 10));
    }

    @Test
    void testListRoles_SizeExceedsMax_ThrowsException() {
        // R3-MEDIUM-2: size 超过 100 应拒绝，防止全量加载
        assertThrows(IllegalArgumentException.class, () ->
                roleServiceWithoutHierarchy.listRoles(0, 200));
    }

    @Test
    void testListRoles_ZeroSize_ThrowsException() {
        // R3-MEDIUM-2: size=0 应拒绝
        assertThrows(IllegalArgumentException.class, () ->
                roleServiceWithoutHierarchy.listRoles(0, 0));
    }
}