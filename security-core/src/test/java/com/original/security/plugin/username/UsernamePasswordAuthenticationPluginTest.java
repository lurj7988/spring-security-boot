package com.original.security.plugin.username;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link UsernamePasswordAuthenticationPlugin}.
 * Tests cover plugin registration, provider retrieval, and authentication type support.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class UsernamePasswordAuthenticationPluginTest {

    private UsernamePasswordAuthenticationPlugin plugin;
    private DaoAuthenticationProvider provider;

    @BeforeEach
    public void setUp() {
        provider = mock(DaoAuthenticationProvider.class);
        plugin = new UsernamePasswordAuthenticationPlugin(provider);
    }

    @Test
    public void testGetName_ReturnsCorrectPluginName() {
        assertEquals("username-password", plugin.getName());
    }

    @Test
    public void testGetAuthenticationProvider_ReturnsConfiguredProvider() {
        assertNotNull(plugin.getAuthenticationProvider());
        assertEquals(provider, plugin.getAuthenticationProvider());
    }

    @Test
    public void testSupports_ValidTokenClass_ReturnsTrue() {
        assertTrue(plugin.supports(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testSupports_NullClass_ReturnsFalse() {
        assertFalse(plugin.supports(null));
    }

    @Test
    public void testSupports_InvalidClass_ReturnsFalse() {
        assertFalse(plugin.supports(String.class));
        assertFalse(plugin.supports(Object.class));
    }
}
