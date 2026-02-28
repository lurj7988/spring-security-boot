package com.original.security.plugin.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link JwtAuthenticationPlugin}
 *
 * <p>JWT authentication uses a filter-based approach ({@link com.original.security.filter.JwtAuthenticationFilter})
 * rather than a traditional AuthenticationProvider. Therefore, this plugin returns null for
 * getAuthenticationProvider() and false for supports() since it doesn't register a provider.
 *
 * @author bmad
 * @since 0.1.0
 */
class JwtAuthenticationPluginTest {

    @Test
    void testGetName_ReturnsJwtPluginName() {
        JwtAuthenticationPlugin plugin = new JwtAuthenticationPlugin();
        assertEquals("jwt", plugin.getName());
    }

    @Test
    void testGetAuthenticationProvider_ReturnsNull_UsesFilterBasedAuth() {
        JwtAuthenticationPlugin plugin = new JwtAuthenticationPlugin();
        // JWT authentication uses JwtAuthenticationFilter instead of a traditional AuthenticationProvider
        assertNull(plugin.getAuthenticationProvider());
    }

    @Test
    void testSupports_AnyClass_ReturnsFalse_FilterBasedAuth() {
        JwtAuthenticationPlugin plugin = new JwtAuthenticationPlugin();
        // This plugin does not support any authentication type via provider pattern
        assertFalse(plugin.supports(Object.class));
        assertFalse(plugin.supports(String.class));
    }
}
