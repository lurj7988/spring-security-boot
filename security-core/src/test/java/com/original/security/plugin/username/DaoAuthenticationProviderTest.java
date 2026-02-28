package com.original.security.plugin.username;

import com.original.security.core.authentication.AuthenticationResult;
import com.original.security.core.authentication.user.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DaoAuthenticationProvider}.
 * Tests cover successful authentication, bad credentials, user not found, and disabled account scenarios.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class DaoAuthenticationProviderTest {

    private DaoAuthenticationProvider provider;
    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        userDetailsService = mock(UserDetailsService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        provider = new DaoAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    // --- Spring Security AuthenticationProvider tests ---

    @Test
    public void testAuthenticate_ValidCredentials_ReturnsAuthentication() {
        UserDetails user = new User("admin", "encodedPass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "pass");
        Authentication result = provider.authenticate(auth);

        assertNotNull(result);
        assertEquals("admin", result.getName());
        assertEquals(user, result.getPrincipal());
    }

    @Test
    public void testAuthenticate_InvalidPassword_ThrowsBadCredentialsException() {
        UserDetails user = new User("admin", "encodedPass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "wrongPass");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            provider.authenticate(auth);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    public void testAuthenticate_UserNotFound_ThrowsBadCredentialsException() {
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("Not found"));

        Authentication auth = new UsernamePasswordAuthenticationToken("nonexistent", "pass");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            provider.authenticate(auth);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    public void testAuthenticate_DisabledAccount_ThrowsDisabledException() {
        UserDetails user = User.builder()
                .username("disabledUser")
                .password("encodedPass")
                .disabled(true)
                .authorities(Collections.emptyList())
                .build();

        when(userDetailsService.loadUserByUsername("disabledUser")).thenReturn(user);

        Authentication auth = new UsernamePasswordAuthenticationToken("disabledUser", "pass");

        DisabledException exception = assertThrows(DisabledException.class, () -> {
            provider.authenticate(auth);
        });

        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    public void testSupports_ValidTokenClass_ReturnsTrue() {
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testSupports_NullClass_ReturnsFalse() {
        assertFalse(provider.supports(null));
    }

    @Test
    public void testSupports_InvalidClass_ReturnsFalse() {
        assertFalse(provider.supports(String.class));
    }

    // --- Framework AuthenticationProvider tests (authenticate with String, String) ---

    @Test
    public void testAuthenticateStringString_ValidCredentials_ReturnsSuccessResult() {
        UserDetails user = new User("testuser", "encodedPass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPass")).thenReturn(true);

        AuthenticationResult result = provider.authenticate("testuser", "password");

        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals("testuser", result.getUser().getUsername());
    }

    @Test
    public void testAuthenticateStringString_InvalidCredentials_ReturnsFailureResult() {
        UserDetails user = new User("testuser", "encodedPass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);

        AuthenticationResult result = provider.authenticate("testuser", "wrongpass");

        assertFalse(result.isSuccess());
        assertEquals("AUTH_ERROR", result.getErrorCode());
    }

    @Test
    public void testAuthenticateStringString_DisabledAccount_ReturnsFailureResult() {
        UserDetails user = User.builder()
                .username("disabledUser")
                .password("encodedPass")
                .disabled(true)
                .authorities(Collections.emptyList())
                .build();
        when(userDetailsService.loadUserByUsername("disabledUser")).thenReturn(user);

        AuthenticationResult result = provider.authenticate("disabledUser", "password");

        assertFalse(result.isSuccess());
        assertEquals("AUTH_ERROR", result.getErrorCode());
    }

    // --- Framework AuthenticationProvider tests (authenticate with Map) ---

    @Test
    public void testAuthenticateMap_ValidCredentials_ReturnsSuccessResult() {
        UserDetails user = new User("mapuser", "encodedPass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("mapuser")).thenReturn(user);
        when(passwordEncoder.matches("mappass", "encodedPass")).thenReturn(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "mapuser");
        credentials.put("password", "mappass");

        AuthenticationResult result = provider.authenticate(credentials, "username-password");

        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals("mapuser", result.getUser().getUsername());
    }

    @Test
    public void testAuthenticateMap_MissingUsername_ReturnsFailureResult() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("password", "pass");

        AuthenticationResult result = provider.authenticate(credentials, "username-password");

        assertFalse(result.isSuccess());
        assertEquals("MISSING_CREDENTIALS", result.getErrorCode());
    }

    @Test
    public void testAuthenticateMap_MissingPassword_ReturnsFailureResult() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "user");

        AuthenticationResult result = provider.authenticate(credentials, "username-password");

        assertFalse(result.isSuccess());
        assertEquals("MISSING_CREDENTIALS", result.getErrorCode());
    }

    @Test
    public void testAuthenticateMap_InvalidFormat_ReturnsFailureResult() {
        // Use Object type to ensure correct method overload is called
        Object invalidCredentials = "not a map";
        AuthenticationResult result = provider.authenticate(invalidCredentials, "username-password");

        assertFalse(result.isSuccess());
        assertEquals("INVALID_CREDENTIALS_FORMAT", result.getErrorCode());
    }

    // --- Token and loadUserByUsername tests ---

    @Test
    public void testValidateToken_AlwaysReturnsFalse() {
        assertFalse(provider.validateToken(null));
    }

    @Test
    public void testRefreshToken_AlwaysReturnsNull() {
        assertNull(provider.refreshToken(null));
    }

    @Test
    public void testLoadUserByUsername_ExistingUser_ReturnsUserDetails() {
        UserDetails user = new User("existinguser", "pass", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("existinguser")).thenReturn(user);

        UserDetails result = provider.loadUserByUsername("existinguser");

        assertNotNull(result);
        assertEquals("existinguser", result.getUsername());
    }

    @Test
    public void testLoadUserByUsername_NonExistingUser_ThrowsException() {
        when(userDetailsService.loadUserByUsername("nonexistent")).thenThrow(new UsernameNotFoundException("Not found"));

        assertThrows(com.original.security.core.authentication.AuthenticationException.class, () -> {
            provider.loadUserByUsername("nonexistent");
        });
    }
}
