package com.original.security.tracing.config;

import com.original.security.tracing.TracingConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityTracingProperties 单元测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class SecurityTracingPropertiesTest {

    private SecurityTracingProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SecurityTracingProperties();
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("testDefaults_EnabledIsTrue")
        void testDefaults_EnabledIsTrue() {
            assertTrue(properties.isEnabled());
        }

        @Test
        @DisplayName("testDefaults_UsernameMaskLength")
        void testDefaults_UsernameMaskLength() {
            assertEquals(TracingConstants.DEFAULT_USERNAME_MASK_LENGTH, properties.getUsernameMaskLength());
        }

        @Test
        @DisplayName("testDefaults_TokenMaskLength")
        void testDefaults_TokenMaskLength() {
            assertEquals(TracingConstants.DEFAULT_TOKEN_MASK_LENGTH, properties.getTokenMaskLength());
        }

        @Test
        @DisplayName("testDefaults_RecordAuthFailureDetailsIsFalse")
        void testDefaults_RecordAuthFailureDetailsIsFalse() {
            assertFalse(properties.isRecordAuthFailureDetails());
        }

        @Test
        @DisplayName("testDefaults_PropagateToFeignIsTrue")
        void testDefaults_PropagateToFeignIsTrue() {
            assertTrue(properties.isPropagateToFeign());
        }

        @Test
        @DisplayName("testDefaults_RecordRequestPathIsTrue")
        void testDefaults_RecordRequestPathIsTrue() {
            assertTrue(properties.isRecordRequestPath());
        }

        @Test
        @DisplayName("testDefaults_RecordClientIpIsFalse")
        void testDefaults_RecordClientIpIsFalse() {
            assertFalse(properties.isRecordClientIp());
        }

        @Test
        @DisplayName("testDefaults_SamplingRate")
        void testDefaults_SamplingRate() {
            assertEquals(TracingConstants.DEFAULT_SAMPLING_RATE, properties.getSamplingRate());
        }

        @Test
        @DisplayName("testDefaults_LoginPathsContainsExpectedPaths")
        void testDefaults_LoginPathsContainsExpectedPaths() {
            assertNotNull(properties.getLoginPaths());
            assertTrue(properties.getLoginPaths().contains("/login"));
            assertTrue(properties.getLoginPaths().contains("/api/login"));
        }

        @Test
        @DisplayName("testDefaults_IgnoredPathsContainsActuatorPaths")
        void testDefaults_IgnoredPathsContainsActuatorPaths() {
            assertNotNull(properties.getIgnoredPaths());
            assertTrue(properties.getIgnoredPaths().contains("/actuator/health"));
        }
    }

    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {

        @Test
        @DisplayName("testSetEnabled_ChangesValue")
        void testSetEnabled_ChangesValue() {
            properties.setEnabled(false);
            assertFalse(properties.isEnabled());
        }

        @Test
        @DisplayName("testSetUsernameMaskLength_ChangesValue")
        void testSetUsernameMaskLength_ChangesValue() {
            properties.setUsernameMaskLength(5);
            assertEquals(5, properties.getUsernameMaskLength());
        }

        @Test
        @DisplayName("testSetTokenMaskLength_ChangesValue")
        void testSetTokenMaskLength_ChangesValue() {
            properties.setTokenMaskLength(12);
            assertEquals(12, properties.getTokenMaskLength());
        }

        @Test
        @DisplayName("testSetRecordAuthFailureDetails_ChangesValue")
        void testSetRecordAuthFailureDetails_ChangesValue() {
            properties.setRecordAuthFailureDetails(true);
            assertTrue(properties.isRecordAuthFailureDetails());
        }

        @Test
        @DisplayName("testSetPropagateToFeign_ChangesValue")
        void testSetPropagateToFeign_ChangesValue() {
            properties.setPropagateToFeign(false);
            assertFalse(properties.isPropagateToFeign());
        }

        @Test
        @DisplayName("testSetRecordRequestPath_ChangesValue")
        void testSetRecordRequestPath_ChangesValue() {
            properties.setRecordRequestPath(false);
            assertFalse(properties.isRecordRequestPath());
        }

        @Test
        @DisplayName("testSetRecordClientIp_ChangesValue")
        void testSetRecordClientIp_ChangesValue() {
            properties.setRecordClientIp(true);
            assertTrue(properties.isRecordClientIp());
        }

        @Test
        @DisplayName("testSetLoginPaths_ChangesValue")
        void testSetLoginPaths_ChangesValue() {
            List<String> customPaths = new ArrayList<>(Arrays.asList("/custom/login", "/auth/signin"));
            properties.setLoginPaths(customPaths);
            assertEquals(customPaths, properties.getLoginPaths());
        }

        @Test
        @DisplayName("testSetIgnoredPaths_ChangesValue")
        void testSetIgnoredPaths_ChangesValue() {
            List<String> customPaths = new ArrayList<>(Arrays.asList("/health", "/metrics"));
            properties.setIgnoredPaths(customPaths);
            assertEquals(customPaths, properties.getIgnoredPaths());
        }

        @Test
        @DisplayName("testGetLoginPaths_ReturnsUnmodifiableList")
        void testGetLoginPaths_ReturnsUnmodifiableList() {
            assertThrows(UnsupportedOperationException.class, () -> {
                properties.getLoginPaths().add("/new/path");
            });
        }

        @Test
        @DisplayName("testGetIgnoredPaths_ReturnsUnmodifiableList")
        void testGetIgnoredPaths_ReturnsUnmodifiableList() {
            assertThrows(UnsupportedOperationException.class, () -> {
                properties.getIgnoredPaths().add("/new/path");
            });
        }

        @Test
        @DisplayName("testSetLoginPaths_NullValue_HandlesGracefully")
        void testSetLoginPaths_NullValue_HandlesGracefully() {
            properties.setLoginPaths(null);
            assertNotNull(properties.getLoginPaths());
            assertTrue(properties.getLoginPaths().isEmpty());
        }

        @Test
        @DisplayName("testSetIgnoredPaths_NullValue_HandlesGracefully")
        void testSetIgnoredPaths_NullValue_HandlesGracefully() {
            properties.setIgnoredPaths(null);
            assertNotNull(properties.getIgnoredPaths());
            assertTrue(properties.getIgnoredPaths().isEmpty());
        }
    }

    @Nested
    @DisplayName("Sampling Rate Validation Tests")
    class SamplingRateValidationTests {

        @Test
        @DisplayName("testSetSamplingRate_ValidValue_SetsCorrectly")
        void testSetSamplingRate_ValidValue_SetsCorrectly() {
            properties.setSamplingRate(0.5f);
            assertEquals(0.5f, properties.getSamplingRate());
        }

        @Test
        @DisplayName("testSetSamplingRate_NegativeValue_ClampsToZero")
        void testSetSamplingRate_NegativeValue_ClampsToZero() {
            properties.setSamplingRate(-0.5f);
            assertEquals(0.0f, properties.getSamplingRate());
        }

        @Test
        @DisplayName("testSetSamplingRate_OverOneValue_ClampsToOne")
        void testSetSamplingRate_OverOneValue_ClampsToOne() {
            properties.setSamplingRate(1.5f);
            assertEquals(1.0f, properties.getSamplingRate());
        }

        @Test
        @DisplayName("testSetSamplingRate_Zero_SetsCorrectly")
        void testSetSamplingRate_Zero_SetsCorrectly() {
            properties.setSamplingRate(0.0f);
            assertEquals(0.0f, properties.getSamplingRate());
        }

        @Test
        @DisplayName("testSetSamplingRate_One_SetsCorrectly")
        void testSetSamplingRate_One_SetsCorrectly() {
            properties.setSamplingRate(1.0f);
            assertEquals(1.0f, properties.getSamplingRate());
        }
    }
}
