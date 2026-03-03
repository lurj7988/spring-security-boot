package com.original.security.user.service.impl;

import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
    private RoleHierarchy roleHierarchy;

    private RoleServiceImpl roleServiceWithHierarchy;
    private RoleServiceImpl roleServiceWithoutHierarchy;

    @BeforeEach
    void setUp() {
        roleServiceWithHierarchy = new RoleServiceImpl(userRepository, roleHierarchy);
        roleServiceWithoutHierarchy = new RoleServiceImpl(userRepository, null);
    }

    @Test
    void hasRole_ShouldReturnTrue_WhenUserHasDirectRole() {
        User user = new User("admin", "pwd", "admin@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        user.addRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertTrue(roleServiceWithoutHierarchy.hasRole("admin", "ADMIN"));
        // 测试缓存：第二次调用不再查询数据库
        assertTrue(roleServiceWithoutHierarchy.hasRole("admin", "ADMIN"));
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenUserDoesNotHaveRole() {
        User user = new User("user", "pwd", "user@test.com");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        assertFalse(roleServiceWithoutHierarchy.hasRole("user", "ADMIN"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasRole_ShouldReturnTrue_WhenUserHasInheritedRole() {
        User user = new User("admin", "pwd", "admin@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        user.addRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // Mock role hierarchy resolving: ADMIN inherits USER
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(roleHierarchy).getReachableGrantedAuthorities(any());

        assertTrue(roleServiceWithHierarchy.hasRole("admin", "USER"));
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenInputsAreInvalid() {
        assertFalse(roleServiceWithoutHierarchy.hasRole(null, "ADMIN"));
        assertFalse(roleServiceWithoutHierarchy.hasRole("admin", null));
        assertFalse(roleServiceWithoutHierarchy.hasRole("", ""));
    }

    @Test
    void hasRole_ShouldReturnFalse_WhenUserIsDisabled() {
        User user = new User("disabled", "pwd", "disabled@test.com");
        user.setEnabled(false);
        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(user));

        assertFalse(roleServiceWithoutHierarchy.hasRole("disabled", "ADMIN"));
    }

    @Test
    void hasRole_ShouldNotCacheDisabledUser_SoSubsequentCallsRecheck() {
        // 禁用用户不应被缓存，以便账户启用后能正常授权
        User disabledUser = new User("target", "pwd", "target@test.com");
        disabledUser.setEnabled(false);
        when(userRepository.findByUsername("target")).thenReturn(Optional.of(disabledUser));

        assertFalse(roleServiceWithoutHierarchy.hasRole("target", "ADMIN"));

        // 模拟账户被启用
        User enabledUser = new User("target", "pwd", "target@test.com");
        Role role = new Role("ADMIN", "Admin Role");
        enabledUser.addRole(role);
        when(userRepository.findByUsername("target")).thenReturn(Optional.of(enabledUser));

        // 若禁用状态被缓存，这里会错误地返回 false
        assertTrue(roleServiceWithoutHierarchy.hasRole("target", "ADMIN"));
        // 两次都查了数据库，说明禁用时结果未被缓存
        verify(userRepository, times(2)).findByUsername("target");
    }

    @Test
    void clearCache_ShouldRemoveUserFromCache() {
        User user = new User("admin", "pwd", "admin@test.com");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertFalse(roleServiceWithoutHierarchy.hasRole("admin", "ADMIN"));
        roleServiceWithoutHierarchy.clearCache("admin");
        assertFalse(roleServiceWithoutHierarchy.hasRole("admin", "ADMIN"));

        verify(userRepository, times(2)).findByUsername("admin");
    }
}
