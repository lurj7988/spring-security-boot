package com.original.security.core.authentication.impl;

import com.original.security.core.authentication.*;
import com.original.security.core.authentication.user.SecurityUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * DefaultAuthenticationProvider 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
public class DefaultAuthenticationProviderTest {

    private DefaultAuthenticationProvider authenticationProvider;
    private PasswordEncoder passwordEncoder;

    @Before
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationProvider = new DefaultAuthenticationProvider(passwordEncoder);
    }

    @Test
    public void testAuthenticateWithValidCredentials() throws AuthenticationException {
        // Given
        String username = "admin";
        String password = "password123";

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        // When
        AuthenticationResult result = authenticationProvider.authenticate(credentials, "username-password");

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals("admin", result.getUser().getUsername());
        assertNotNull(result.getDetails());
    }

    @Test
    public void testAuthenticateWithInvalidCredentials() throws AuthenticationException {
        // Given
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", "nonexistent");
        credentials.put("password", "wrongpassword");

        // When
        AuthenticationResult result = authenticationProvider.authenticate(credentials, "username-password");

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getUser());
        assertNotNull(result.getErrorMessage());
        assertEquals("User not found: nonexistent", result.getErrorMessage());
    }

    @Test
    public void testAuthenticateWithMissingCredentials() throws AuthenticationException {
        // Given
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", "admin");
        // missing password

        // When
        AuthenticationResult result = authenticationProvider.authenticate(credentials, "username-password");

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getUser());
        assertEquals("Username and password are required", result.getErrorMessage());
    }

    @Test
    public void testAuthenticateWithMapCredentials() throws AuthenticationException {
        // Given
        String username = "admin";
        String password = "password123";

        // When
        AuthenticationResult result = authenticationProvider.authenticate(username, password);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals("admin", result.getUser().getUsername());
    }

    @Test
    public void testValidateToken() {
        // Create a mock token
        Token token = new SimpleToken(
            "test-token",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "admin",
            new String[]{"web"},
            new HashMap<>()
        );

        // When
        boolean isValid = authenticationProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    public void testValidateNullToken() {
        // When
        boolean isValid = authenticationProvider.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    public void testRefreshToken() {
        // Create a valid token
        Token originalToken = new SimpleToken(
            "test-token",
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            "admin",
            new String[]{"web"},
            new HashMap<>()
        );

        // When
        Token newToken = authenticationProvider.refreshToken(originalToken);

        // Then
        assertNotNull(newToken);
        assertNotEquals(originalToken.getTokenId(), newToken.getTokenId());
        assertEquals("admin", newToken.getSubject());
    }

    @Test
    public void testLoadUserByUsername() throws AuthenticationException {
        // When
        org.springframework.security.core.userdetails.UserDetails userDetails =
            authenticationProvider.loadUserByUsername("admin");

        // Then
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority())));
    }

    @Test(expected = org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public void testLoadNonExistentUser() throws AuthenticationException {
        // When
        authenticationProvider.loadUserByUsername("nonexistent");
    }

    @Test
    public void testPasswordEncoder() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertNotEquals(rawPassword, encodedPassword);
    }
}