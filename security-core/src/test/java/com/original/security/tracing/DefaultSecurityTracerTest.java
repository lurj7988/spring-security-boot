package com.original.security.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DefaultSecurityTracer 单元测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultSecurityTracerTest {

    @Mock
    private Tracer micrometerTracer;

    @Mock
    private Tracer.SpanInScope spanInScope;

    @Mock
    private Span mockSpan;

    @Mock
    private TraceContext mockContext;

    private DefaultSecurityTracer securityTracer;

    @BeforeEach
    void setUp() {
        lenient().when(mockSpan.context()).thenReturn(mockContext);
        lenient().when(mockContext.traceId()).thenReturn("test-trace-id");
        lenient().when(mockContext.spanId()).thenReturn("test-span-id");
        lenient().when(micrometerTracer.nextSpan()).thenReturn(mockSpan);
        lenient().when(micrometerTracer.currentSpan()).thenReturn(mockSpan);
        lenient().when(micrometerTracer.withSpan(any())).thenReturn(spanInScope);
        lenient().when(mockSpan.name(any())).thenReturn(mockSpan);
        lenient().when(mockSpan.tag(any(), any())).thenReturn(mockSpan);
        lenient().when(mockSpan.start()).thenReturn(mockSpan);
        lenient().when(mockSpan.event(any())).thenReturn(mockSpan);
        lenient().when(mockSpan.error(any())).thenReturn(mockSpan);

        securityTracer = new DefaultSecurityTracer(micrometerTracer, 3, 8);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("testConstructor_WithValidParams_CreatesInstance")
        void testConstructor_WithValidParams_CreatesInstance() {
            DefaultSecurityTracer tracer = new DefaultSecurityTracer(micrometerTracer, 5, 10);

            assertNotNull(tracer);
            assertTrue(tracer.isAvailable());
        }

        @Test
        @DisplayName("testConstructor_WithDefaultParams_CreatesInstance")
        void testConstructor_WithDefaultParams_CreatesInstance() {
            DefaultSecurityTracer tracer = new DefaultSecurityTracer(micrometerTracer);

            assertNotNull(tracer);
            assertTrue(tracer.isAvailable());
        }

        @Test
        @DisplayName("testConstructor_WithNullTracer_CreatesNoOpInstance")
        void testConstructor_WithNullTracer_CreatesNoOpInstance() {
            DefaultSecurityTracer tracer = new DefaultSecurityTracer(null);

            assertNotNull(tracer);
            assertFalse(tracer.isAvailable());
        }

        @Test
        @DisplayName("testConstructor_WithInvalidMaskLengths_UsesDefaults")
        void testConstructor_WithInvalidMaskLengths_UsesDefaults() {
            DefaultSecurityTracer tracer = new DefaultSecurityTracer(micrometerTracer, 0, -1);

            assertNotNull(tracer);
            // Uses default values when invalid
            assertEquals("abc*****", tracer.maskUsername("abcdefgh"));
            assertEquals("12345678********", tracer.maskToken("1234567890123456"));
        }
    }

    @Nested
    @DisplayName("Span Creation Tests")
    class SpanCreationTests {

        @Test
        @DisplayName("testStartSpan_WithValidName_ReturnsSpan")
        void testStartSpan_WithValidName_ReturnsSpan() {
            Span span = securityTracer.startSpan("test-span");

            assertNotNull(span);
            verify(micrometerTracer).nextSpan();
            verify(mockSpan).name("test-span");
            verify(mockSpan).start();
        }

        @Test
        @DisplayName("testStartSpan_WithTags_AddsTagsToSpan")
        void testStartSpan_WithTags_AddsTagsToSpan() {
            Map<String, String> tags = new HashMap<>();
            tags.put("key1", "value1");
            tags.put("key2", "value2");

            Span span = securityTracer.startSpan("test-span", tags);

            assertNotNull(span);
            verify(mockSpan).tag("key1", "value1");
            verify(mockSpan).tag("key2", "value2");
        }

        @Test
        @DisplayName("testStartSpan_WhenTracerNotAvailable_ReturnsNull")
        void testStartSpan_WhenTracerNotAvailable_ReturnsNull() {
            DefaultSecurityTracer noOpTracer = new DefaultSecurityTracer(null);

            Span span = noOpTracer.startSpan("test-span");

            assertNull(span);
        }
    }

    @Nested
    @DisplayName("Span Context Operations Tests")
    class SpanContextOperationsTests {

        @Test
        @DisplayName("testWithSpan_Supplier_ExecutesOperationAndReturnsResult")
        void testWithSpan_Supplier_ExecutesOperationAndReturnsResult() {
            String result = securityTracer.withSpan("test-span", () -> "success");

            assertEquals("success", result);
            verify(mockSpan).start();
            verify(mockSpan).end();
        }

        @Test
        @DisplayName("testWithSpan_Runnable_ExecutesOperation")
        void testWithSpan_Runnable_ExecutesOperation() {
            final boolean[] executed = {false};

            securityTracer.withSpan("test-span", () -> executed[0] = true);

            assertTrue(executed[0]);
            verify(mockSpan).start();
            verify(mockSpan).end();
        }

        @Test
        @DisplayName("testWithSpan_Exception_RecordsErrorAndRethrows")
        void testWithSpan_Exception_RecordsErrorAndRethrows() {
            RuntimeException exception = new RuntimeException("Test error");

            assertThrows(RuntimeException.class, () ->
                    securityTracer.withSpan("test-span", () -> {
                        throw exception;
                    })
            );

            verify(mockSpan).error(exception);
            verify(mockSpan).tag(TracingConstants.TAG_ERROR_TYPE, "RuntimeException");
            verify(mockSpan).end();
        }
    }

    @Nested
    @DisplayName("Security-Specific Spans Tests")
    class SecuritySpecificSpansTests {

        @Test
        @DisplayName("testStartAuthenticationSpan_CreatesSpanWithAuthTags")
        void testStartAuthenticationSpan_CreatesSpanWithAuthTags() {
            Span span = securityTracer.startAuthenticationSpan("jwt", "testuser");

            assertNotNull(span);
            verify(mockSpan).tag(TracingConstants.TAG_AUTH_TYPE, "jwt");
            verify(mockSpan).tag(TracingConstants.TAG_USERNAME, "tes*****");
        }

        @Test
        @DisplayName("testStartAuthenticationSpan_WithNullUsername_CreatesSpanWithoutUsername")
        void testStartAuthenticationSpan_WithNullUsername_CreatesSpanWithoutUsername() {
            Span span = securityTracer.startAuthenticationSpan("session", null);

            assertNotNull(span);
            verify(mockSpan).tag(TracingConstants.TAG_AUTH_TYPE, "session");
            verify(mockSpan, never()).tag(eq(TracingConstants.TAG_USERNAME), any());
        }

        @Test
        @DisplayName("testStartJwtValidationSpan_CreatesSpanWithJwtTags")
        void testStartJwtValidationSpan_CreatesSpanWithJwtTags() {
            Span span = securityTracer.startJwtValidationSpan("token-123456");

            assertNotNull(span);
            verify(mockSpan).tag(TracingConstants.TAG_AUTH_TYPE, TracingConstants.AUTH_TYPE_JWT);
            // Token mask length is 8, so "token-123456" becomes "token-12****"
            verify(mockSpan).tag(TracingConstants.TAG_JWT_TOKEN_ID, "token-12****");
        }

        @Test
        @DisplayName("testStartUserLoadSpan_CreatesSpanWithUsernameTag")
        void testStartUserLoadSpan_CreatesSpanWithUsernameTag() {
            Span span = securityTracer.startUserLoadSpan("admin");

            assertNotNull(span);
            verify(mockSpan).tag(TracingConstants.TAG_USERNAME, "adm**");
        }
    }

    @Nested
    @DisplayName("Context Access Tests")
    class ContextAccessTests {

        @Test
        @DisplayName("testGetCurrentSpan_ReturnsCurrentSpan")
        void testGetCurrentSpan_ReturnsCurrentSpan() {
            Span span = securityTracer.getCurrentSpan();

            assertNotNull(span);
            verify(micrometerTracer).currentSpan();
        }

        @Test
        @DisplayName("testGetCurrentTraceId_ReturnsTraceId")
        void testGetCurrentTraceId_ReturnsTraceId() {
            String traceId = securityTracer.getCurrentTraceId();

            assertEquals("test-trace-id", traceId);
        }

        @Test
        @DisplayName("testGetCurrentSpanId_ReturnsSpanId")
        void testGetCurrentSpanId_ReturnsSpanId() {
            String spanId = securityTracer.getCurrentSpanId();

            assertEquals("test-span-id", spanId);
        }

        @Test
        @DisplayName("testGetCurrentTraceId_WhenNoSpan_ReturnsNull")
        void testGetCurrentTraceId_WhenNoSpan_ReturnsNull() {
            when(micrometerTracer.currentSpan()).thenReturn(null);

            String traceId = securityTracer.getCurrentTraceId();

            assertNull(traceId);
        }
    }

    @Nested
    @DisplayName("Span Operations Tests")
    class SpanOperationsTests {

        @Test
        @DisplayName("testAddTag_AddsTagToCurrentSpan")
        void testAddTag_AddsTagToCurrentSpan() {
            securityTracer.addTag("test.key", "test.value");

            verify(mockSpan).tag("test.key", "test.value");
        }

        @Test
        @DisplayName("testAddEvent_WithName_AddsEvent")
        void testAddEvent_WithName_AddsEvent() {
            securityTracer.addEvent("test.event");

            verify(mockSpan).event("test.event");
        }

        @Test
        @DisplayName("testAddEvent_WithAttributes_AddsEventAndTags")
        void testAddEvent_WithAttributes_AddsEventAndTags() {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("attr1", "value1");

            securityTracer.addEvent("test.event", attributes);

            verify(mockSpan).event("test.event");
            verify(mockSpan).tag("attr1", "value1");
        }

        @Test
        @DisplayName("testRecordError_RecordsErrorOnCurrentSpan")
        void testRecordError_RecordsErrorOnCurrentSpan() {
            RuntimeException error = new RuntimeException("Test error");

            securityTracer.recordError(error);

            verify(mockSpan).error(error);
            verify(mockSpan).tag(TracingConstants.TAG_ERROR_TYPE, "RuntimeException");
        }
    }

    @Nested
    @DisplayName("Data Masking Tests")
    class DataMaskingTests {

        @Test
        @DisplayName("testMaskUsername_NullInput_ReturnsEmpty")
        void testMaskUsername_NullInput_ReturnsEmpty() {
            assertEquals("[EMPTY]", securityTracer.maskUsername(null));
        }

        @Test
        @DisplayName("testMaskUsername_EmptyInput_ReturnsEmpty")
        void testMaskUsername_EmptyInput_ReturnsEmpty() {
            assertEquals("[EMPTY]", securityTracer.maskUsername(""));
        }

        @Test
        @DisplayName("testMaskUsername_ShortInput_ReturnsAllAsterisks")
        void testMaskUsername_ShortInput_ReturnsAllAsterisks() {
            assertEquals("***", securityTracer.maskUsername("abc"));
            assertEquals("**", securityTracer.maskUsername("ab"));
        }

        @Test
        @DisplayName("testMaskUsername_LongInput_ReturnsMasked")
        void testMaskUsername_LongInput_ReturnsMasked() {
            assertEquals("tes*****", securityTracer.maskUsername("testuser"));
            assertEquals("adm***", securityTracer.maskUsername("admin1"));
        }

        @Test
        @DisplayName("testMaskToken_NullInput_ReturnsEmpty")
        void testMaskToken_NullInput_ReturnsEmpty() {
            assertEquals("[EMPTY]", securityTracer.maskToken(null));
        }

        @Test
        @DisplayName("testMaskToken_EmptyInput_ReturnsEmpty")
        void testMaskToken_EmptyInput_ReturnsEmpty() {
            assertEquals("[EMPTY]", securityTracer.maskToken(""));
        }

        @Test
        @DisplayName("testMaskToken_ShortInput_ReturnsAllAsterisks")
        void testMaskToken_ShortInput_ReturnsAllAsterisks() {
            assertEquals("********", securityTracer.maskToken("12345678"));
            assertEquals("*******", securityTracer.maskToken("1234567"));
        }

        @Test
        @DisplayName("testMaskToken_LongInput_ReturnsMasked")
        void testMaskToken_LongInput_ReturnsMasked() {
            assertEquals("12345678********", securityTracer.maskToken("1234567890123456"));
            assertEquals("abcdefgh***", securityTracer.maskToken("abcdefghijk"));
        }
    }

    @Nested
    @DisplayName("Availability Tests")
    class AvailabilityTests {

        @Test
        @DisplayName("testIsAvailable_WithTracer_ReturnsTrue")
        void testIsAvailable_WithTracer_ReturnsTrue() {
            assertTrue(securityTracer.isAvailable());
        }

        @Test
        @DisplayName("testIsAvailable_WithoutTracer_ReturnsFalse")
        void testIsAvailable_WithoutTracer_ReturnsFalse() {
            DefaultSecurityTracer noOpTracer = new DefaultSecurityTracer(null);

            assertFalse(noOpTracer.isAvailable());
        }
    }
}
