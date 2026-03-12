package com.original.security.test.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthenticationTestUtils 测试类。
 *
 * @author Claude
 * @since 1.0.0
 */
@DisplayName("AuthenticationTestUtils Tests")
class AuthenticationTestUtilsTest {

    @AfterEach
    void tearDown() {
        AuthenticationTestUtils.clearAuthentication();
    }

    @Nested
    @DisplayName("mockAuthentication Method Tests")
    class MockAuthenticationTests {

        @Test
        @DisplayName("Mock with valid username and roles - Returns authentication")
        void testMockAuthentication_ValidUsername_ReturnsAuthentication() {
            // Act
            Authentication auth = AuthenticationTestUtils.mockAuthentication("admin", "ADMIN", "USER");

            // Assert
            assertNotNull(auth);
            assertEquals("admin", auth.getName());
            assertTrue(auth.isAuthenticated());
        }

        @Test
        @DisplayName("Mock with empty username - Throws exception")
        void testMockAuthentication_EmptyUsername_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    AuthenticationTestUtils.mockAuthentication(""));
        }

        @Test
        @DisplayName("Mock with null username - Throws exception")
        void testMockAuthentication_NullUsername_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    AuthenticationTestUtils.mockAuthentication(null));
        }

        @Test
        @DisplayName("Mock without roles - Returns default ROLE_USER")
        void testMockAuthentication_NoRoles_ReturnsDefaultRole() {
            // Act
            Authentication auth = AuthenticationTestUtils.mockAuthentication("user");

            // Assert
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Mock sets authentication in SecurityContext")
        void testMockAuthentication_SetsInSecurityContext() {
            // Act
            AuthenticationTestUtils.mockAuthentication("testuser", "TEST");

            // Assert
            Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(contextAuth);
            assertEquals("testuser", contextAuth.getName());
        }
    }

    @Nested
    @DisplayName("withUser Builder Tests")
    class WithUserBuilderTests {

        @Test
        @DisplayName("Build with username only - Returns valid builder")
        void testWithUser_ValidUsername_ReturnsBuilder() {
            // Act
            TestUserBuilder builder = AuthenticationTestUtils.withUser("testuser");

            // Assert
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Build with empty username - Throws exception")
        void testWithUser_EmptyUsername_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    AuthenticationTestUtils.withUser(""));
        }

        @Test
        @DisplayName("Build with roles - Returns authentication with roles")
        void testWithUser_WithRoles_ReturnsAuthenticationWithRoles() {
            // Act
            Authentication auth = AuthenticationTestUtils.withUser("admin")
                    .roles("ADMIN", "USER")
                    .buildAuthentication();

            // Assert
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Build with authorities - Returns authentication with authorities")
        void testWithUser_WithAuthorities_ReturnsAuthenticationWithAuthorities() {
            // Act
            Authentication auth = AuthenticationTestUtils.withUser("user")
                    .authorities("user:read", "user:write")
                    .buildAuthentication();

            // Assert
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("user:read")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("user:write")));
        }

        @Test
        @DisplayName("Build and setup in context - Sets authentication in SecurityContext")
        void testWithUser_SetupInContext_SetsAuthentication() {
            // Act
            AuthenticationTestUtils.withUser("testuser")
                    .roles("TEST")
                    .setupInContext();

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals("testuser", auth.getName());
        }

        @Test
        @DisplayName("Build disabled user - Returns disabled UserDetails")
        void testWithUser_Disabled_ReturnsDisabledUser() {
            // Act
            Authentication auth = AuthenticationTestUtils.withUser("disabled")
                    .enabled(false)
                    .buildAuthentication();

            // Assert
            Object principal = auth.getPrincipal();
            assertInstanceOf(org.springframework.security.core.userdetails.UserDetails.class, principal);
            assertFalse(((org.springframework.security.core.userdetails.UserDetails) principal).isEnabled());
        }
    }

    @Nested
    @DisplayName("mockJwtToken Method Tests")
    class MockJwtTokenTests {

        @Test
        @DisplayName("Mock JWT token with valid username - Returns token")
        void testMockJwtToken_ValidUsername_ReturnsToken() {
            // Act
            String token = AuthenticationTestUtils.mockJwtToken("testuser");

            // Assert
            assertNotNull(token);
            assertTrue(token.contains("."));
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length);
        }

        @Test
        @DisplayName("Mock JWT token with empty username - Throws exception")
        void testMockJwtToken_EmptyUsername_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    AuthenticationTestUtils.mockJwtToken(""));
        }

        @Test
        @DisplayName("Mock JWT token with roles - Contains roles in payload")
        void testMockJwtToken_WithRoles_ContainsRolesInPayload() {
            // Act
            String token = AuthenticationTestUtils.mockJwtToken("admin", "ADMIN", "USER");

            // Assert
            assertNotNull(token);
            // 解码 payload 并验证包含角色
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length);
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            assertTrue(payload.contains("admin"));
            assertTrue(payload.contains("ADMIN"));
            assertTrue(payload.contains("USER"));
        }
    }

    @Nested
    @DisplayName("clearAuthentication Method Tests")
    class ClearAuthenticationTests {

        @Test
        @DisplayName("Clear authentication - Removes from SecurityContext")
        void testClearAuthentication_RemovesFromContext() {
            // Arrange
            AuthenticationTestUtils.mockAuthentication("testuser");

            // Act
            AuthenticationTestUtils.clearAuthentication();

            // Assert
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
        }
    }

    @Nested
    @DisplayName("hasRole and hasAuthority Method Tests")
    class HasRoleAndAuthorityTests {

        @Test
        @DisplayName("hasRole with existing role - Returns true")
        void testHasRole_ExistingRole_ReturnsTrue() {
            // Arrange
            AuthenticationTestUtils.mockAuthentication("admin", "ADMIN");

            // Act & Assert
            assertTrue(AuthenticationTestUtils.hasRole("ADMIN"));
            assertTrue(AuthenticationTestUtils.hasRole("ROLE_ADMIN"));
        }

        @Test
        @DisplayName("hasRole with non-existing role - Returns false")
        void testHasRole_NonExistingRole_ReturnsFalse() {
            // Arrange
            AuthenticationTestUtils.mockAuthentication("user", "USER");

            // Act & Assert
            assertFalse(AuthenticationTestUtils.hasRole("ADMIN"));
        }

        @Test
        @DisplayName("hasAuthority with existing authority - Returns true")
        void testHasAuthority_ExistingAuthority_ReturnsTrue() {
            // Arrange
            AuthenticationTestUtils.withUser("user")
                    .authorities("user:read", "user:write")
                    .setupInContext();

            // Act & Assert
            assertTrue(AuthenticationTestUtils.hasAuthority("user:read"));
            assertTrue(AuthenticationTestUtils.hasAuthority("user:write"));
        }

        @Test
        @DisplayName("hasAuthority with non-existing authority - Returns false")
        void testHasAuthority_NonExistingAuthority_ReturnsFalse() {
            // Arrange
            AuthenticationTestUtils.mockAuthentication("user", "USER");

            // Act & Assert
            assertFalse(AuthenticationTestUtils.hasAuthority("admin:delete"));
        }
    }

    @Nested
    @DisplayName("getCurrentUsername Method Tests")
    class GetCurrentUsernameTests {

        @Test
        @DisplayName("Get current username when authenticated - Returns username")
        void testGetCurrentUsername_Authenticated_ReturnsUsername() {
            // Arrange
            AuthenticationTestUtils.mockAuthentication("testuser");

            // Act
            String username = AuthenticationTestUtils.getCurrentUsername();

            // Assert
            assertEquals("testuser", username);
        }

        @Test
        @DisplayName("Get current username when not authenticated - Returns null")
        void testGetCurrentUsername_NotAuthenticated_ReturnsNull() {
            // Arrange
            AuthenticationTestUtils.clearAuthentication();

            // Act
            String username = AuthenticationTestUtils.getCurrentUsername();

            // Assert
            assertNull(username);
        }
    }
}
