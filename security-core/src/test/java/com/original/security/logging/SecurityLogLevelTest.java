package com.original.security.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityLogLevel 枚举测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DisplayName("SecurityLogLevel Tests")
class SecurityLogLevelTest {

    @Nested
    @DisplayName("getLevel method")
    class GetLevel {

        @Test
        @DisplayName("Should return correct level string for each enum value")
        void testGetLevel_AllValues_ReturnsCorrectString() {
            assertEquals("DEBUG", SecurityLogLevel.DEBUG.getLevel());
            assertEquals("INFO", SecurityLogLevel.INFO.getLevel());
            assertEquals("WARN", SecurityLogLevel.WARN.getLevel());
            assertEquals("ERROR", SecurityLogLevel.ERROR.getLevel());
        }
    }

    @Nested
    @DisplayName("fromString method")
    class FromString {

        @ParameterizedTest
        @CsvSource({
                "DEBUG, DEBUG",
                "debug, DEBUG",
                "Debug, DEBUG",
                "INFO, INFO",
                "info, INFO",
                "WARN, WARN",
                "warn, WARN",
                "ERROR, ERROR",
                "error, ERROR"
        })
        @DisplayName("Should parse valid level string")
        void testFromString_ValidLevel_ReturnsCorrectEnum(String input, SecurityLogLevel expected) {
            assertEquals(expected, SecurityLogLevel.fromString(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "INVALID", "TRACE", "FATAL"})
        @DisplayName("Should return INFO for invalid or unknown level")
        void testFromString_InvalidLevel_ReturnsInfo(String input) {
            assertEquals(SecurityLogLevel.INFO, SecurityLogLevel.fromString(input));
        }

        @Test
        @DisplayName("Should return INFO for null input")
        void testFromString_NullInput_ReturnsInfo() {
            assertEquals(SecurityLogLevel.INFO, SecurityLogLevel.fromString(null));
        }
    }

    @Nested
    @DisplayName("enum values")
    class EnumValues {

        @Test
        @DisplayName("Should have exactly 4 log levels")
        void testEnumValues_Count() {
            assertEquals(4, SecurityLogLevel.values().length);
        }
    }
}
