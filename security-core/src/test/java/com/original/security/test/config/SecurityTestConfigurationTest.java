package com.original.security.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.handler.FrameAccessDeniedHandler;
import com.original.security.handler.FrameAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityTestConfiguration 测试类。
 *
 * @author Claude
 * @since 1.0.0
 */
@DisplayName("SecurityTestConfiguration Tests")
class SecurityTestConfigurationTest {

    private final SecurityTestConfiguration configuration = new SecurityTestConfiguration();

    @Nested
    @DisplayName("Bean Creation Tests")
    class BeanCreationTests {

        @Test
        @DisplayName("objectMapper - Creates ObjectMapper instance")
        void testObjectMapper_CreatesInstance() {
            // Act
            ObjectMapper mapper = configuration.objectMapper();

            // Assert
            assertNotNull(mapper);
        }

        @Test
        @DisplayName("passwordEncoder - Creates BCryptPasswordEncoder instance")
        void testPasswordEncoder_CreatesInstance() {
            // Act
            PasswordEncoder encoder = configuration.passwordEncoder();

            // Assert
            assertNotNull(encoder);
            assertTrue(encoder instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder);
        }

        @Test
        @DisplayName("authenticationEntryPoint - Creates FrameAuthenticationEntryPoint")
        void testAuthenticationEntryPoint_CreatesInstance() {
            // Arrange
            ObjectMapper objectMapper = new ObjectMapper();

            // Act
            FrameAuthenticationEntryPoint entryPoint = configuration.authenticationEntryPoint(objectMapper);

            // Assert
            assertNotNull(entryPoint);
        }

        @Test
        @DisplayName("accessDeniedHandler - Creates FrameAccessDeniedHandler")
        void testAccessDeniedHandler_CreatesInstance() {
            // Arrange
            ObjectMapper objectMapper = new ObjectMapper();

            // Act
            FrameAccessDeniedHandler handler = configuration.accessDeniedHandler(objectMapper, null);

            // Assert
            assertNotNull(handler);
        }
    }

    @Nested
    @DisplayName("PasswordEncoder Tests")
    class PasswordEncoderTests {

        @Test
        @DisplayName("Encode and match - Returns true for matching password")
        void testEncodeAndMatch_MatchingPassword_ReturnsTrue() {
            // Arrange
            PasswordEncoder encoder = configuration.passwordEncoder();
            String rawPassword = "testPassword123";

            // Act
            String encoded = encoder.encode(rawPassword);
            boolean matches = encoder.matches(rawPassword, encoded);

            // Assert
            assertTrue(matches);
        }

        @Test
        @DisplayName("Encode and match - Returns false for non-matching password")
        void testEncodeAndMatch_NonMatchingPassword_ReturnsFalse() {
            // Arrange
            PasswordEncoder encoder = configuration.passwordEncoder();
            String rawPassword = "testPassword123";
            String wrongPassword = "wrongPassword";

            // Act
            String encoded = encoder.encode(rawPassword);
            boolean matches = encoder.matches(wrongPassword, encoded);

            // Assert
            assertFalse(matches);
        }
    }
}
