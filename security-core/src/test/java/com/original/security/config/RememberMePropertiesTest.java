package com.original.security.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RememberMePropertiesTest {

    @Test
    public void testDefaultValues() {
        RememberMeProperties properties = new RememberMeProperties();
        
        assertTrue(properties.isEnabled());
        assertEquals(604800, properties.getTokenValiditySeconds());
        assertEquals("remember-me", properties.getCookieName());
        assertNull(properties.getKey());
    }

    @Test
    public void testSetEnabled() {
        RememberMeProperties properties = new RememberMeProperties();
        properties.setEnabled(false);
        assertFalse(properties.isEnabled());
    }

    @Test
    public void testSetTokenValiditySeconds_ValidInput() {
        RememberMeProperties properties = new RememberMeProperties();
        properties.setTokenValiditySeconds(3600);
        assertEquals(3600, properties.getTokenValiditySeconds());
    }

    @Test
    public void testSetTokenValiditySeconds_InvalidInput_ThrowsException() {
        RememberMeProperties properties = new RememberMeProperties();
        
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            properties.setTokenValiditySeconds(0);
        });
        assertEquals("Remember me token validity seconds must be greater than 0", exception1.getMessage());

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            properties.setTokenValiditySeconds(-1);
        });
        assertEquals("Remember me token validity seconds must be greater than 0", exception2.getMessage());
    }

    @Test
    public void testSetKey() {
        RememberMeProperties properties = new RememberMeProperties();
        properties.setKey("my-test-key");
        assertEquals("my-test-key", properties.getKey());
    }

    @Test
    public void testSetCookieName_ValidInput() {
        RememberMeProperties properties = new RememberMeProperties();
        properties.setCookieName("custom-remember-me");
        assertEquals("custom-remember-me", properties.getCookieName());
    }

    @Test
    public void testSetCookieName_InvalidInput_ThrowsException() {
        RememberMeProperties properties = new RememberMeProperties();
        
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            properties.setCookieName(null);
        });
        assertEquals("Remember me cookie name cannot be empty", exception1.getMessage());

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            properties.setCookieName("");
        });
        assertEquals("Remember me cookie name cannot be empty", exception2.getMessage());

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
            properties.setCookieName("   ");
        });
        assertEquals("Remember me cookie name cannot be empty", exception3.getMessage());
    }
}
