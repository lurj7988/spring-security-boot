package com.original.security.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityHeadersProperties 配置验证测试。
 * 测试配置属性的范围验证和非法输入处理。
 *
 * @author Naulu
 * @since 1.0.0
 */
public class SecurityHeadersPropertiesValidationTest {

    @Test
    public void testSetFrameOptions_InvalidValue_ThrowsIllegalArgumentException() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> properties.setFrameOptions("ALLOW-FROM")
        );
        assertTrue(exception.getMessage().contains("frameOptions must be either 'DENY' or 'SAMEORIGIN'"));
    }

    @Test
    public void testSetFrameOptions_AllowFromLowercase_ThrowsIllegalArgumentException() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> properties.setFrameOptions("allow-from")
        );
        assertTrue(exception.getMessage().contains("frameOptions must be either 'DENY' or 'SAMEORIGIN'"));
    }

    @Test
    public void testSetFrameOptions_DenyCaseInsensitive_Accepts() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        // 所有大小写变体都应该被转换为大写 DENY
        assertDoesNotThrow(() -> properties.setFrameOptions("deny"));
        assertEquals("DENY", properties.getFrameOptions());

        properties.setFrameOptions("Deny");
        assertEquals("DENY", properties.getFrameOptions());

        properties.setFrameOptions("DENY");
        assertEquals("DENY", properties.getFrameOptions());
    }

    @Test
    public void testSetFrameOptions_SameOriginCaseInsensitive_Accepts() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        // 所有大小写变体都应该被转换为大写 SAMEORIGIN
        assertDoesNotThrow(() -> properties.setFrameOptions("sameorigin"));
        assertEquals("SAMEORIGIN", properties.getFrameOptions());

        properties.setFrameOptions("SameOrigin");
        assertEquals("SAMEORIGIN", properties.getFrameOptions());

        properties.setFrameOptions("SAMEORIGIN");
        assertEquals("SAMEORIGIN", properties.getFrameOptions());
    }

    @Test
    public void testSetHstsMaxAge_NegativeValue_ThrowsIllegalArgumentException() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> properties.setHstsMaxAge(-1)
        );
        assertTrue(exception.getMessage().contains("hstsMaxAge must be non-negative"));
    }

    @Test
    public void testSetHstsMaxAge_LargeValue_Accepts() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        assertDoesNotThrow(() -> properties.setHstsMaxAge(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, properties.getHstsMaxAge());
    }

    @Test
    public void testSetHstsMaxAge_Zero_Accepts() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        assertDoesNotThrow(() -> properties.setHstsMaxAge(0));
        assertEquals(0, properties.getHstsMaxAge());
    }

    @Test
    public void testSetHstsMaxAge_DefaultValue_IsValid() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        assertEquals(31536000, properties.getHstsMaxAge());
    }

    @Test
    public void testSetFrameOptions_Null_ResetsToDefault() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        properties.setFrameOptions("SAMEORIGIN");
        assertEquals("SAMEORIGIN", properties.getFrameOptions());

        // null 应该重置为默认值 DENY
        properties.setFrameOptions(null);
        assertEquals("DENY", properties.getFrameOptions());
    }

    @Test
    public void testSetFrameOptions_Lowercase_ConvertsToUppercase() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();

        properties.setFrameOptions("deny");
        assertEquals("DENY", properties.getFrameOptions());

        properties.setFrameOptions("sameorigin");
        assertEquals("SAMEORIGIN", properties.getFrameOptions());
    }

    @Test
    public void testSetFrameOptions_MixedCase_ConvertsToUppercase() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();

        properties.setFrameOptions("Deny");
        assertEquals("DENY", properties.getFrameOptions());

        properties.setFrameOptions("SameOrigin");
        assertEquals("SAMEORIGIN", properties.getFrameOptions());
    }

    // HSTS includeSubDomains 测试
    @Test
    public void testHstsIncludeSubDomains_DefaultValue_IsTrue() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        assertTrue(properties.isHstsIncludeSubDomains());
    }

    @Test
    public void testHstsIncludeSubDomains_CanBeSetToFalse() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        properties.setHstsIncludeSubDomains(false);
        assertFalse(properties.isHstsIncludeSubDomains());
    }

    // HSTS preload 测试
    @Test
    public void testHstsPreload_DefaultValue_IsFalse() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        assertFalse(properties.isHstsPreload());
    }

    @Test
    public void testHstsPreload_CanBeSetToTrue() {
        SecurityHeadersProperties properties = new SecurityHeadersProperties();
        properties.setHstsPreload(true);
        assertTrue(properties.isHstsPreload());
    }
}
