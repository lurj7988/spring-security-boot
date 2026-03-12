package com.original.security.logging.config;

import com.original.security.logging.SensitiveDataMasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityLoggingProperties 测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DisplayName("SecurityLoggingProperties Tests")
class SecurityLoggingPropertiesTest {

    private SecurityLoggingProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SecurityLoggingProperties();
    }

    @Nested
    @DisplayName("Default values tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have enabled = true by default")
        void testDefault_Enabled_IsTrue() {
            assertTrue(properties.isEnabled());
        }

        @Test
        @DisplayName("Should have jsonOutput = true by default")
        void testDefault_JsonOutput_IsTrue() {
            assertTrue(properties.isJsonOutput());
        }

        @Test
        @DisplayName("Should have includeStackTrace = true by default")
        void testDefault_IncludeStackTrace_IsTrue() {
            assertTrue(properties.isIncludeStackTrace());
        }

        @Test
        @DisplayName("Should have includeClientIp = true by default")
        void testDefault_IncludeClientIp_IsTrue() {
            assertTrue(properties.isIncludeClientIp());
        }

        @Test
        @DisplayName("Should have includeUserAgent = false by default")
        void testDefault_IncludeUserAgent_IsFalse() {
            assertFalse(properties.isIncludeUserAgent());
        }

        @Test
        @DisplayName("Should have includeRequestId = true by default")
        void testDefault_IncludeRequestId_IsTrue() {
            assertTrue(properties.isIncludeRequestId());
        }

        @Test
        @DisplayName("Should have includeSessionId = false by default")
        void testDefault_IncludeSessionId_IsFalse() {
            assertFalse(properties.isIncludeSessionId());
        }

        @Test
        @DisplayName("Should have defaultLevel = INFO by default")
        void testDefault_DefaultLevel_IsInfo() {
            assertEquals("INFO", properties.getDefaultLevel());
        }

        @Test
        @DisplayName("Should have maskingMode = PARTIAL by default")
        void testDefault_MaskingMode_IsPartial() {
            assertEquals(SensitiveDataMasker.MaskingMode.PARTIAL, properties.getMaskingMode());
        }
    }

    @Nested
    @DisplayName("Setter tests")
    class SetterTests {

        @Test
        @DisplayName("Should set enabled")
        void testSetEnabled() {
            properties.setEnabled(false);
            assertFalse(properties.isEnabled());
        }

        @Test
        @DisplayName("Should set jsonOutput")
        void testSetJsonOutput() {
            properties.setJsonOutput(false);
            assertFalse(properties.isJsonOutput());
        }

        @Test
        @DisplayName("Should set includeStackTrace")
        void testSetIncludeStackTrace() {
            properties.setIncludeStackTrace(false);
            assertFalse(properties.isIncludeStackTrace());
        }

        @Test
        @DisplayName("Should set includeClientIp")
        void testSetIncludeClientIp() {
            properties.setIncludeClientIp(false);
            assertFalse(properties.isIncludeClientIp());
        }

        @Test
        @DisplayName("Should set includeUserAgent")
        void testSetIncludeUserAgent() {
            properties.setIncludeUserAgent(true);
            assertTrue(properties.isIncludeUserAgent());
        }

        @Test
        @DisplayName("Should set includeRequestId")
        void testSetIncludeRequestId() {
            properties.setIncludeRequestId(false);
            assertFalse(properties.isIncludeRequestId());
        }

        @Test
        @DisplayName("Should set includeSessionId")
        void testSetIncludeSessionId() {
            properties.setIncludeSessionId(true);
            assertTrue(properties.isIncludeSessionId());
        }

        @Test
        @DisplayName("Should set defaultLevel")
        void testSetDefaultLevel() {
            properties.setDefaultLevel("DEBUG");
            assertEquals("DEBUG", properties.getDefaultLevel());
        }

        @Test
        @DisplayName("Should set maskingMode")
        void testSetMaskingMode() {
            properties.setMaskingMode(SensitiveDataMasker.MaskingMode.FULL);
            assertEquals(SensitiveDataMasker.MaskingMode.FULL, properties.getMaskingMode());
        }
    }

    @Nested
    @DisplayName("MaskingMode enum tests")
    class MaskingModeEnumTests {

        @Test
        @DisplayName("Should have FULL mode")
        void testMaskingMode_Full() {
            assertEquals("FULL", SensitiveDataMasker.MaskingMode.FULL.name());
        }

        @Test
        @DisplayName("Should have PARTIAL mode")
        void testMaskingMode_Partial() {
            assertEquals("PARTIAL", SensitiveDataMasker.MaskingMode.PARTIAL.name());
        }

        @Test
        @DisplayName("Should have NONE mode")
        void testMaskingMode_None() {
            assertEquals("NONE", SensitiveDataMasker.MaskingMode.NONE.name());
        }
    }
}
