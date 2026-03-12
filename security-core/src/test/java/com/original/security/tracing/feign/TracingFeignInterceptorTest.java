package com.original.security.tracing.feign;

import com.original.security.tracing.SecurityTracer;
import com.original.security.tracing.TracingConstants;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TracingFeignInterceptor 单元测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class TracingFeignInterceptorTest {

    @Mock
    private SecurityTracer securityTracer;

    private TracingFeignInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new TracingFeignInterceptor(securityTracer);
    }

    @Nested
    @DisplayName("Context Propagation Tests")
    class ContextPropagationTests {

        @Test
        @DisplayName("testApply_TracerAvailable_PropagatesB3Headers")
        void testApply_TracerAvailable_PropagatesB3Headers() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn("abc123def456abc123def456abc123de");
            when(securityTracer.getCurrentSpanId()).thenReturn("1234567890abcdef");
            RequestTemplate template = new RequestTemplate();
            template.uri("/api/test");

            // Act
            interceptor.apply(template);

            // Assert
            assertTrue(template.headers().containsKey(TracingConstants.HEADER_B3_TRACE_ID));
            assertTrue(template.headers().containsKey(TracingConstants.HEADER_B3_SPAN_ID));
            assertTrue(template.headers().containsKey(TracingConstants.HEADER_B3_SAMPLED));
        }

        @Test
        @DisplayName("testApply_TracerAvailable_PropagatesW3CHeaders")
        void testApply_TracerAvailable_PropagatesW3CHeaders() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn("abc123def456abc123def456abc123de");
            when(securityTracer.getCurrentSpanId()).thenReturn("1234567890abcdef");
            RequestTemplate template = new RequestTemplate();
            template.uri("/api/test");

            // Act
            interceptor.apply(template);

            // Assert
            assertTrue(template.headers().containsKey(TracingConstants.HEADER_TRACEPARENT));
            // Verify W3C format: 00-{32位traceId}-{16位spanId}-01
            String traceparent = template.headers().get(TracingConstants.HEADER_TRACEPARENT)
                    .iterator().next();
            assertTrue(traceparent.startsWith("00-"));
            assertTrue(traceparent.endsWith("-01"));
            String[] parts = traceparent.split("-");
            assertEquals(4, parts.length);
            assertEquals(32, parts[1].length(), "trace-id should be 32 hex chars");
            assertEquals(16, parts[2].length(), "parent-id should be 16 hex chars");
        }

        @Test
        @DisplayName("testApply_TracerNotAvailable_SkipsPropagation")
        void testApply_TracerNotAvailable_SkipsPropagation() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(false);
            RequestTemplate template = new RequestTemplate();

            // Act
            interceptor.apply(template);

            // Assert
            assertFalse(template.headers().containsKey(TracingConstants.HEADER_B3_TRACE_ID));
            assertFalse(template.headers().containsKey(TracingConstants.HEADER_TRACEPARENT));
        }

        @Test
        @DisplayName("testApply_NoTraceContext_SkipsPropagation")
        void testApply_NoTraceContext_SkipsPropagation() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn(null);
            when(securityTracer.getCurrentSpanId()).thenReturn(null);
            RequestTemplate template = new RequestTemplate();

            // Act
            interceptor.apply(template);

            // Assert
            assertFalse(template.headers().containsKey(TracingConstants.HEADER_B3_TRACE_ID));
        }

        @Test
        @DisplayName("testApply_TraceIdNullButSpanIdExists_SkipsPropagation")
        void testApply_TraceIdNullButSpanIdExists_SkipsPropagation() {
            // Arrange - incomplete context: only spanId exists
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn(null);
            when(securityTracer.getCurrentSpanId()).thenReturn("1234567890abcdef");
            RequestTemplate template = new RequestTemplate();

            // Act
            interceptor.apply(template);

            // Assert - should NOT propagate incomplete context
            assertFalse(template.headers().containsKey(TracingConstants.HEADER_B3_TRACE_ID));
            assertFalse(template.headers().containsKey(TracingConstants.HEADER_TRACEPARENT));
        }

        @Test
        @DisplayName("testApply_SpanIdNullButTraceIdExists_SkipsPropagation")
        void testApply_SpanIdNullButTraceIdExists_SkipsPropagation() {
            // Arrange - incomplete context: only traceId exists
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn("abc123def456abc123def456abc123de");
            when(securityTracer.getCurrentSpanId()).thenReturn(null);
            RequestTemplate template = new RequestTemplate();

            // Act
            interceptor.apply(template);

            // Assert - should NOT propagate incomplete context
            assertFalse(template.headers().containsKey(TracingConstants.HEADER_B3_TRACE_ID));
            assertFalse(template.headers().containsKey(TracingConstants.HEADER_TRACEPARENT));
        }
    }

    @Nested
    @DisplayName("Security Tags Tests")
    class SecurityTagsTests {

        @Test
        @DisplayName("testApply_AddsSecurityTags")
        void testApply_AddsSecurityTags() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn("abc123");
            when(securityTracer.getCurrentSpanId()).thenReturn("def456");
            RequestTemplate template = new RequestTemplate();
            template.uri("/api/users");

            // Act
            interceptor.apply(template);

            // Assert
            verify(securityTracer).addTag(eq("feign.url"), anyString());
        }
    }

    @Nested
    @DisplayName("URL Masking Tests")
    class UrlMaskingTests {

        @Test
        @DisplayName("testApply_UrlWithToken_MasksSensitiveData")
        void testApply_UrlWithToken_MasksSensitiveData() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn("abc123");
            when(securityTracer.getCurrentSpanId()).thenReturn("def456");
            RequestTemplate template = new RequestTemplate();
            template.uri("/api/auth?token=secretvalue123");

            // Act
            interceptor.apply(template);

            // Assert - URL with token should be masked
            verify(securityTracer).addTag(eq("feign.url"), contains("token=****"));
        }

        @Test
        @DisplayName("testApply_UrlWithSecret_MasksSensitiveData")
        void testApply_UrlWithSecret_MasksSensitiveData() {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.getCurrentTraceId()).thenReturn("abc123");
            when(securityTracer.getCurrentSpanId()).thenReturn("def456");
            RequestTemplate template = new RequestTemplate();
            template.uri("/api/config?secret=mysecretkey");

            // Act
            interceptor.apply(template);

            // Assert - URL with secret should be masked
            verify(securityTracer).addTag(eq("feign.url"), contains("secret=****"));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("testCreate_ReturnsNewInstance")
        void testCreate_ReturnsNewInstance() {
            TracingFeignInterceptor created = TracingFeignInterceptor.create(securityTracer);
            assertNotNull(created);
        }
    }
}
