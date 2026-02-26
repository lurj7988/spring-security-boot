package com.original.security.core.authentication;

import com.original.security.core.authentication.JwtAuthenticationToken;
import com.original.security.core.authentication.impl.DefaultAuthenticationProvider;
import com.original.security.config.ConfigProvider;
import com.original.security.plugin.AuthenticationPlugin;
import com.original.security.plugin.impl.DefaultAuthenticationPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JWT 认证测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationTest {

    private AuthenticationPlugin authenticationPlugin;
    private AuthenticationProvider authenticationProvider;

    @Mock
    private ConfigProvider mockConfigProvider;

    @BeforeEach
    void setUp() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        authenticationProvider = new DefaultAuthenticationProvider(passwordEncoder, mockConfigProvider);

        // Mock 配置返回
        when(mockConfigProvider.getConfig(anyString(), any())).thenReturn(Optional.empty());

        authenticationPlugin = new DefaultAuthenticationPlugin("jwt-authentication", authenticationProvider);
    }

    @Test
    void testJwtAuthenticationCreation() {
        // 创建有效的用户详情
        User user = User.builder()
            .username("testuser")
            .password("password")
            .authorities("ROLE_USER")
            .build();

        // 创建 JWT 认证令牌
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTIwMDAwMDAwMDAwLCJpYXQiOjE2MDAwMDAwMDAwMH0";
        JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(token, user);

        assertTrue(jwtToken.isAuthenticated());
        assertEquals(token, jwtToken.getCredentials());
        assertEquals(user, jwtToken.getPrincipal());
    }

    @Test
    void testJwtAuthenticationWithoutUserDetails() {
        // 创建不带用户详情的 JWT 令牌
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTIwMDAwMDAwMDAwLCJpYXQiOjE2MDAwMDAwMDAwMH0";
        JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(token);

        assertFalse(jwtToken.isAuthenticated());
        assertNull(jwtToken.getPrincipal());
        assertEquals(token, jwtToken.getCredentials());
    }

    @Test
    void testAuthenticationPluginSupportsJwt() {
        assertTrue(authenticationPlugin.supports(UsernamePasswordAuthenticationToken.class));
        assertTrue(authenticationPlugin.supports(JwtAuthenticationToken.class));
    }

    @Test
    void testAuthenticationProviderValidateToken() {
        // 创建有效的 JWT 令牌
        User user = User.builder()
            .username("testuser")
            .password("password")
            .authorities("ROLE_USER")
            .build();

        JwtAuthenticationToken token = new JwtAuthenticationToken("valid-token", user);
        assertTrue(authenticationProvider.validateToken(token));
    }

    @Test
    void testAuthenticationProviderValidateNullToken() {
        assertFalse(authenticationProvider.validateToken(null));
    }

    @Test
    void testAuthenticationProviderRefreshToken() {
        // 创建有效的 JWT 令牌
        User user = User.builder()
            .username("testuser")
            .password("password")
            .authorities("ROLE_USER")
            .build();

        JwtAuthenticationToken originalToken = new JwtAuthenticationToken("valid-token", user);
        Token refreshedToken = authenticationProvider.refreshToken(originalToken);

        assertNotNull(refreshedToken);
        assertEquals("testuser", refreshedToken.getSubject());
    }
}