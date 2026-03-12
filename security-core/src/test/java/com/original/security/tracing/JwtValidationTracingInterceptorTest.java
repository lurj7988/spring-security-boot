package com.original.security.tracing;

import io.micrometer.tracing.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JwtValidationTracingInterceptor 单元测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class JwtValidationTracingInterceptorTest {

    @Mock
    private SecurityTracer securityTracer;

    @Mock
    private Span mockSpan;

    private JwtValidationTracingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtValidationTracingInterceptor(securityTracer);
    }

    @Nested
    @DisplayName("JWT Validation Tests")
    class JwtValidationTests {

        @Test
        @DisplayName("testWithJwtValidation_Success_RecordsDurationAndResult")
        void testWithJwtValidation_Success_RecordsDurationAndResult() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            when(securityTracer.maskToken("test-token-123")).thenReturn("test-tok****");

            // Act
            String result = interceptor.withJwtValidation("test-token-123", () -> "validated");

            // Assert
            assertEquals("validated", result);
            verify(securityTracer).startSpan(eq(TracingConstants.SPAN_JWT_VALIDATION), anyMap());
            verify(securityTracer).addEvent(TracingConstants.EVENT_TOKEN_VALIDATE_START);
            verify(securityTracer).addEvent(TracingConstants.EVENT_TOKEN_VALIDATE_COMPLETE);
            verify(securityTracer).addTag(eq(TracingConstants.TAG_AUTH_RESULT), eq(TracingConstants.VALUE_SUCCESS));
            verify(securityTracer).addTag(eq(TracingConstants.TAG_DURATION_MS), anyString());
            verify(mockSpan).end();
        }

        @Test
        @DisplayName("testWithJwtValidation_TracerNotAvailable_ExecutesDirectly")
        void testWithJwtValidation_TracerNotAvailable_ExecutesDirectly() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(false);

            // Act
            String result = interceptor.withJwtValidation("test-token", () -> "direct");

            // Assert
            assertEquals("direct", result);
            verify(securityTracer, never()).startSpan(anyString(), anyMap());
        }

        @Test
        @DisplayName("testWithJwtValidation_Exception_RecordsErrorAndRethrows")
        void testWithJwtValidation_Exception_RecordsErrorAndRethrows() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            when(securityTracer.maskToken(anyString())).thenReturn("masked****");
            RuntimeException exception = new RuntimeException("Validation failed");

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    interceptor.withJwtValidation("test-token", () -> {
                        throw exception;
                    }));

            verify(securityTracer).addTag(TracingConstants.TAG_AUTH_RESULT, TracingConstants.VALUE_FAILURE);
            verify(securityTracer).addTag(TracingConstants.TAG_ERROR_TYPE, "RuntimeException");
            verify(securityTracer).recordError(exception);
            verify(mockSpan).end();
        }

        @Test
        @DisplayName("testWithJwtValidation_Runnable_ExecutesSuccessfully")
        void testWithJwtValidation_Runnable_ExecutesSuccessfully() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            when(securityTracer.maskToken(anyString())).thenReturn("masked****");
            final boolean[] executed = {false};

            // Act
            interceptor.withJwtValidation("test-token", () -> executed[0] = true);

            // Assert
            assertTrue(executed[0]);
            verify(mockSpan).end();
        }
    }

    @Nested
    @DisplayName("Token Refresh Tests")
    class TokenRefreshTests {

        @Test
        @DisplayName("testRecordTokenRefresh_AllTokenIds_RecordsAllTags")
        void testRecordTokenRefresh_AllTokenIds_RecordsAllTags() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);

            // Act
            interceptor.recordTokenRefresh("old-token", "new-token", "refresh-token");

            // Assert
            verify(securityTracer).withSpan(eq(TracingConstants.SPAN_JWT_REFRESH), any(Runnable.class));
        }

        @Test
        @DisplayName("testRecordTokenRefresh_TracerNotAvailable_SkipsRecording")
        void testRecordTokenRefresh_TracerNotAvailable_SkipsRecording() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(false);

            // Act
            interceptor.recordTokenRefresh("old", "new", "refresh");

            // Assert
            verify(securityTracer, never()).withSpan(anyString(), any(Runnable.class));
        }
    }

    @Nested
    @DisplayName("Token Creation Tests")
    class TokenCreationTests {

        @Test
        @DisplayName("testRecordTokenCreation_ValidData_RecordsTags")
        void testRecordTokenCreation_ValidData_RecordsTags() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);

            // Act
            interceptor.recordTokenCreation("testuser", "token-123");

            // Assert
            verify(securityTracer).withSpan(eq(TracingConstants.SPAN_JWT_CREATE), any(Runnable.class));
        }

        @Test
        @DisplayName("testRecordTokenCreation_TracerNotAvailable_SkipsRecording")
        void testRecordTokenCreation_TracerNotAvailable_SkipsRecording() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(false);

            // Act
            interceptor.recordTokenCreation("user", "token");

            // Assert
            verify(securityTracer, never()).withSpan(anyString(), any(Runnable.class));
        }

        @Test
        @DisplayName("testRecordTokenCreation_NullUsername_HandlesGracefully")
        void testRecordTokenCreation_NullUsername_HandlesGracefully() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);

            // Act - should not throw
            interceptor.recordTokenCreation(null, "token-123");

            // Assert
            verify(securityTracer).withSpan(eq(TracingConstants.SPAN_JWT_CREATE), any(Runnable.class));
        }
    }
}
