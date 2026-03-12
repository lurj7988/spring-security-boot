package com.original.security.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityLogEvent 测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DisplayName("SecurityLogEvent Tests")
class SecurityLogEventTest {

    @Nested
    @DisplayName("Builder tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build event with all required fields")
        void testBuild_AllRequiredFields_ReturnsEvent() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("AUTHENTICATION_SUCCESS")
                    .level(SecurityLogLevel.INFO)
                    .message("User authenticated")
                    .build();

            assertNotNull(event);
            assertEquals("AUTHENTICATION_SUCCESS", event.getEventType());
            assertEquals(SecurityLogLevel.INFO, event.getLevel());
            assertEquals("User authenticated", event.getMessage());
            assertNotNull(event.getTimestamp());
        }

        @Test
        @DisplayName("Should build event with username and success fields")
        void testBuild_WithUsernameAndSuccess_ReturnsEvent() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .username("testuser")
                    .success(true)
                    .build();

            assertEquals("testuser", event.getField(SecurityLogField.USERNAME));
            assertEquals(true, event.getField(SecurityLogField.SUCCESS));
        }

        @Test
        @DisplayName("Should build event with custom fields")
        void testBuild_WithCustomFields_ReturnsEvent() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .field("customField", "customValue")
                    .field(SecurityLogField.CLIENT_IP, "192.168.1.1")
                    .build();

            assertEquals("customValue", event.getField("customField"));
            assertEquals("192.168.1.1", event.getField(SecurityLogField.CLIENT_IP));
        }

        @Test
        @DisplayName("Should build event with multiple fields from map")
        void testBuild_WithFieldsMap_ReturnsEvent() {
            Map<String, Object> additionalFields = new HashMap<>();
            additionalFields.put("field1", "value1");
            additionalFields.put("field2", "value2");

            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .fields(additionalFields)
                    .build();

            assertEquals("value1", event.getField("field1"));
            assertEquals("value2", event.getField("field2"));
        }

        @Test
        @DisplayName("Should build event with custom timestamp")
        void testBuild_WithCustomTimestamp_ReturnsEvent() {
            Instant customTime = Instant.parse("2026-01-01T00:00:00Z");

            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .timestamp(customTime)
                    .build();

            assertEquals(customTime, event.getTimestamp());
        }

        @Test
        @DisplayName("Should throw exception when eventType is null")
        void testBuild_NullEventType_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () ->
                    SecurityLogEvent.builder()
                            .eventType(null)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception when eventType is empty")
        void testBuild_EmptyEventType_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () ->
                    SecurityLogEvent.builder()
                            .eventType("")
                            .build()
            );
        }

        @Test
        @DisplayName("Should default level to INFO when not specified")
        void testBuild_DefaultLevel_ReturnsInfo() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .build();

            assertEquals(SecurityLogLevel.INFO, event.getLevel());
        }

        @Test
        @DisplayName("Should auto-generate timestamp when not specified")
        void testBuild_DefaultTimestamp_ReturnsCurrentTime() {
            Instant before = Instant.now();
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .build();
            Instant after = Instant.now();

            assertTrue(event.getTimestamp().isAfter(before) || event.getTimestamp().equals(before));
            assertTrue(event.getTimestamp().isBefore(after) || event.getTimestamp().equals(after));
        }
    }

    @Nested
    @DisplayName("Masking tests")
    class MaskingTests {

        @Test
        @DisplayName("Should mask password field")
        void testMasking_PasswordField_IsMasked() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .field("password", "secretPassword123")
                    .build();

            Object value = event.getField("password");
            assertNotNull(value);
            assertNotEquals("secretPassword123", value);
        }

        @Test
        @DisplayName("Should mask token field")
        void testMasking_TokenField_IsMasked() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .field("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                    .build();

            Object value = event.getField("token");
            assertNotNull(value);
            assertNotEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c", value);
        }

        @Test
        @DisplayName("Should mask non-String sensitive fields")
        void testMasking_NonStringSensitiveField_IsMasked() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .field("password", new char[]{'s', 'e', 'c', 'r', 'e', 't'})
                    .build();

            Object value = event.getField("password");
            assertEquals("******", value);
        }

        @Test
        @DisplayName("Should not mask non-sensitive fields")
        void testMasking_NonSensitiveField_NotMasked() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .field("username", "testuser")
                    .field("client_ip", "192.168.1.1")
                    .build();

            assertEquals("testuser", event.getField("username"));
            assertEquals("192.168.1.1", event.getField("client_ip"));
        }
    }

    @Nested
    @DisplayName("toMap tests")
    class ToMapTests {

        @Test
        @DisplayName("Should convert to map with all standard fields")
        void testToMap_StandardFields_ReturnsMap() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .level(SecurityLogLevel.WARN)
                    .message("Test message")
                    .build();

            Map<String, Object> map = event.toMap();

            assertEquals("TEST_EVENT", map.get("event_type"));
            assertEquals("WARN", map.get("level"));
            assertEquals("Test message", map.get("message"));
            assertNotNull(map.get("timestamp"));
        }

        @Test
        @DisplayName("Should include custom fields in map")
        void testToMap_CustomFields_ReturnsMap() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .field("customKey", "customValue")
                    .build();

            Map<String, Object> map = event.toMap();

            assertEquals("customValue", map.get("customKey"));
        }
    }

    @Nested
    @DisplayName("Immutability tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should return unmodifiable fields map")
        void testGetFields_ReturnsUnmodifiableMap() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .build();

            assertThrows(UnsupportedOperationException.class, () ->
                    event.getFields().put("newKey", "newValue")
            );
        }
    }

    @Nested
    @DisplayName("equals and hashCode tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal for same values")
        void testEquals_SameValues_ReturnsTrue() {
            Instant timestamp = Instant.now();
            SecurityLogEvent event1 = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .level(SecurityLogLevel.INFO)
                    .timestamp(timestamp)
                    .message("Test")
                    .build();

            SecurityLogEvent event2 = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT")
                    .level(SecurityLogLevel.INFO)
                    .timestamp(timestamp)
                    .message("Test")
                    .build();

            assertEquals(event1, event2);
            assertEquals(event1.hashCode(), event2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different values")
        void testEquals_DifferentValues_ReturnsFalse() {
            SecurityLogEvent event1 = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT_1")
                    .build();

            SecurityLogEvent event2 = SecurityLogEvent.builder()
                    .eventType("TEST_EVENT_2")
                    .build();

            assertNotEquals(event1, event2);
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain event type")
        void testToString_ContainsEventType() {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType("MY_EVENT")
                    .build();

            String str = event.toString();
            assertTrue(str.contains("MY_EVENT"));
        }
    }
}
