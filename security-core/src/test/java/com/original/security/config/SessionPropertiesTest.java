package com.original.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionProperties 单元测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class SessionPropertiesTest {

    @Test
    @DisplayName("DefaultValues_AreCorrect")
    void testDefaultValues_AreCorrect() {
        // Given
        SessionProperties properties = new SessionProperties();

        // Then
        assertEquals(1800, properties.getTimeout(), "Default timeout should be 1800 seconds");
        assertEquals(1, properties.getMaxSessions(), "Default max sessions should be 1");
        assertEquals("memory", properties.getStoreType(), "Default store type should be memory");
        assertTrue(properties.isFixationProtection(), "Fixation protection should be enabled by default");
    }

    @Test
    @DisplayName("setTimeout_WithValidValue_Succeeds")
    void testSetTimeout_WithValidValue_Succeeds() {
        // Given
        SessionProperties properties = new SessionProperties();

        // When
        properties.setTimeout(3600);

        // Then
        assertEquals(3600, properties.getTimeout());
    }

    @Test
    @DisplayName("setTimeout_WithZero_ThrowsException")
    void testSetTimeout_WithZero_ThrowsException() {
        // Given
        SessionProperties properties = new SessionProperties();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> properties.setTimeout(0));
    }

    @Test
    @DisplayName("setTimeout_WithNegativeValue_ThrowsException")
    void testSetTimeout_WithNegativeValue_ThrowsException() {
        // Given
        SessionProperties properties = new SessionProperties();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> properties.setTimeout(-1));
    }

    @Test
    @DisplayName("setMaxSessions_WithValidValue_Succeeds")
    void testSetMaxSessions_WithValidValue_Succeeds() {
        // Given
        SessionProperties properties = new SessionProperties();

        // When
        properties.setMaxSessions(5);

        // Then
        assertEquals(5, properties.getMaxSessions());
    }

    @Test
    @DisplayName("setMaxSessions_WithNegativeOne_ForUnlimited_Succeeds")
    void testSetMaxSessions_WithNegativeOne_ForUnlimited_Succeeds() {
        // Given
        SessionProperties properties = new SessionProperties();

        // When
        properties.setMaxSessions(-1);

        // Then
        assertEquals(-1, properties.getMaxSessions());
    }

    @Test
    @DisplayName("isMemoryStore_WithMemoryStoreType_ReturnsTrue")
    void testIsMemoryStore_WithMemoryStoreType_ReturnsTrue() {
        // Given
        SessionProperties properties = new SessionProperties();
        properties.setStoreType("memory");

        // When/Then
        assertTrue(properties.isMemoryStore());
    }

    @Test
    @DisplayName("isMemoryStore_WithRedisStoreType_ReturnsFalse")
    void testIsMemoryStore_WithRedisStoreType_ReturnsFalse() {
        // Given
        SessionProperties properties = new SessionProperties();
        properties.setStoreType("redis");

        // When/Then
        assertFalse(properties.isMemoryStore());
    }

    @Test
    @DisplayName("isRedisStore_WithRedisStoreType_ReturnsTrue")
    void testIsRedisStore_WithRedisStoreType_ReturnsTrue() {
        // Given
        SessionProperties properties = new SessionProperties();
        properties.setStoreType("redis");

        // When/Then
        assertTrue(properties.isRedisStore());
    }

    @Test
    @DisplayName("isRedisStore_WithMemoryStoreType_ReturnsFalse")
    void testIsRedisStore_WithMemoryStoreType_ReturnsFalse() {
        // Given
        SessionProperties properties = new SessionProperties();
        properties.setStoreType("memory");

        // When/Then
        assertFalse(properties.isRedisStore());
    }

    @Test
    @DisplayName("setFixationProtection_UpdatesValue")
    void testSetFixationProtection_UpdatesValue() {
        // Given
        SessionProperties properties = new SessionProperties();

        // When
        properties.setFixationProtection(false);

        // Then
        assertFalse(properties.isFixationProtection());
    }
}
