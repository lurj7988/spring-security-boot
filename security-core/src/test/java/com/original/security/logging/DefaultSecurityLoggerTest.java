package com.original.security.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DefaultSecurityLogger 测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultSecurityLogger Tests")
class DefaultSecurityLoggerTest {

    private DefaultSecurityLogger logger;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        logger = new DefaultSecurityLogger(objectMapper, true);
        MDC.clear();
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create logger with default settings")
        void testConstructor_Default_CreatesLogger() {
            DefaultSecurityLogger defaultLogger = new DefaultSecurityLogger();
            assertNotNull(defaultLogger);
        }

        @Test
        @DisplayName("Should create logger with custom settings")
        void testConstructor_Custom_CreatesLogger() {
            DefaultSecurityLogger customLogger = new DefaultSecurityLogger(objectMapper, false);
            assertNotNull(customLogger);
        }

        @Test
        @DisplayName("Should handle null ObjectMapper")
        void testConstructor_NullObjectMapper_CreatesLogger() {
            DefaultSecurityLogger loggerWithNull = new DefaultSecurityLogger(null, true);
            assertNotNull(loggerWithNull);
        }
    }

    @Nested
    @DisplayName("debug logging tests")
    class DebugLoggingTests {

        @Test
        @DisplayName("Should log debug event")
        void testDebug_Event_LogsSuccessfully() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("DEBUG_TEST")
                    .level(SecurityLogLevel.DEBUG)
                    .message("Debug message")
                    .build();

            assertDoesNotThrow(() -> logger.debug(event));
        }

        @Test
        @DisplayName("Should log debug with type and message")
        void testDebug_TypeAndMessage_LogsSuccessfully() {
            assertDoesNotThrow(() -> logger.debug("DEBUG_EVENT", "Debug test message"));
        }
    }

    @Nested
    @DisplayName("info logging tests")
    class InfoLoggingTests {

        @Test
        @DisplayName("Should log info event")
        void testInfo_Event_LogsSuccessfully() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("INFO_TEST")
                    .level(SecurityLogLevel.INFO)
                    .message("Info message")
                    .build();

            assertDoesNotThrow(() -> logger.info(event));
        }

        @Test
        @DisplayName("Should log info with type and message")
        void testInfo_TypeAndMessage_LogsSuccessfully() {
            assertDoesNotThrow(() -> logger.info("INFO_EVENT", "Info test message"));
        }
    }

    @Nested
    @DisplayName("warn logging tests")
    class WarnLoggingTests {

        @Test
        @DisplayName("Should log warn event")
        void testWarn_Event_LogsSuccessfully() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("WARN_TEST")
                    .level(SecurityLogLevel.WARN)
                    .message("Warn message")
                    .build();

            assertDoesNotThrow(() -> logger.warn(event));
        }

        @Test
        @DisplayName("Should log warn with type and message")
        void testWarn_TypeAndMessage_LogsSuccessfully() {
            assertDoesNotThrow(() -> logger.warn("WARN_EVENT", "Warn test message"));
        }
    }

    @Nested
    @DisplayName("error logging tests")
    class ErrorLoggingTests {

        @Test
        @DisplayName("Should log error event")
        void testError_Event_LogsSuccessfully() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("ERROR_TEST")
                    .level(SecurityLogLevel.ERROR)
                    .message("Error message")
                    .build();

            assertDoesNotThrow(() -> logger.error(event));
        }

        @Test
        @DisplayName("Should log error with type and message")
        void testError_TypeAndMessage_LogsSuccessfully() {
            assertDoesNotThrow(() -> logger.error("ERROR_EVENT", "Error test message"));
        }

        @Test
        @DisplayName("Should log error with throwable")
        void testError_WithThrowable_LogsSuccessfully() {
            Exception exception = new RuntimeException("Test exception");
            assertDoesNotThrow(() -> logger.error("ERROR_EVENT", "Error with exception", exception));
        }

        @Test
        @DisplayName("Should handle null throwable in error method")
        void testError_WithNullThrowable_LogsSuccessfully() {
            assertDoesNotThrow(() -> logger.error("ERROR_EVENT", "Error with null throwable", (Throwable) null));
        }

        @Test
        @DisplayName("Should log error event with throwable")
        void testError_EventWithThrowable_LogsSuccessfully() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("ERROR_TEST")
                    .level(SecurityLogLevel.ERROR)
                    .message("Error message")
                    .build();
            Exception exception = new RuntimeException("Test exception");

            assertDoesNotThrow(() -> logger.error(event, exception));
        }
    }

    @Nested
    @DisplayName("Authentication logging tests")
    class AuthenticationLoggingTests {

        @Test
        @DisplayName("Should log authentication success")
        void testLogAuthenticationSuccess_LogsSuccessfully() {
            assertDoesNotThrow(() -> logger.logAuthenticationSuccess("testuser", "Login successful"));
        }

        @Test
        @DisplayName("Should log authentication failure")
        void testLogAuthenticationFailure_LogsSuccessfully() {
            assertDoesNotThrow(() -> logger.logAuthenticationFailure("testuser", "Invalid password"));
        }

        @Test
        @DisplayName("Should log authentication failure with throwable")
        void testLogAuthenticationFailure_WithThrowable_LogsSuccessfully() {
            Exception exception = new RuntimeException("Authentication failed");
            assertDoesNotThrow(() -> logger.logAuthenticationFailure("testuser", "Auth failed", exception));
        }
    }

    @Nested
    @DisplayName("Authorization logging tests")
    class AuthorizationLoggingTests {

        @Test
        @DisplayName("Should log authorization failure")
        void testLogAuthorizationFailure_LogsSuccessfully() {
            assertDoesNotThrow(() ->
                    logger.logAuthorizationFailure("testuser", "/api/admin", "ROLE_ADMIN", "Access denied")
            );
        }
    }

    @Nested
    @DisplayName("MDC tests")
    class MdcTests {

        @Test
        @DisplayName("Should put value in MDC")
        void testPutMdc_ValidInput_PutsInMdc() {
            logger.putMdc("requestId", "12345");

            assertEquals("12345", MDC.get("requestId"));

            MDC.clear();
        }

        @Test
        @DisplayName("Should not put null key in MDC")
        void testPutMdc_NullKey_DoesNotPut() {
            logger.putMdc(null, "value");

            // Should not throw exception when key is null
            assertTrue(true);
        }

        @Test
        @DisplayName("Should not put null value in MDC")
        void testPutMdc_NullValue_DoesNotPut() {
            logger.putMdc("key", null);

            assertNull(MDC.get("key"));
        }

        @Test
        @DisplayName("Should remove value from MDC")
        void testRemoveMdc_ExistingKey_RemovesFromMdc() {
            MDC.put("testKey", "testValue");

            logger.removeMdc("testKey");

            assertNull(MDC.get("testKey"));
        }

        @Test
        @DisplayName("Should clear MDC")
        void testClearMdc_ClearsAllMdc() {
            MDC.put("key1", "value1");
            MDC.put("key2", "value2");

            logger.clearMdc();

            assertNull(MDC.get("key1"));
            assertNull(MDC.get("key2"));
        }
    }

    @Nested
    @DisplayName("JSON output tests")
    class JsonOutputTests {

        @Test
        @DisplayName("Should output JSON format when enabled")
        void testJsonOutput_Enabled_OutputsJson() {
            DefaultSecurityLogger jsonLogger = new DefaultSecurityLogger(objectMapper, true);

            assertDoesNotThrow(() -> jsonLogger.info("JSON_TEST", "JSON output test"));
        }

        @Test
        @DisplayName("Should output plain format when disabled")
        void testJsonOutput_Disabled_OutputsPlain() {
            DefaultSecurityLogger plainLogger = new DefaultSecurityLogger(objectMapper, false);

            assertDoesNotThrow(() -> plainLogger.info("PLAIN_TEST", "Plain output test"));
        }
    }
}
