package com.original.security.user.service.impl;

import com.original.security.user.entity.Permission;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(userRepository);
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenUserHasPermission() {
        User user = new User("admin", "pwd", "admin@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        Permission perm = new Permission("user:read", "Read User");
        role.addPermission(perm);
        user.addRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertTrue(permissionService.hasPermission("admin", "user:read"));

        // 验证缓存生效，再次调用不会访问数据库
        assertTrue(permissionService.hasPermission("admin", "user:read"));
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUserDoesNotHavePermission() {
        User user = new User("user", "pwd", "user@test.com");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        assertFalse(permissionService.hasPermission("user", "user:read"));
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUserIsDisabled() {
        User user = new User("disabled_user", "pwd", "user@test.com");
        user.setEnabled(false);
        when(userRepository.findByUsername("disabled_user")).thenReturn(Optional.of(user));

        assertFalse(permissionService.hasPermission("disabled_user", "user:read"));
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertFalse(permissionService.hasPermission("unknown", "user:read"));
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenInputsAreInvalid() {
        assertFalse(permissionService.hasPermission(null, "user:read"));
        assertFalse(permissionService.hasPermission("user", null));
        assertFalse(permissionService.hasPermission("", ""));
    }

    @Test
    void clearCache_ShouldRemoveUserFromCache() {
        User user = new User("admin", "pwd", "admin@test.com");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertFalse(permissionService.hasPermission("admin", "user:read"));

        permissionService.clearCache("admin");
        assertFalse(permissionService.hasPermission("admin", "user:read"));

        verify(userRepository, times(2)).findByUsername("admin");
    }

    @Test
    void hasPermission_ShouldNotCacheDisabledUser_SoSubsequentCallsRecheck() {
        // 禁用用户不应被缓存，以便账户启用后能正常授权
        User disabledUser = new User("target", "pwd", "target@test.com");
        disabledUser.setEnabled(false);
        when(userRepository.findByUsername("target")).thenReturn(Optional.of(disabledUser));

        assertFalse(permissionService.hasPermission("target", "user:read"));

        // 模拟账户被启用
        User enabledUser = new User("target", "pwd", "target@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        Permission perm = new Permission("user:read", "Read User");
        role.addPermission(perm);
        enabledUser.addRole(role);
        when(userRepository.findByUsername("target")).thenReturn(Optional.of(enabledUser));

        // 若禁用状态被缓存，这里会错误地返回 false
        assertTrue(permissionService.hasPermission("target", "user:read"));
        // 两次都查了数据库，说明禁用时结果未被缓存
        verify(userRepository, times(2)).findByUsername("target");
    }

    @Test
    void clearAllCache_ShouldForceReloadForAllUsers() {
        User user1 = new User("user1", "pwd", "user1@test.com");
        User user2 = new User("user2", "pwd", "user2@test.com");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));

        permissionService.hasPermission("user1", "x");
        permissionService.hasPermission("user2", "x");

        permissionService.clearAllCache();

        permissionService.hasPermission("user1", "x");
        permissionService.hasPermission("user2", "x");

        // After clearing, DB should be queried again for both users
        verify(userRepository, times(2)).findByUsername("user1");
        verify(userRepository, times(2)).findByUsername("user2");
    }

    @Test
    void hasPermission_CachedResultShouldBeReusedWithoutDbQuery() {
        // 验证双重检查锁定（DCL）：缓存填充后的后续调用应直接返回缓存结果，不再查询 DB
        User user = new User("concurrent_user", "pwd", "cu@test.com");
        Role role = new Role("USER", "User Role");
        Permission perm = new Permission("data:read", "Read Data");
        role.addPermission(perm);
        user.addRole(role);
        when(userRepository.findByUsername("concurrent_user")).thenReturn(Optional.of(user));

        // 第一次调用：缓存未命中，查 DB 并填充缓存
        assertTrue(permissionService.hasPermission("concurrent_user", "data:read"));
        // 后续多次调用：均命中缓存，不再访问 DB
        assertTrue(permissionService.hasPermission("concurrent_user", "data:read"));
        assertFalse(permissionService.hasPermission("concurrent_user", "data:delete"));
        assertTrue(permissionService.hasPermission("concurrent_user", "data:read"));

        // DB 应只被查询一次
        verify(userRepository, times(1)).findByUsername("concurrent_user");
    }
}
