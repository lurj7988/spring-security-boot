package com.original.security.core.authentication.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * SecurityUser 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class SecurityUserTest {

    @Test
    void testCreateSecurityUser() {
        SecurityUser user = SecurityUser.builder()
                .userId("user001")
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .roles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
                .permissions(Arrays.asList("READ", "WRITE"))
                .build();

        assertEquals("user001", user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("Test User", user.getDisplayName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(2, user.getRoles().size());
        assertEquals(2, user.getPermissions().size());
        assertEquals(SecurityUser.UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void testFromBuilder() {
        SecurityUser original = SecurityUser.builder()
                .userId("user001")
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .roles(Arrays.asList("ROLE_USER"))
                .permissions(Arrays.asList("READ"))
                .lastActiveTime(LocalDateTime.now())
                .build();

        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put("department", "IT");

        SecurityUser updated = SecurityUser.builder()
                .from(original)
                .roles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
                .attributes(newAttributes)
                .build();

        assertEquals("user001", updated.getUserId());
        assertEquals("testuser", updated.getUsername());
        assertEquals(2, updated.getRoles().size());
        assertEquals(1, updated.getPermissions().size());
        assertEquals("IT", updated.getAttributes().get("department"));
    }

    @Test
    void testEmptyUserId() {
        assertThrows(IllegalArgumentException.class, () -> {
            SecurityUser.builder()
                    .userId("")
                    .username("testuser")
                    .build();
        });
    }

    @Test
    void testNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> {
            SecurityUser.builder()
                    .userId(null)
                    .username("testuser")
                    .build();
        });
    }

    @Test
    void testEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            SecurityUser.builder()
                    .userId("user001")
                    .username("")
                    .build();
        });
    }

    @Test
    void testImmutableCollections() {
        SecurityUser user = SecurityUser.builder()
                .userId("user001")
                .username("testuser")
                .roles(Arrays.asList("ROLE_USER"))
                .permissions(Arrays.asList("READ"))
                .build();

        // 尝试修改角色列表应该抛出 UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            user.getRoles().add("ROLE_ADMIN");
        });

        // 尝试修改权限列表应该抛出 UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            user.getPermissions().add("WRITE");
        });

        // 尝试修改属性应该抛出 UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            user.getAttributes().put("key", "value");
        });
    }

    @Test
    void testUserStatusEnum() {
        assertEquals(SecurityUser.UserStatus.ACTIVE, SecurityUser.UserStatus.valueOf("ACTIVE"));
        assertEquals(SecurityUser.UserStatus.INACTIVE, SecurityUser.UserStatus.valueOf("INACTIVE"));
        assertEquals(SecurityUser.UserStatus.LOCKED, SecurityUser.UserStatus.valueOf("LOCKED"));
        assertEquals(SecurityUser.UserStatus.DISABLED, SecurityUser.UserStatus.valueOf("DISABLED"));
    }
}