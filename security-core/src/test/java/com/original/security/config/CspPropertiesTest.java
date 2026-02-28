package com.original.security.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CspProperties 单元测试。
 * 测试 CSP 配置属性的默认值和 setter 方法。
 *
 * @author Naulu
 * @since 1.0.0
 */
class CspPropertiesTest {

    @Test
    void testDefaultValues() {
        CspProperties properties = new CspProperties();

        assertFalse(properties.isEnabled());
        assertEquals("default-src 'self'", properties.getPolicy());
    }

    @Test
    void testSetters() {
        CspProperties properties = new CspProperties();

        properties.setEnabled(true);
        properties.setPolicy("default-src *");

        assertTrue(properties.isEnabled());
        assertEquals("default-src *", properties.getPolicy());
    }

    @Test
    void testSetPolicy_WithWhitespace_TrimsValue() {
        CspProperties properties = new CspProperties();
        properties.setPolicy("  default-src 'self'  ");
        assertEquals("default-src 'self'", properties.getPolicy());
    }

    @Test
    void testSetPolicy_NullOrEmpty_ThrowsException() {
        CspProperties properties = new CspProperties();
        assertThrows(IllegalArgumentException.class, () -> properties.setPolicy(null));
        assertThrows(IllegalArgumentException.class, () -> properties.setPolicy(""));
        assertThrows(IllegalArgumentException.class, () -> properties.setPolicy("   "));
    }
}
