package com.original.security.plugin.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionAuthenticationPlugin 单元测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class SessionAuthenticationPluginTest {

    private SessionAuthenticationPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new SessionAuthenticationPlugin();
    }

    @Test
    @DisplayName("getName_ReturnsCorrectPluginName")
    void testGetName_ReturnsCorrectPluginName() {
        // When
        String name = plugin.getName();

        // Then
        assertEquals("session-authentication", name);
    }

    @Test
    @DisplayName("getAuthenticationProvider_ReturnsNull")
    void testGetAuthenticationProvider_ReturnsNull() {
        // When
        com.original.security.core.authentication.AuthenticationProvider provider = plugin.getAuthenticationProvider();

        // Then
        assertNull(provider, "Session authentication relies on Spring Security built-in mechanisms");
    }

    @Test
    @DisplayName("supports_WithUsernamePasswordToken_ReturnsTrue")
    void testSupports_WithUsernamePasswordToken_ReturnsTrue() {
        // When
        boolean supports = plugin.supports(UsernamePasswordAuthenticationToken.class);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("supports_WithNullClass_ReturnsFalse")
    void testSupports_WithNullClass_ReturnsFalse() {
        // When
        boolean supports = plugin.supports(null);

        // Then
        assertFalse(supports);
    }

    @Test
    @DisplayName("supports_WithUnsupportedClass_ReturnsFalse")
    void testSupports_WithUnsupportedClass_ReturnsFalse() {
        // When
        boolean supports = plugin.supports(TestingAuthenticationToken.class);

        // Then
        assertFalse(supports);
    }

    @Test
    @DisplayName("supports_WithSubclassOfUsernamePasswordToken_ReturnsTrue")
    void testSupports_WithSubclassOfUsernamePasswordToken_ReturnsTrue() {
        // Given
        class CustomToken extends UsernamePasswordAuthenticationToken {
            public CustomToken() {
                super("principal", "credentials");
            }
        }

        // When
        boolean supports = plugin.supports(CustomToken.class);

        // Then
        assertTrue(supports);
    }
}
