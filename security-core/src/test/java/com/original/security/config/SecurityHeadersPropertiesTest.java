package com.original.security.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityHeadersProperties 单元测试。
 * 测试默认值和 setter 方法。
 *
 * @author Naulu
 * @since 1.0.0
 */
class SecurityHeadersPropertiesTest {

    @Test
    void testDefaultValues() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();

        assertTrue(properties.isEnabled());
        assertEquals("DENY", properties.getFrameOptions());
        assertTrue(properties.isContentTypeOptions());
        assertTrue(properties.isXssProtection());
        assertEquals(31536000, properties.getHstsMaxAge());
        assertTrue(properties.isHstsIncludeSubDomains());
        assertFalse(properties.isHstsPreload());
    }

    @Test
    void testSetters() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();

        properties.setEnabled(false);
        properties.setFrameOptions("SAMEORIGIN");
        properties.setContentTypeOptions(false);
        properties.setXssProtection(false);
        properties.setHstsMaxAge(0);
        properties.setHstsIncludeSubDomains(false);
        properties.setHstsPreload(true);

        assertFalse(properties.isEnabled());
        assertEquals("SAMEORIGIN", properties.getFrameOptions());
        assertFalse(properties.isContentTypeOptions());
        assertFalse(properties.isXssProtection());
        assertEquals(0, properties.getHstsMaxAge());
        assertFalse(properties.isHstsIncludeSubDomains());
        assertTrue(properties.isHstsPreload());
    }
}
