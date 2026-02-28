package com.original.security.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CsrfPropertiesTest {

    @Test
    void testDefaultValues() {
        CsrfProperties properties = new CsrfProperties();
        
        assertTrue(properties.isEnabled());
        assertEquals("X-CSRF-TOKEN", properties.getTokenHeader());
    }
    
    @Test
    void testSetters() {
        CsrfProperties properties = new CsrfProperties();
        
        properties.setEnabled(false);
        properties.setTokenHeader("X-CUSTOM-CSRF");
        
        assertFalse(properties.isEnabled());
        assertEquals("X-CUSTOM-CSRF", properties.getTokenHeader());
    }
}
