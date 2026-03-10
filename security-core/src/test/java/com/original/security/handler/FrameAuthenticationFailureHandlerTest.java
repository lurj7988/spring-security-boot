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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FrameAuthenticationFailureHandler}.
 * Tests cover authentication failure response formatting for different exception types.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class FrameAuthenticationFailureHandlerTest {

    private static final String METRIC_FAILURE = "security.authentication.failure";

    private FrameAuthenticationFailureHandler failureHandler;
    private ObjectMapper objectMapper;
    private com.original.security.event.AuditEventPublisher auditEventPublisher;
    private MeterRegistry meterRegistry;
    private SecurityMetrics securityMetrics;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        auditEventPublisher = mock(com.original.security.event.AuditEventPublisher.class);
        meterRegistry = new SimpleMeterRegistry();
        SecurityMetricsProperties properties = new SecurityMetricsProperties();
        securityMetrics = new SecurityMetrics(meterRegistry, properties);
        failureHandler = new FrameAuthenticationFailureHandler(objectMapper, auditEventPublisher, securityMetrics);
    }

    @Test
    public void testOnAuthenticationFailure_BadCredentials_ReturnsUnauthorizedResponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(401, jsonNode.get("code").asInt());
        assertTrue(jsonNode.get("message").asText().contains("用户名或密码错误"));
    }

    @Test
    public void testOnAuthenticationFailure_DisabledAccount_ReturnsDisabledMessage() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        DisabledException exception = new DisabledException("User is disabled");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(401, jsonNode.get("code").asInt());
        assertTrue(jsonNode.get("message").asText().contains("账号已被禁用"));
    }

    @Test
    public void testOnAuthenticationFailure_WithUsernameParameter_PublishesEventWithUsername() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("username", "testuser");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        failureHandler.onAuthenticationFailure(request, response, exception);

        // 验证事件发布
        verify(auditEventPublisher, times(1)).publish(any(com.original.security.event.AuthenticationFailureEvent.class));
    }

    @Test
    public void testOnAuthenticationFailure_EventPublisherThrowsException_DoesNotFail() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // 模拟事件发布器抛出异常
        doThrow(new RuntimeException("Event publisher failed")).when(auditEventPublisher).publish(any());

        // 这不应该抛出异常
        failureHandler.onAuthenticationFailure(request, response, exception);

        // 验证响应仍然返回 401
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testOnAuthenticationFailure_OtherExceptionType_ReturnsGenericMessage() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new AuthenticationException("Generic auth error") {};

        failureHandler.onAuthenticationFailure(request, response, exception);

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(401, jsonNode.get("code").asInt());
        assertEquals("认证失败", jsonNode.get("message").asText());
    }

    @Test
    public void testOnAuthenticationFailure_RecordsMetrics() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("authMethod", "username-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        failureHandler.onAuthenticationFailure(request, response, exception);

        // 验证 metrics 被记录
        Counter counter = meterRegistry.find(METRIC_FAILURE).counter();
        assertNotNull(counter, "Failure counter should be registered");
        assertEquals(1.0, counter.count(), 0.001, "Should record 1 failed authentication");
    }

    @Test
    public void testOnAuthenticationFailure_RecordsMetricsWithFailureReason() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        DisabledException exception = new DisabledException("User is disabled");

        failureHandler.onAuthenticationFailure(request, response, exception);

        // 验证 metrics 被记录，并包含 failure_reason tag
        Counter counter = meterRegistry.find(METRIC_FAILURE).counter();
        assertNotNull(counter, "Failure counter should be registered");

        // 验证 tag 中包含 failure_reason
        assertTrue(counter.getId().getTags().stream()
                .anyMatch(t -> t.getKey().equals("failure_reason")),
                "Counter should have failure_reason tag");
    }
}
