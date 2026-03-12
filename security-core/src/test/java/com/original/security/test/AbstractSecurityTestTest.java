package com.original.security.test;

import com.original.security.test.util.AuthenticationTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AbstractSecurityTest 测试类。
 *
 * @author Claude
 * @since 1.0.0
 */
@DisplayName("AbstractSecurityTest Tests")
class AbstractSecurityTestTest {

    /**
     * 具体的测试子类实现。
     */
    private static class TestSecurityTest extends AbstractSecurityTest {
        // 继承所有方法用于测试
    }

    private final TestSecurityTest testInstance = new TestSecurityTest();

    @Nested
    @DisplayName("withAdmin Method Tests")
    class WithAdminTests {

        @Test
        @DisplayName("withAdmin default - Creates admin authentication")
        void testWithAdmin_Default_CreatesAdminAuth() {
            // Act
            Authentication auth = testInstance.withAdmin();

            // Assert
            assertNotNull(auth);
            assertEquals("admin", auth.getName());
            assertTrue(AuthenticationTestUtils.hasRole("ADMIN"));
        }

        @Test
        @DisplayName("withAdmin custom username - Creates authentication with custom username")
        void testWithAdmin_CustomUsername_CreatesCustomAuth() {
            // Act
            Authentication auth = testInstance.withAdmin("superadmin", "SUPERADMIN", "ADMIN");

            // Assert
            assertNotNull(auth);
            assertEquals("superadmin", auth.getName());
            assertTrue(AuthenticationTestUtils.hasRole("SUPERADMIN"));
            assertTrue(AuthenticationTestUtils.hasRole("ADMIN"));
        }
    }

    @Nested
    @DisplayName("withUser Method Tests")
    class WithUserTests {

        @Test
        @DisplayName("withUser default - Creates user authentication")
        void testWithUser_Default_CreatesUserAuth() {
            // Act
            Authentication auth = testInstance.withUser();

            // Assert
            assertNotNull(auth);
            assertEquals("user", auth.getName());
            assertTrue(AuthenticationTestUtils.hasRole("USER"));
        }

        @Test
        @DisplayName("withUser custom username - Creates authentication with custom username")
        void testWithUser_CustomUsername_CreatesCustomAuth() {
            // Act
            Authentication auth = testInstance.withUser("testuser", "USER", "GUEST");

            // Assert
            assertNotNull(auth);
            assertEquals("testuser", auth.getName());
            assertTrue(AuthenticationTestUtils.hasRole("USER"));
            assertTrue(AuthenticationTestUtils.hasRole("GUEST"));
        }
    }

    @Nested
    @DisplayName("withAuthorities Method Tests")
    class WithAuthoritiesTests {

        @Test
        @DisplayName("withAuthorities - Creates authentication with authorities")
        void testWithAuthorities_CreatesAuthWithAuthorities() {
            // Act
            Authentication auth = testInstance.withAuthorities("user", "user:read", "user:write");

            // Assert
            assertNotNull(auth);
            assertEquals("user", auth.getName());
            assertTrue(AuthenticationTestUtils.hasAuthority("user:read"));
            assertTrue(AuthenticationTestUtils.hasAuthority("user:write"));
        }
    }

    @Nested
    @DisplayName("Assertion Methods Tests")
    class AssertionMethodsTests {

        @Test
        @DisplayName("assertHasRole - Does not throw when role exists")
        void testAssertHasRole_RoleExists_NoException() {
            // Arrange
            testInstance.withAdmin();

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> testInstance.assertHasRole("ADMIN"));
        }

        @Test
        @DisplayName("assertHasRole - Throws when role does not exist")
        void testAssertHasRole_RoleNotExists_ThrowsException() {
            // Arrange
            testInstance.withUser();

            // Act & Assert
            AssertionError exception = assertThrows(AssertionError.class,
                    () -> testInstance.assertHasRole("ADMIN"));
            assertTrue(exception.getMessage().contains("ADMIN"));
        }

        @Test
        @DisplayName("assertDoesNotHaveRole - Does not throw when role does not exist")
        void testAssertDoesNotHaveRole_RoleNotExists_NoException() {
            // Arrange
            testInstance.withUser();

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> testInstance.assertDoesNotHaveRole("ADMIN"));
        }

        @Test
        @DisplayName("assertDoesNotHaveRole - Throws when role exists")
        void testAssertDoesNotHaveRole_RoleExists_ThrowsException() {
            // Arrange
            testInstance.withAdmin();

            // Act & Assert
            AssertionError exception = assertThrows(AssertionError.class,
                    () -> testInstance.assertDoesNotHaveRole("ADMIN"));
            assertTrue(exception.getMessage().contains("ADMIN"));
        }

        @Test
        @DisplayName("assertHasAuthority - Does not throw when authority exists")
        void testAssertHasAuthority_AuthExists_NoException() {
            // Arrange
            testInstance.withAuthorities("user", "user:read");

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> testInstance.assertHasAuthority("user:read"));
        }

        @Test
        @DisplayName("assertHasAuthority - Throws when authority does not exist")
        void testAssertHasAuthority_AuthNotExists_ThrowsException() {
            // Arrange
            testInstance.withUser();

            // Act & Assert
            AssertionError exception = assertThrows(AssertionError.class,
                    () -> testInstance.assertHasAuthority("admin:delete"));
            assertTrue(exception.getMessage().contains("admin:delete"));
        }

        @Test
        @DisplayName("assertAuthenticated - Does not throw when authenticated")
        void testAssertAuthenticated_IsAuthenticated_NoException() {
            // Arrange
            testInstance.withUser();

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> testInstance.assertAuthenticated());
        }

        @Test
        @DisplayName("assertAuthenticated - Throws when not authenticated")
        void testAssertAuthenticated_NotAuthenticated_ThrowsException() {
            // Arrange
            SecurityContextHolder.clearContext();

            // Act & Assert
            assertThrows(AssertionError.class, () -> testInstance.assertAuthenticated());
        }

        @Test
        @DisplayName("assertNotAuthenticated - Does not throw when not authenticated")
        void testAssertNotAuthenticated_NotAuthenticated_NoException() {
            // Arrange
            SecurityContextHolder.clearContext();

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> testInstance.assertNotAuthenticated());
        }

        @Test
        @DisplayName("assertNotAuthenticated - Throws when authenticated")
        void testAssertNotAuthenticated_IsAuthenticated_ThrowsException() {
            // Arrange
            testInstance.withUser();

            // Act & Assert
            assertThrows(AssertionError.class, () -> testInstance.assertNotAuthenticated());
        }
    }

    @Nested
    @DisplayName("getCurrentUsername Method Tests")
    class GetCurrentUsernameTests {

        @Test
        @DisplayName("getCurrentUsername - Returns current username")
        void testGetCurrentUsername_ReturnsUsername() {
            // Arrange
            testInstance.withUser("testuser", "USER");

            // Act
            String username = testInstance.getCurrentUsername();

            // Assert
            assertEquals("testuser", username);
        }
    }
}
