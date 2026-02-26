package com.original.security.core.authentication;

import com.original.security.core.authentication.user.SecurityUser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthenticationResult 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class AuthenticationResultTest {

    @Test
    void testSuccessResult() {
        SecurityUser user = SecurityUser.builder()
                .userId("user001")
                .username("testuser")
                .displayName("Test User")
                .email("test@example.com")
                .roles(Collections.singletonList("ROLE_USER"))
                .permissions(Collections.singletonList("READ"))
                .build();

        Map<String, Object> details = new HashMap<>();
        details.put("loginTime", "2024-01-01T10:00:00");

        AuthenticationResult result = AuthenticationResult.success(user, details);

        assertTrue(result.isSuccess());
        assertEquals(user, result.getUser());
        assertEquals(details, result.getDetails());
        assertNull(result.getErrorMessage());
        assertNull(result.getErrorCode());
    }

    @Test
    void testFailureResult() {
        String errorMessage = "Authentication failed";
        String errorCode = "AUTH_FAILED";

        AuthenticationResult result = AuthenticationResult.failure(errorMessage, errorCode);

        assertFalse(result.isSuccess());
        assertNull(result.getUser());
        assertNull(result.getDetails());
        assertEquals(errorMessage, result.getErrorMessage());
        assertEquals(errorCode, result.getErrorCode());
    }

    @Test
    void testBuilderPattern() {
        SecurityUser user = SecurityUser.builder()
                .userId("user001")
                .username("testuser")
                .displayName("Test User")
                .build();

        Map<String, Object> details = Collections.singletonMap("key", "value");

        AuthenticationResult result = new AuthenticationResult.Builder()
                .success(true)
                .user(user)
                .details(details)
                .build();

        assertTrue(result.isSuccess());
        assertEquals(user, result.getUser());
        assertEquals(details, result.getDetails());
    }

    @Test
    void testSecurityUserBuilderValidation() {
        // 测试用户 ID 为空时的验证
        assertThrows(IllegalArgumentException.class, () -> {
            SecurityUser.builder()
                    .username("testuser")
                    .build();
        });
    }
}