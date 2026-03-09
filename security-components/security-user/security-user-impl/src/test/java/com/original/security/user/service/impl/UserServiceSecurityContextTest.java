package com.original.security.user.service.impl;

import com.original.security.user.api.dto.request.PasswordChangeRequest;
import com.original.security.user.config.UserProperties;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.exception.InvalidPasswordException;
import com.original.security.user.exception.UserNotFoundException;
import com.original.security.user.notification.NotificationService;
import com.original.security.user.repository.RoleRepository;
import com.original.security.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceSecurityContextTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SessionRegistry sessionRegistry;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserProperties userProperties;

    @Mock
    private UserProperties.Password passwordConfig;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // 设置用户属性和密码配置的mock
        when(userProperties.getPassword()).thenReturn(passwordConfig);
        when(passwordConfig.getLength()).thenReturn(12);
        when(passwordConfig.getMinLength()).thenReturn(8);
        when(passwordConfig.getMaxLength()).thenReturn(50);
        when(passwordConfig.isIncludeUppercase()).thenReturn(true);
        when(passwordConfig.isIncludeLowercase()).thenReturn(true);
        when(passwordConfig.isIncludeNumbers()).thenReturn(true);
        when(passwordConfig.isIncludeSpecialChars()).thenReturn(true);
        when(passwordConfig.getSpecialCharacters()).thenReturn("!@#$%^&*");

        userService = new UserServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                eventPublisher,
                userProperties,
                sessionRegistry,
                notificationService
        );
    }

    @Test
    void testChangePassword_InvalidatesSessions() {
        // 准备测试数据
        String username = "testuser";
        String oldPassword = "oldPassword123!";
        String newPassword = "newPassword456@";
        User user = new User(1L, username, "encodedOldPassword", "test@example.com", true, null, Collections.emptySet());

        // 设置安全上下文
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 设置mock行为
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // 模拟会话信息
        SessionInformation sessionInfo1 = mock(SessionInformation.class);
        SessionInformation sessionInfo2 = mock(SessionInformation.class);
        when(sessionRegistry.getAllSessions(user, false)).thenReturn(
            java.util.Arrays.asList(sessionInfo1, sessionInfo2)
        );

        // 准备请求对象
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);

        // 执行测试
        assertDoesNotThrow(() -> userService.changePassword(request));

        // 验证会话被过期
        verify(sessionInfo1).expireNow();
        verify(sessionInfo2).expireNow();

        // 验证用户密码被更新
        verify(userRepository).save(any(User.class));

        // 验证通知服务被调用
        verify(notificationService).sendPasswordChangedNotification(user);

        // 清理安全上下文
        SecurityContextHolder.clearContext();
    }

    @Test
    void testResetPassword_InvalidatesSessions() {
        // 准备测试数据
        Long userId = 1L;
        String newPassword = "generatedPassword123!";
        User user = new User(userId, "testuser", "oldEncodedPassword", "test@example.com", true, null, Collections.emptySet());

        // 设置mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedGeneratedPassword");

        // 模拟会话信息
        SessionInformation sessionInfo1 = mock(SessionInformation.class);
        SessionInformation sessionInfo2 = mock(SessionInformation.class);
        when(sessionRegistry.getAllSessions(user, false)).thenReturn(
            java.util.Arrays.asList(sessionInfo1, sessionInfo2)
        );

        // 执行测试
        String result = assertDoesNotThrow(() -> userService.resetPassword(userId));

        // 验证会话被过期
        verify(sessionInfo1).expireNow();
        verify(sessionInfo2).expireNow();

        // 验证用户密码被更新
        verify(userRepository).save(any(User.class));

        // 验证通知服务被调用
        verify(notificationService).sendPasswordResetNotification(any(User.class), anyString());

        // 验证返回的密码（在实际实现中，这个返回值会被忽略，因为我们不再在响应中返回密码）
        assertNotNull(result);
    }
}