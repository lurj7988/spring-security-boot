package com.original.security.tracing;

import com.original.security.tracing.config.SecurityTracingProperties;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AuthenticationTracingFilter 单元测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationTracingFilterTest {

    @Mock
    private SecurityTracer securityTracer;

    @Mock
    private Tracer tracer;
    
    @Mock
    private SecurityTracingProperties properties;

    @Mock
    private Tracer.SpanInScope spanInScope;

    @Mock
    private Span mockSpan;

    private AuthenticationTracingFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getIgnoredPaths()).thenReturn(Arrays.asList("/actuator/health", "/actuator/prometheus", "/static/", "/favicon.ico"));
        lenient().when(properties.getLoginPaths()).thenReturn(Arrays.asList("/login", "/api/login", "/auth/login"));
        filter = new AuthenticationTracingFilter(securityTracer, tracer, properties);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
        lenient().when(tracer.withSpan(any())).thenReturn(spanInScope);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Filter Execution Tests")
    class FilterExecutionTests {

        @Test
        @DisplayName("testDoFilterInternal_TracerAvailable_CreatesSpanAndExecutesChain")
        void testDoFilterInternal_TracerAvailable_CreatesSpanAndExecutesChain()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.setMethod("GET");
            request.setRequestURI("/api/test");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityTracer).startSpan(anyString(), anyMap());
            verify(securityTracer).addEvent(TracingConstants.EVENT_AUTH_START);
            verify(securityTracer).addEvent(TracingConstants.EVENT_AUTH_COMPLETE);
            verify(mockSpan).end();
        }

        @Test
        @DisplayName("testDoFilterInternal_TracerNotAvailable_SkipsTracing")
        void testDoFilterInternal_TracerNotAvailable_SkipsTracing()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(false);

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityTracer, never()).startSpan(anyString());
            verify(securityTracer, never()).startSpan(anyString(), anyMap());
        }

        @Test
        @DisplayName("testDoFilterInternal_ExceptionThrown_RecordsErrorAndRethrows")
        void testDoFilterInternal_ExceptionThrown_RecordsErrorAndRethrows()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            FilterChain failingChain = (req, res) -> {
                throw new RuntimeException("Test exception");
            };

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    filter.doFilterInternal(request, response, failingChain));

            verify(securityTracer).addTag(TracingConstants.TAG_AUTH_RESULT, TracingConstants.VALUE_FAILURE);
            verify(securityTracer).recordError(any(RuntimeException.class));
            verify(mockSpan).end();
        }
    }

    @Nested
    @DisplayName("Tag Building Tests")
    class TagBuildingTests {

        @Test
        @DisplayName("testBuildRequestTags_WithBearerToken_SetsJwtAuthType")
        void testBuildRequestTags_WithBearerToken_SetsJwtAuthType()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.addHeader("Authorization", "Bearer token123");
            request.setMethod("POST");
            request.setRequestURI("/api/login");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, String>> tagsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(securityTracer).startSpan(anyString(), tagsCaptor.capture());
            Map<String, String> tags = tagsCaptor.getValue();
            assertEquals(TracingConstants.AUTH_TYPE_JWT, tags.get(TracingConstants.TAG_AUTH_TYPE));
        }

        @Test
        @DisplayName("testBuildRequestTags_WithBasicAuth_SetsBasicAuthType")
        void testBuildRequestTags_WithBasicAuth_SetsBasicAuthType()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
            request.setMethod("POST");
            request.setRequestURI("/api/login");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, String>> tagsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(securityTracer).startSpan(anyString(), tagsCaptor.capture());
            Map<String, String> tags = tagsCaptor.getValue();
            assertEquals(TracingConstants.AUTH_TYPE_USERNAME_PASSWORD, tags.get(TracingConstants.TAG_AUTH_TYPE));
        }

        @Test
        @DisplayName("testBuildRequestTags_NoAuthHeader_NoAuthType")
        void testBuildRequestTags_NoAuthHeader_NoAuthType()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.setMethod("GET");
            request.setRequestURI("/api/public");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, String>> tagsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(securityTracer).startSpan(anyString(), tagsCaptor.capture());
            Map<String, String> tags = tagsCaptor.getValue();
            assertNull(tags.get(TracingConstants.TAG_AUTH_TYPE));
        }
    }

    @Nested
    @DisplayName("Authentication Success Tests")
    class AuthenticationSuccessTests {

        @Test
        @DisplayName("testRecordAuthenticationSuccess_AuthenticatedUser_RecordsSuccess")
        void testRecordAuthenticationSuccess_AuthenticatedUser_RecordsSuccess()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            when(securityTracer.maskUsername("testuser")).thenReturn("tes*****");

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "testuser", null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            request.setMethod("GET");
            request.setRequestURI("/api/login");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityTracer).addTag(TracingConstants.TAG_AUTH_RESULT, TracingConstants.VALUE_SUCCESS);
            verify(securityTracer).addTag(eq(TracingConstants.TAG_USERNAME), anyString());
        }

        @Test
        @DisplayName("testRecordAuthenticationSuccess_AnonymousUser_SkipsUsernameTag")
        void testRecordAuthenticationSuccess_AnonymousUser_SkipsUsernameTag()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "anonymousUser", null,
                    Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            request.setMethod("GET");
            request.setRequestURI("/api/login");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityTracer).addTag(TracingConstants.TAG_AUTH_RESULT, TracingConstants.VALUE_SUCCESS);
            verify(securityTracer, never()).addTag(eq(TracingConstants.TAG_USERNAME), anyString());
        }

        @Test
        @DisplayName("testRecordAuthenticationSuccess_NoAuthentication_SkipsRecording")
        void testRecordAuthenticationSuccess_NoAuthentication_SkipsRecording()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);

            // No authentication set
            request.setMethod("GET");
            request.setRequestURI("/api/test");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityTracer, never()).addTag(eq(TracingConstants.TAG_AUTH_RESULT), anyString());
        }
    }

    @Nested
    @DisplayName("Should Not Filter Tests")
    class ShouldNotFilterTests {

        @Test
        @DisplayName("testShouldNotFilter_ActuatorHealthPath_ReturnsTrue")
        void testShouldNotFilter_ActuatorHealthPath_ReturnsTrue() {
            // Arrange
            request.setRequestURI("/actuator/health");

            // Act
            boolean result = filter.shouldNotFilter(request);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("testShouldNotFilter_ActuatorPrometheusPath_ReturnsTrue")
        void testShouldNotFilter_ActuatorPrometheusPath_ReturnsTrue() {
            // Arrange
            request.setRequestURI("/actuator/prometheus");

            // Act
            boolean result = filter.shouldNotFilter(request);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("testShouldNotFilter_StaticPath_ReturnsTrue")
        void testShouldNotFilter_StaticPath_ReturnsTrue() {
            // Arrange
            request.setRequestURI("/static/css/style.css");

            // Act
            boolean result = filter.shouldNotFilter(request);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("testShouldNotFilter_FaviconPath_ReturnsTrue")
        void testShouldNotFilter_FaviconPath_ReturnsTrue() {
            // Arrange
            request.setRequestURI("/favicon.ico");

            // Act
            boolean result = filter.shouldNotFilter(request);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("testShouldNotFilter_ApiPath_ReturnsFalse")
        void testShouldNotFilter_ApiPath_ReturnsFalse() {
            // Arrange
            request.setRequestURI("/api/users");

            // Act
            boolean result = filter.shouldNotFilter(request);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("testShouldNotFilter_LoginPath_ReturnsFalse")
        void testShouldNotFilter_LoginPath_ReturnsFalse() {
            // Arrange
            request.setRequestURI("/login");

            // Act
            boolean result = filter.shouldNotFilter(request);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Span Name Building Tests")
    class SpanNameBuildingTests {

        @Test
        @DisplayName("testBuildSpanName_GetRequest_CorrectSpanName")
        void testBuildSpanName_GetRequest_CorrectSpanName()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.setMethod("GET");
            request.setRequestURI("/api/users");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityTracer).startSpan(eq("GET /api/users"), anyMap());
        }

        @Test
        @DisplayName("testBuildSpanName_PostRequest_CorrectSpanName")
        void testBuildSpanName_PostRequest_CorrectSpanName()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.setMethod("POST");
            request.setRequestURI("/api/login");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(securityTracer).startSpan(eq(TracingConstants.SPAN_AUTHENTICATION), anyMap());
        }

        @Test
        @DisplayName("testBuildSpanName_RequestWithQueryParams_RemovesQueryParams")
        void testBuildSpanName_RequestWithQueryParams_RemovesQueryParams()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.setMethod("GET");
            request.setRequestURI("/api/users?page=1&size=10&token=secret123");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert - Query params should be removed from span name
            verify(securityTracer).startSpan(eq("GET /api/users"), anyMap());
        }

        @Test
        @DisplayName("testBuildSpanName_RequestWithSensitiveQueryParam_NoSensitiveDataInSpanName")
        void testBuildSpanName_RequestWithSensitiveQueryParam_NoSensitiveDataInSpanName()
                throws ServletException, IOException {
            // Arrange
            when(securityTracer.isAvailable()).thenReturn(true);
            when(securityTracer.startSpan(anyString(), anyMap())).thenReturn(mockSpan);
            request.setMethod("POST");
            request.setRequestURI("/api/data?password=mypassword&token=secret");

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert - Span name should NOT contain sensitive query params
            ArgumentCaptor<String> spanNameCaptor = ArgumentCaptor.forClass(String.class);
            verify(securityTracer).startSpan(spanNameCaptor.capture(), anyMap());
            String spanName = spanNameCaptor.getValue();
            assertFalse(spanName.contains("password"));
            assertFalse(spanName.contains("secret"));
            assertFalse(spanName.contains("token"));
        }
    }
}
