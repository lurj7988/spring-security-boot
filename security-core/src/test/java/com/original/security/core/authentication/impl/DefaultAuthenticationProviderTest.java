package com.original.security.core.authentication.impl;

import com.original.security.core.authentication.*;
import com.original.security.core.authentication.token.Token;
import com.original.security.core.authentication.token.SimpleToken;
import com.original.security.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DefaultAuthenticationProvider 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultAuthenticationProviderTest {

    private DefaultAuthenticationProvider authenticationProvider;

    @Mock
    private ConfigProvider mockConfigProvider;

    @BeforeEach
    void setUp() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        authenticationProvider = new DefaultAuthenticationProvider(passwordEncoder, mockConfigProvider);
    }

    @Test
    void testAuthenticateWithValidCredentials() throws AuthenticationException {
        // Given
        String username = "admin";
        String password = "password123";

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        // When
        AuthenticationResult result = authenticationProvider.authenticate(credentials, "username-password");

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(username, result.getUser().getUsername());
    }

    @Test
    void testAuthenticateWithInvalidCredentialsFormat() {
        // Given
        Integer invalidCredentials = 123; // Not a Map type

        // When
        AuthenticationResult result = authenticationProvider.authenticate(invalidCredentials, "username-password");

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid credentials format", result.getErrorMessage());
    }

    @Test
    void testAuthenticateWithMissingCredentials() throws AuthenticationException {
        // Given
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", "admin");
        // Missing password

        // When
        AuthenticationResult result = authenticationProvider.authenticate(credentials, "username-password");

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Username and password are required", result.getErrorMessage());
    }

    @Test
    void testAuthenticateWithUsernameAndPassword() throws AuthenticationException {
        // Given
        String username = "admin";
        String password = "password123";

        // When
        AuthenticationResult result = authenticationProvider.authenticate(username, password);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(username, result.getUser().getUsername());
        assertEquals("系统管理员", result.getUser().getDisplayName());
    }

    @Test
    void testAuthenticateWithNonExistentUser() throws AuthenticationException {
        // Given
        String username = "nonexistent";

        // When
        AuthenticationResult result = authenticationProvider.authenticate(username, "password");

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("User not found"));
    }

    @Test
    void testValidateToken() {
        // Given
        Token token = new SimpleToken(
            "valid-token",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "admin",
            new String[]{"web"},
            Collections.emptyMap()
        );

        // When
        boolean isValid = authenticationProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateNullToken() {
        // When
        boolean isValid = authenticationProvider.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testRefreshToken() {
        // Given
        when(mockConfigProvider.getConfig("security.token.expiration.hours", 1L))
            .thenReturn(1L);

        Token originalToken = new SimpleToken(
            "valid-token",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "admin",
            new String[]{"web"},
            Collections.emptyMap()
        );

        // When
        Token refreshedToken = authenticationProvider.refreshToken(originalToken);

        // Then
        assertNotNull(refreshedToken);
        assertEquals("admin", refreshedToken.getSubject());
    }

    @Test
    void testRefreshInvalidToken() {
        // Given
        Token invalidToken = new SimpleToken(
            "expired-token",
            "JWT",
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1),
            "system",
            "admin",
            new String[]{"web"},
            Collections.emptyMap()
        );

        // When
        Token refreshedToken = authenticationProvider.refreshToken(invalidToken);

        // Then
        assertNull(refreshedToken);
    }

    @Test
    void testLoadUserByUsername() throws AuthenticationException {
        // Given
        String username = "admin";

        // When
        UserDetails userDetails = authenticationProvider.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadNonExistentUser() {
        // Given
        String username = "nonexistent";

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationProvider.loadUserByUsername(username);
        });
    }

    @Test
    void testAuthenticationWithDetails() throws AuthenticationException {
        // Given
        String username = "admin";
        String password = "password123";

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        // When
        AuthenticationResult result = authenticationProvider.authenticate(credentials, "username-password");

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getDetails());
        assertEquals("unknown", result.getDetails().get("ip"));
        assertTrue(result.getDetails().containsKey("loginTime"));
    }

    @Test
    void testTokenExpirationConfig() {
        // Given
        long expectedHours = 2;
        when(mockConfigProvider.getConfig("security.token.expiration.hours", 1L))
            .thenReturn(expectedHours);

        // When
        Token token = authenticationProvider.refreshToken(new SimpleToken(
            "test",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "test",
            new String[]{"web"},
            Collections.emptyMap()
        ));

        // Then
        assertNotNull(token);
        // The expiration should be 2 hours from now
        LocalDateTime expectedExpiration = LocalDateTime.now().plusHours(expectedHours);
        assertTrue(Math.abs(token.getExpiresAt().getHour() - expectedExpiration.getHour()) <= 1);
    }

    @Test
    void testDefaultTokenExpiration() {
        // Given - no config provided, should use default
        when(mockConfigProvider.getConfig("security.token.expiration.hours", 1L))
            .thenReturn(1L);

        // When
        Token token = authenticationProvider.refreshToken(new SimpleToken(
            "test",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "test",
            new String[]{"web"},
            Collections.emptyMap()
        ));

        // Then
        assertNotNull(token);
        // Should use default 1 hour
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void testUserRolesAndPermissions() throws AuthenticationException {
        // Given
        String username = "admin";

        // When
        UserDetails userDetails = authenticationProvider.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testRegularUserRoles() throws AuthenticationException {
        // Given
        String username = "user";

        // When
        UserDetails userDetails = authenticationProvider.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }
}