package com.original.security.core.authentication;

import com.original.security.core.authentication.impl.DefaultAuthenticationProvider;
import com.original.security.core.authentication.token.Token;
import com.original.security.core.authentication.token.SimpleToken;
import com.original.security.config.ConfigProvider;
import com.original.security.plugin.AuthenticationPlugin;
import com.original.security.plugin.impl.DefaultAuthenticationPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
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

        authenticationPlugin = new DefaultAuthenticationPlugin("jwt-authentication", authenticationProvider);
    }

    @Test
    void testAuthenticationPluginSupportsJwt() {
        assertTrue(authenticationPlugin.supports(UsernamePasswordAuthenticationToken.class));
        assertTrue(authenticationPlugin.supports(JwtAuthenticationToken.class));
    }

    @Test
    void testAuthenticationProviderValidateToken() {
        // 创建有效的 Token 实现
        Token token = new SimpleToken(
            "test-token",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "testuser",
            new String[]{"web"},
            Collections.emptyMap()
        );

        assertTrue(authenticationProvider.validateToken(token));
    }

    @Test
    void testAuthenticationProviderValidateNullToken() {
        assertFalse(authenticationProvider.validateToken(null));
    }

    @Test
    void testAuthenticationProviderRefreshToken() {
        // 创建有效的 Token
        when(mockConfigProvider.getConfig("security.token.expiration.hours", 1L))
            .thenReturn(1L);

        Token originalToken = new SimpleToken(
            "original-token",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "testuser",
            new String[]{"web"},
            Collections.emptyMap()
        );

        Token refreshedToken = authenticationProvider.refreshToken(originalToken);

        assertNotNull(refreshedToken);
        assertEquals("testuser", refreshedToken.getSubject());
    }

    @Test
    void testAuthenticationProviderRefreshExpiredToken() {
        // 创建过期的 Token
        Token expiredToken = new SimpleToken(
            "expired-token",
            "JWT",
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1),
            "system",
            "testuser",
            new String[]{"web"},
            Collections.emptyMap()
        );

        Token refreshedToken = authenticationProvider.refreshToken(expiredToken);

        assertNull(refreshedToken);
    }

    @Test
    void testTokenCreationWithClaims() {
        Map<String, Object> claims = Collections.singletonMap("role", "admin");

        Token token = new SimpleToken(
            "admin-token",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2),
            "system",
            "admin",
            new String[]{"web"},
            claims
        );

        assertEquals("admin", token.getSubject());
        assertEquals("admin", token.getClaims().get("role"));
    }

    @Test
    void testTokenExpiration() {
        Token token = new SimpleToken(
            "test",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().minusHours(1), // 已过期
            "system",
            "testuser",
            new String[]{"web"},
            Collections.emptyMap()
        );

        assertFalse(authenticationProvider.validateToken(token));
    }
}