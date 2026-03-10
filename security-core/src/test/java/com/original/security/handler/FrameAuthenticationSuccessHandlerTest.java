package com.original.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.config.SecurityMetricsProperties;
import com.original.security.observability.SecurityMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import java.io.IOException;

import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FrameAuthenticationSuccessHandler}.
 * Tests cover successful authentication response formatting and JSON output.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class FrameAuthenticationSuccessHandlerTest {

    private static final String METRIC_SUCCESS = "security.authentication.success";

    private FrameAuthenticationSuccessHandler successHandler;
    private ObjectMapper objectMapper;
    private ObjectProvider<com.original.security.util.JwtUtils> jwtUtilsProvider;
    private com.original.security.util.JwtUtils jwtUtils;
    private com.original.security.event.AuditEventPublisher auditEventPublisher;
    private MeterRegistry meterRegistry;
    private SecurityMetrics securityMetrics;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        objectMapper = new ObjectMapper();
        jwtUtilsProvider = mock(ObjectProvider.class);
        jwtUtils = mock(com.original.security.util.JwtUtils.class);
        auditEventPublisher = mock(com.original.security.event.AuditEventPublisher.class);
        meterRegistry = new SimpleMeterRegistry();
        SecurityMetricsProperties properties = new SecurityMetricsProperties();
        securityMetrics = new SecurityMetrics(meterRegistry, properties);
        when(jwtUtilsProvider.getIfAvailable()).thenReturn(jwtUtils);
        successHandler = new FrameAuthenticationSuccessHandler(objectMapper, jwtUtilsProvider, auditEventPublisher, securityMetrics);
    }

    @Test
    public void testOnAuthenticationSuccess_ValidAuthentication_ReturnsSuccessResponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "pass", java.util.Collections.emptyList());

        when(jwtUtils.generateToken(eq("admin"), any())).thenReturn("mocked.jwt.token");

        successHandler.onAuthenticationSuccess(request, response, auth);

        assertEquals(200, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(200, jsonNode.get("code").asInt());
        assertTrue(jsonNode.has("data"));
        assertTrue(jsonNode.get("data").has("user"));
        assertEquals("admin", jsonNode.get("data").get("user").asText());
        assertEquals("mocked.jwt.token", jsonNode.get("data").get("token").asText());
    }

    @Test
    public void testOnAuthenticationSuccess_WithAuthMethodAttribute_PublishesEvent() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("authMethod", "jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "pass", java.util.Collections.emptyList());

        when(jwtUtils.generateToken(eq("testuser"), any())).thenReturn("token");

        successHandler.onAuthenticationSuccess(request, response, auth);

        // 验证事件发布器被调用
        verify(auditEventPublisher, times(1)).publish(any(com.original.security.event.AuthenticationSuccessEvent.class));
    }

    @Test
    public void testOnAuthenticationSuccess_EventPublisherThrowsException_DoesNotFail() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "pass", java.util.Collections.emptyList());

        // 模拟事件发布器抛出异常
        doThrow(new RuntimeException("Event publisher failed")).when(auditEventPublisher).publish(any());

        // 这不应该抛出异常
        successHandler.onAuthenticationSuccess(request, response, auth);

        // 验证响应仍然成功
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testOnAuthenticationSuccess_NoJwtUtils_DoesNotIncludeToken() throws IOException, ServletException {
        // 重新创建不带 jwtUtils 的 handler
        successHandler = new FrameAuthenticationSuccessHandler(objectMapper, jwtUtilsProvider, auditEventPublisher, securityMetrics);
        when(jwtUtilsProvider.getIfAvailable()).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "pass", java.util.Collections.emptyList());

        successHandler.onAuthenticationSuccess(request, response, auth);

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertTrue(jsonNode.has("data"));
        assertFalse(jsonNode.get("data").has("token"));
    }

    @Test
    public void testOnAuthenticationSuccess_RecordsMetrics() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("authMethod", "username-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "pass", java.util.Collections.emptyList());

        successHandler.onAuthenticationSuccess(request, response, auth);

        // 验证 metrics 被记录
        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter, "Success counter should be registered");
        assertEquals(1.0, counter.count(), 0.001, "Should record 1 successful authentication");
    }
}
