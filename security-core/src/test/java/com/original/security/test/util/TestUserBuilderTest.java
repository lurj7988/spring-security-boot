package com.original.security.test.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestUserBuilder 测试类。
 *
 * @author Claude
 * @since 1.0.0
 */
@DisplayName("TestUserBuilder Tests")
class TestUserBuilderTest {

    @AfterEach
    void tearDown() {
        AuthenticationTestUtils.clearAuthentication();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with valid username - Creates builder")
        void testConstructor_ValidUsername_CreatesBuilder() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("testuser");

            // Assert
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Constructor with empty username - Throws exception")
        void testConstructor_EmptyUsername_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new TestUserBuilder(""));
        }

        @Test
        @DisplayName("Constructor with null username - Throws exception")
        void testConstructor_NullUsername_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new TestUserBuilder(null));
        }
    }

    @Nested
    @DisplayName("Password Method Tests")
    class PasswordTests {

        @Test
        @DisplayName("Set password - Returns builder with password")
        void testPassword_ValidPassword_ReturnsBuilder() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("testuser")
                    .password("secret123");

            // Assert
            UserDetails userDetails = builder.buildUserDetails();
            assertEquals("secret123", userDetails.getPassword());
        }

        @Test
        @DisplayName("Set null password - Uses default password")
        void testPassword_NullPassword_UsesDefault() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("testuser")
                    .password(null);

            // Assert
            UserDetails userDetails = builder.buildUserDetails();
            assertEquals("password", userDetails.getPassword());
        }
    }

    @Nested
    @DisplayName("Roles Method Tests")
    class RolesTests {

        @Test
        @DisplayName("Add roles - Returns builder with roles")
        void testRoles_ValidRoles_ReturnsBuilderWithRoles() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("admin")
                    .roles("ADMIN", "USER");

            // Assert
            Authentication auth = builder.buildAuthentication();
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Add null roles - Handles gracefully")
        void testRoles_NullRoles_HandlesGracefully() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("testuser")
                    .roles((String[]) null);

            // Assert - Should have default ROLE_USER
            UserDetails userDetails = builder.buildUserDetails();
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Add empty role - Skips empty")
        void testRoles_EmptyRole_SkipsEmpty() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("testuser")
                    .roles("", "ADMIN", "");

            // Assert
            Authentication auth = builder.buildAuthentication();
            assertEquals(1, auth.getAuthorities().size());
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }
    }

    @Nested
    @DisplayName("Authorities Method Tests")
    class AuthoritiesTests {

        @Test
        @DisplayName("Add authorities - Returns builder with authorities")
        void testAuthorities_ValidAuthorities_ReturnsBuilderWithAuthorities() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("user")
                    .authorities("user:read", "user:write");

            // Assert
            Authentication auth = builder.buildAuthentication();
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("user:read")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("user:write")));
        }

        @Test
        @DisplayName("Add null authorities - Handles gracefully")
        void testAuthorities_NullAuthorities_HandlesGracefully() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("testuser")
                    .authorities((String[]) null);

            // Assert - Should have default ROLE_USER
            UserDetails userDetails = builder.buildUserDetails();
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }
    }

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Set enabled false - Creates disabled user")
        void testEnabled_False_CreatesDisabledUser() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("disabled")
                    .enabled(false);

            // Assert
            UserDetails userDetails = builder.buildUserDetails();
            assertFalse(userDetails.isEnabled());
        }

        @Test
        @DisplayName("Set accountNonExpired false - Creates expired account")
        void testAccountNonExpired_False_CreatesExpiredAccount() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("expired")
                    .accountNonExpired(false);

            // Assert
            UserDetails userDetails = builder.buildUserDetails();
            assertFalse(userDetails.isAccountNonExpired());
        }

        @Test
        @DisplayName("Set accountNonLocked false - Creates locked account")
        void testAccountNonLocked_False_CreatesLockedAccount() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("locked")
                    .accountNonLocked(false);

            // Assert
            UserDetails userDetails = builder.buildUserDetails();
            assertFalse(userDetails.isAccountNonLocked());
        }

        @Test
        @DisplayName("Set credentialsNonExpired false - Creates expired credentials")
        void testCredentialsNonExpired_False_CreatesExpiredCredentials() {
            // Act
            TestUserBuilder builder = new TestUserBuilder("expiredCreds")
                    .credentialsNonExpired(false);

            // Assert
            UserDetails userDetails = builder.buildUserDetails();
            assertFalse(userDetails.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Build Methods Tests")
    class BuildMethodsTests {

        @Test
        @DisplayName("buildUserDetails - Returns valid UserDetails")
        void testBuildUserDetails_ReturnsValidUserDetails() {
            // Act
            UserDetails userDetails = new TestUserBuilder("testuser")
                    .password("secret")
                    .roles("USER")
                    .buildUserDetails();

            // Assert
            assertNotNull(userDetails);
            assertEquals("testuser", userDetails.getUsername());
            assertEquals("secret", userDetails.getPassword());
            assertTrue(userDetails.isEnabled());
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("buildAuthentication - Returns valid Authentication")
        void testBuildAuthentication_ReturnsValidAuthentication() {
            // Act
            Authentication auth = new TestUserBuilder("admin")
                    .roles("ADMIN")
                    .buildAuthentication();

            // Assert
            assertNotNull(auth);
            assertEquals("admin", auth.getName());
            assertTrue(auth.isAuthenticated());
            assertNotNull(auth.getPrincipal());
            assertInstanceOf(UserDetails.class, auth.getPrincipal());
        }

        @Test
        @DisplayName("setupInContext - Sets authentication in SecurityContext")
        void testSetupInContext_SetsAuthentication() {
            // Act
            Authentication auth = new TestUserBuilder("testuser")
                    .roles("USER")
                    .setupInContext();

            // Assert
            Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(contextAuth);
            assertEquals("testuser", contextAuth.getName());
            assertEquals(auth, contextAuth);
        }
    }

    @Nested
    @DisplayName("Combined Roles and Authorities Tests")
    class CombinedRolesAndAuthoritiesTests {

        @Test
        @DisplayName("Roles and authorities together - Both are included")
        void testRolesAndAuthorities_BothIncluded() {
            // Act
            Authentication auth = new TestUserBuilder("admin")
                    .roles("ADMIN")
                    .authorities("user:read", "user:write")
                    .buildAuthentication();

            // Assert
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("user:read")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("user:write")));
            assertEquals(3, auth.getAuthorities().size());
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("No roles or authorities - Returns default ROLE_USER")
        void testNoRolesOrAuthorities_ReturnsDefaultRole() {
            // Act
            UserDetails userDetails = new TestUserBuilder("testuser")
                    .buildUserDetails();

            // Assert
            assertEquals(1, userDetails.getAuthorities().size());
            assertTrue(userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Default values - All account status are true")
        void testDefaultValues_AllAccountStatusTrue() {
            // Act
            UserDetails userDetails = new TestUserBuilder("testuser")
                    .buildUserDetails();

            // Assert
            assertTrue(userDetails.isEnabled());
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Role Prefix Tests")
    class RolePrefixTests {

        @Test
        @DisplayName("Role without ROLE_ prefix - Adds prefix automatically")
        void testRoleWithoutPrefix_AddsPrefixAutomatically() {
            // Act
            Authentication auth = new TestUserBuilder("admin")
                    .roles("ADMIN")
                    .buildAuthentication();

            // Assert
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("Role with ROLE_ prefix - Does not duplicate prefix")
        void testRoleWithPrefix_DoesNotDuplicatePrefix() {
            // Act
            Authentication auth = new TestUserBuilder("admin")
                    .roles("ROLE_ADMIN")
                    .buildAuthentication();

            // Assert
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            // Should not have ROLE_ROLE_ADMIN
            assertFalse(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ROLE_ADMIN")));
        }
    }
}
