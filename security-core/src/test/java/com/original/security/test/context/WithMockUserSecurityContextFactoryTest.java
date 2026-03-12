package com.original.security.test.context;

import com.original.security.test.annotation.WithMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WithMockUserSecurityContextFactory 测试类。
 *
 * @author Claude
 * @since 1.0.0
 */
@DisplayName("WithMockUserSecurityContextFactory Tests")
class WithMockUserSecurityContextFactoryTest {

    private final WithMockUserSecurityContextFactory factory = new WithMockUserSecurityContextFactory();

    @Nested
    @DisplayName("createSecurityContext Method Tests")
    class CreateSecurityContextTests {

        @Test
        @DisplayName("Create with default values - Returns valid context")
        void testCreateSecurityContext_DefaultValues_ReturnsValidContext() {
            // Arrange
            WithMockUser annotation = createTestAnnotation("user", "password", new String[]{}, new String[]{});

            // Act
            SecurityContext context = factory.createSecurityContext(annotation);

            // Assert
            assertNotNull(context);
            assertNotNull(context.getAuthentication());
            assertEquals("user", context.getAuthentication().getName());
        }

        @Test
        @DisplayName("Create with roles - Returns context with ROLE_ prefix")
        void testCreateSecurityContext_WithRoles_ReturnsWithRolePrefix() {
            // Arrange
            WithMockUser annotation = createTestAnnotation("admin", "password", new String[]{"ADMIN", "USER"}, new String[]{});

            // Act
            SecurityContext context = factory.createSecurityContext(annotation);

            // Assert
            Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }

        @Test
        @DisplayName("Create with authorities - Returns context without prefix")
        void testCreateSecurityContext_WithAuthorities_ReturnsWithoutPrefix() {
            // Arrange
            WithMockUser annotation = createTestAnnotation("user", "password", new String[]{}, new String[]{"user:read", "user:write"});

            // Act
            SecurityContext context = factory.createSecurityContext(annotation);

            // Assert
            Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("user:read")));
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("user:write")));
        }

        @Test
        @DisplayName("Create with roles and authorities - Returns both types")
        void testCreateSecurityContext_WithRolesAndAuthorities_ReturnsBoth() {
            // Arrange
            WithMockUser annotation = createTestAnnotation("admin", "password", new String[]{"ADMIN"}, new String[]{"user:read"});

            // Act
            SecurityContext context = factory.createSecurityContext(annotation);

            // Assert
            Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("user:read")));
        }

        @Test
        @DisplayName("Create with disabled account - Returns disabled user")
        void testCreateSecurityContext_DisabledAccount_ReturnsDisabledUser() {
            // Arrange
            WithMockUser annotation = createDisabledTestAnnotation();

            // Act
            SecurityContext context = factory.createSecurityContext(annotation);

            // Assert
            Object principal = context.getAuthentication().getPrincipal();
            assertInstanceOf(UserDetails.class, principal);
            assertFalse(((UserDetails) principal).isEnabled());
        }

        @Test
        @DisplayName("Create with no roles or authorities - Returns default ROLE_USER")
        void testCreateSecurityContext_NoRolesOrAuthorities_ReturnsDefaultRole() {
            // Arrange
            WithMockUser annotation = createTestAnnotation("user", "password", new String[]{}, new String[]{});

            // Act
            SecurityContext context = factory.createSecurityContext(annotation);

            // Assert
            Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
            assertEquals(1, authorities.size());
            assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        }
    }

    @Nested
    @DisplayName("UserDetails Tests")
    class UserDetailsTests {

        @Test
        @DisplayName("Authentication principal is UserDetails")
        void testAuthenticationPrincipal_IsUserDetails() {
            // Arrange
            WithMockUser annotation = createTestAnnotation("testuser", "testpass", new String[]{"TEST"}, new String[]{});

            // Act
            SecurityContext context = factory.createSecurityContext(annotation);

            // Assert
            Authentication auth = context.getAuthentication();
            assertInstanceOf(UserDetails.class, auth.getPrincipal());
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            assertEquals("testuser", userDetails.getUsername());
            assertEquals("testpass", userDetails.getPassword());
        }
    }

    /**
     * 创建测试用的 WithMockUser 注解实例。
     */
    private WithMockUser createTestAnnotation(String username, String password, String[] roles, String[] authorities) {
        return new WithMockUser() {
            @Override
            public String username() {
                return username;
            }

            @Override
            public String password() {
                return password;
            }

            @Override
            public String[] roles() {
                return roles;
            }

            @Override
            public String[] authorities() {
                return authorities;
            }

            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public boolean accountNonExpired() {
                return true;
            }

            @Override
            public boolean accountNonLocked() {
                return true;
            }

            @Override
            public boolean credentialsNonExpired() {
                return true;
            }

            @Override
            public String userDetailsClass() {
                return "";
            }

            @Override
            public org.springframework.security.test.context.support.TestExecutionEvent setupBefore() {
                return org.springframework.security.test.context.support.TestExecutionEvent.TEST_METHOD;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return WithMockUser.class;
            }
        };
    }

    /**
     * 创建禁用账户的测试注解实例。
     */
    private WithMockUser createDisabledTestAnnotation() {
        return new WithMockUser() {
            @Override
            public String username() {
                return "disabled";
            }

            @Override
            public String password() {
                return "password";
            }

            @Override
            public String[] roles() {
                return new String[]{};
            }

            @Override
            public String[] authorities() {
                return new String[]{};
            }

            @Override
            public boolean enabled() {
                return false;
            }

            @Override
            public boolean accountNonExpired() {
                return true;
            }

            @Override
            public boolean accountNonLocked() {
                return true;
            }

            @Override
            public boolean credentialsNonExpired() {
                return true;
            }

            @Override
            public String userDetailsClass() {
                return "";
            }

            @Override
            public org.springframework.security.test.context.support.TestExecutionEvent setupBefore() {
                return org.springframework.security.test.context.support.TestExecutionEvent.TEST_METHOD;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return WithMockUser.class;
            }
        };
    }
}
