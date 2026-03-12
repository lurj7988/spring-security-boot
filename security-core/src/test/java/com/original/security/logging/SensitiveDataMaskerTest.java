package com.original.security.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SensitiveDataMasker 测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DisplayName("SensitiveDataMasker Tests")
class SensitiveDataMaskerTest {

    private final SensitiveDataMasker masker = new SensitiveDataMasker();

    @Nested
    @DisplayName("isSensitiveField method")
    class IsSensitiveField {

        @ParameterizedTest
        @ValueSource(strings = {"password", "PASSWORD", "Password", "pwd", "PWD", "secret", "SECRET", "token", "TOKEN"})
        @DisplayName("Should identify sensitive fields")
        void testIsSensitiveField_SensitiveFields_ReturnsTrue(String fieldName) {
            assertTrue(masker.isSensitiveField(fieldName));
        }

        @ParameterizedTest
        @ValueSource(strings = {"username", "email", "name", "id", "client_ip"})
        @DisplayName("Should not identify non-sensitive fields")
        void testIsSensitiveField_NonSensitiveFields_ReturnsFalse(String fieldName) {
            assertFalse(masker.isSensitiveField(fieldName));
        }

        @Test
        @DisplayName("Should return false for null field name")
        void testIsSensitiveField_NullFieldName_ReturnsFalse() {
            assertFalse(masker.isSensitiveField(null));
        }
    }

    @Nested
    @DisplayName("maskPassword method")
    class MaskPassword {

        @Test
        @DisplayName("Should completely mask password")
        void testMaskPassword_ValidPassword_ReturnsMasked() {
            String masked = masker.maskPassword("MySecretPassword123!");
            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testMaskPassword_NullInput_ReturnsNull() {
            assertNull(masker.maskPassword(null));
        }

        @Test
        @DisplayName("Should return empty for empty input")
        void testMaskPassword_EmptyInput_ReturnsEmpty() {
            assertEquals("", masker.maskPassword(""));
        }
    }

    @Nested
    @DisplayName("maskJwtToken method")
    class MaskJwtToken {

        @Test
        @DisplayName("Should mask JWT token showing first 10 characters")
        void testMaskJwtToken_ValidToken_ReturnsPartiallyMasked() {
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
            String masked = masker.maskJwtToken(token);

            assertTrue(masked.startsWith("eyJhbGciOi"));
            assertTrue(masked.endsWith("..."));
        }

        @Test
        @DisplayName("Should fully mask short token")
        void testMaskJwtToken_ShortToken_ReturnsFullyMasked() {
            String shortToken = "short";
            String masked = masker.maskJwtToken(shortToken);

            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testMaskJwtToken_NullInput_ReturnsNull() {
            assertNull(masker.maskJwtToken(null));
        }

        @Test
        @DisplayName("Should return empty for empty input")
        void testMaskJwtToken_EmptyInput_ReturnsEmpty() {
            assertEquals("", masker.maskJwtToken(""));
        }
    }

    @Nested
    @DisplayName("maskPhone method")
    class MaskPhone {

        @Test
        @DisplayName("Should mask phone showing first 3 and last 4 digits")
        void testMaskPhone_ValidPhone_ReturnsPartiallyMasked() {
            String masked = masker.maskPhone("13812345678");

            assertEquals("138****5678", masked);
        }

        @Test
        @DisplayName("Should fully mask short phone")
        void testMaskPhone_ShortPhone_ReturnsFullyMasked() {
            String masked = masker.maskPhone("123456");

            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testMaskPhone_NullInput_ReturnsNull() {
            assertNull(masker.maskPhone(null));
        }
    }

    @Nested
    @DisplayName("maskEmail method")
    class MaskEmail {

        @Test
        @DisplayName("Should mask email showing first 2 characters and domain")
        void testMaskEmail_ValidEmail_ReturnsPartiallyMasked() {
            String masked = masker.maskEmail("test@example.com");

            assertEquals("te***@example.com", masked);
        }

        @Test
        @DisplayName("Should fully mask short email prefix")
        void testMaskEmail_ShortPrefix_ReturnsFullyMasked() {
            String masked = masker.maskEmail("a@b.com");

            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testMaskEmail_NullInput_ReturnsNull() {
            assertNull(masker.maskEmail(null));
        }
    }

    @Nested
    @DisplayName("maskCardNumber method")
    class MaskCardNumber {

        @Test
        @DisplayName("Should mask card number showing first 4 and last 4 digits")
        void testMaskCardNumber_ValidCard_ReturnsPartiallyMasked() {
            String masked = masker.maskCardNumber("6222021234567890");

            assertEquals("6222****7890", masked);
        }

        @Test
        @DisplayName("Should fully mask short card number")
        void testMaskCardNumber_ShortCard_ReturnsFullyMasked() {
            String masked = masker.maskCardNumber("1234567");

            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testMaskCardNumber_NullInput_ReturnsNull() {
            assertNull(masker.maskCardNumber(null));
        }
    }

    @Nested
    @DisplayName("maskIdCard method")
    class MaskIdCard {

        @Test
        @DisplayName("Should mask ID card showing first 4 and last 4 digits")
        void testMaskIdCard_ValidIdCard_ReturnsPartiallyMasked() {
            String masked = masker.maskIdCard("110101199001011234");

            assertEquals("1101****1234", masked);
        }

        @Test
        @DisplayName("Should fully mask short ID card")
        void testMaskIdCard_ShortIdCard_ReturnsFullyMasked() {
            String masked = masker.maskIdCard("1234567");

            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testMaskIdCard_NullInput_ReturnsNull() {
            assertNull(masker.maskIdCard(null));
        }
    }

    @Nested
    @DisplayName("mask method")
    class Mask {

        @Test
        @DisplayName("Should mask password field")
        void testMask_PasswordField_ReturnsMasked() {
            String masked = masker.mask("password", "secretPassword");
            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should mask token field")
        void testMask_TokenField_ReturnsMasked() {
            String masked = masker.mask("token", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0In0.test");
            assertTrue(masked.startsWith("eyJhbGciOi"));
        }

        @Test
        @DisplayName("Should not mask non-sensitive field")
        void testMask_NonSensitiveField_ReturnsOriginal() {
            String masked = masker.mask("username", "testuser");
            assertEquals("testuser", masked);
        }

        @Test
        @DisplayName("Should return null for null value")
        void testMask_NullValue_ReturnsNull() {
            assertNull(masker.mask("field", null));
        }

        @Test
        @DisplayName("Should return empty for empty value")
        void testMask_EmptyValue_ReturnsEmpty() {
            assertEquals("", masker.mask("field", ""));
        }
    }

    @Nested
    @DisplayName("autoMask method")
    class AutoMask {

        @Test
        @DisplayName("Should auto-detect and mask JWT token")
        void testAutoMask_JwtToken_ReturnsMasked() {
            String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0In0.test";
            String masked = masker.autoMask(jwt);

            assertTrue(masked.startsWith("eyJhbGciOi"));
        }

        @Test
        @DisplayName("Should auto-detect and mask phone number")
        void testAutoMask_PhoneNumber_ReturnsMasked() {
            String masked = masker.autoMask("13812345678");
            assertEquals("138****5678", masked);
        }

        @Test
        @DisplayName("Should auto-detect and mask email")
        void testAutoMask_Email_ReturnsMasked() {
            String masked = masker.autoMask("test@example.com");
            assertEquals("te***@example.com", masked);
        }

        @Test
        @DisplayName("Should return default mask for unrecognized pattern")
        void testAutoMask_UnrecognizedPattern_ReturnsDefaultMask() {
            String masked = masker.autoMask("randomtext123");
            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testAutoMask_NullInput_ReturnsNull() {
            assertNull(masker.autoMask(null));
        }
    }

    @Nested
    @DisplayName("maskCustom method")
    class MaskCustom {

        @Test
        @DisplayName("Should mask with custom prefix and suffix")
        void testMaskCustom_ValidInput_ReturnsCustomMasked() {
            String masked = masker.maskCustom("abcdefghij", 2, 3);
            assertEquals("ab****hij", masked);
        }

        @Test
        @DisplayName("Should fully mask when length equals prefix + suffix")
        void testMaskCustom_ShortInput_ReturnsFullyMasked() {
            String masked = masker.maskCustom("abcde", 2, 3);
            assertEquals("******", masked);
        }

        @Test
        @DisplayName("Should return null for null input")
        void testMaskCustom_NullInput_ReturnsNull() {
            assertNull(masker.maskCustom(null, 2, 2));
        }
    }
}
