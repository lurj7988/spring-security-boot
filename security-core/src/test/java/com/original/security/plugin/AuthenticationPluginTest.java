package com.original.security.plugin;

import com.original.security.core.authentication.AuthenticationProvider;
import com.original.security.core.authentication.JwtAuthenticationToken;
import com.original.security.core.authentication.impl.DefaultAuthenticationProvider;
import com.original.security.config.ConfigProvider;
import com.original.security.config.impl.DefaultConfigProvider;
import com.original.security.plugin.impl.DefaultAuthenticationPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthenticationPlugin 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationPluginTest {

    private AuthenticationPlugin authenticationPlugin;
    private AuthenticationProvider authenticationProvider;

    @Mock
    private AuthenticationProvider mockAuthenticationProvider;

    @BeforeEach
    void setUp() {
        ConfigProvider configProvider = new DefaultConfigProvider();
        authenticationProvider = new DefaultAuthenticationProvider(new BCryptPasswordEncoder(), configProvider);
        authenticationPlugin = new DefaultAuthenticationPlugin("test-plugin", authenticationProvider);
    }

    @Test
    void testGetName() {
        assertEquals("test-plugin", authenticationPlugin.getName());
    }

    @Test
    void testGetAuthenticationProvider() {
        assertNotNull(authenticationPlugin.getAuthenticationProvider());
        assertEquals(authenticationProvider, authenticationPlugin.getAuthenticationProvider());
    }

    @Test
    void testSupportsUsernamePasswordAuthenticationToken() {
        assertTrue(authenticationPlugin.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testSupportsJwtAuthenticationToken() {
        // 验证插件支持 JWT 认证类型
        assertTrue(authenticationPlugin.supports(JwtAuthenticationToken.class));
    }

    @Test
    void testSupportsNull() {
        assertFalse(authenticationPlugin.supports(null));
    }

    @Test
    void testSupportsUnsupportedType() {
        assertFalse(authenticationPlugin.supports(String.class));
    }

    @Test
    void testConstructorWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DefaultAuthenticationPlugin(null, authenticationProvider);
        });
    }

    @Test
    void testConstructorWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DefaultAuthenticationPlugin("", authenticationProvider);
        });
    }

    @Test
    void testConstructorWithNullProvider() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DefaultAuthenticationPlugin("test-plugin", null);
        });
    }

    @Test
    void testPluginCreation() {
        AuthenticationPlugin plugin = new DefaultAuthenticationPlugin(
            "jwt-authentication",
            mockAuthenticationProvider
        );

        assertEquals("jwt-authentication", plugin.getName());
        assertNotNull(plugin.getAuthenticationProvider());
    }

    @Test
    void testJwtAuthenticationTokenCreation() {
        // 测试实际创建和使用 JWT 认证令牌
        String jwtToken = "test.jwt.token";
        UserDetails userDetails = User.withUsername("testuser")
            .password("password")
            .roles("USER")
            .build();

        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwtToken, userDetails);

        assertEquals(jwtToken, jwtAuth.getToken());
        assertEquals(userDetails, jwtAuth.getPrincipal());
        assertTrue(jwtAuth.isAuthenticated());
        assertTrue(jwtAuth.isValid());
    }

    @Test
    void testJwtAuthenticationTokenWithoutUserDetails() {
        // 测试没有用户详情的 JWT 认证令牌
        String jwtToken = "test.jwt.token";
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwtToken);

        assertEquals(jwtToken, jwtAuth.getToken());
        assertNull(jwtAuth.getPrincipal());
        assertFalse(jwtAuth.isAuthenticated());
        assertFalse(jwtAuth.isValid());
    }
}