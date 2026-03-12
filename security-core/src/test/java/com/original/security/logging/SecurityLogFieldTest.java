package com.original.security.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityLogField 枚举测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DisplayName("SecurityLogField Tests")
class SecurityLogFieldTest {

    @Nested
    @DisplayName("getFieldName method")
    class GetFieldName {

        @Test
        @DisplayName("Should return correct field name for EVENT_TYPE")
        void testGetFieldName_EventType_ReturnsCorrectName() {
            assertEquals("event_type", SecurityLogField.EVENT_TYPE.getFieldName());
        }

        @Test
        @DisplayName("Should return correct field name for TIMESTAMP")
        void testGetFieldName_Timestamp_ReturnsCorrectName() {
            assertEquals("timestamp", SecurityLogField.TIMESTAMP.getFieldName());
        }

        @Test
        @DisplayName("Should return correct field name for USERNAME")
        void testGetFieldName_Username_ReturnsCorrectName() {
            assertEquals("username", SecurityLogField.USERNAME.getFieldName());
        }

        @Test
        @DisplayName("Should return correct field name for SUCCESS")
        void testGetFieldName_Success_ReturnsCorrectName() {
            assertEquals("success", SecurityLogField.SUCCESS.getFieldName());
        }

        @Test
        @DisplayName("Should return correct field name for CLIENT_IP")
        void testGetFieldName_ClientIp_ReturnsCorrectName() {
            assertEquals("client_ip", SecurityLogField.CLIENT_IP.getFieldName());
        }
    }

    @Nested
    @DisplayName("toString method")
    class ToString {

        @Test
        @DisplayName("Should return field name as string")
        void testToString_ReturnsFieldName() {
            assertEquals("event_type", SecurityLogField.EVENT_TYPE.toString());
            assertEquals("error_message", SecurityLogField.ERROR_MESSAGE.toString());
        }
    }

    @Nested
    @DisplayName("enum values")
    class EnumValues {

        @Test
        @DisplayName("Should have all required fields")
        void testEnumValues_RequiredFields() {
            assertNotNull(SecurityLogField.EVENT_TYPE);
            assertNotNull(SecurityLogField.TIMESTAMP);
            assertNotNull(SecurityLogField.LEVEL);
            assertNotNull(SecurityLogField.MESSAGE);
            assertNotNull(SecurityLogField.USERNAME);
            assertNotNull(SecurityLogField.SUCCESS);
            assertNotNull(SecurityLogField.CLIENT_IP);
            assertNotNull(SecurityLogField.ERROR_CODE);
            assertNotNull(SecurityLogField.ERROR_MESSAGE);
        }
    }
}
