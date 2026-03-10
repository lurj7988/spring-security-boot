package com.original.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.event.AuditEventPublisher;
import com.original.security.event.AuthorizationFailureEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfException;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FrameAccessDeniedHandler}.
 * Tests cover access denied response formatting and audit event publishing.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class FrameAccessDeniedHandlerTest {

    private FrameAccessDeniedHandler accessDeniedHandler;
    private ObjectMapper objectMapper;
    private AuditEventPublisher auditEventPublisher;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        auditEventPublisher = mock(AuditEventPublisher.class);
        accessDeniedHandler = new FrameAccessDeniedHandler(objectMapper, auditEventPublisher);
    }

    @Test
    public void testHandle_AccessDenied_ReturnsForbiddenResponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Setup authenticated user
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        accessDeniedHandler.handle(request, response, exception);

        assertEquals(403, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(403, jsonNode.get("code").asInt());
        assertTrue(jsonNode.get("message").asText().contains("拒绝访问"));
        // Response uses "path" as the JSON field name for location
        assertEquals("/api/admin", jsonNode.get("path").asText());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testHandle_CsrfException_ReturnsCsrfErrorMessage() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        CsrfException exception = new CsrfException("Invalid CSRF Token");

        accessDeniedHandler.handle(request, response, exception);

        assertEquals(403, response.getStatus());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertTrue(jsonNode.get("message").asText().contains("无效的 CSRF Token"));

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testHandle_AnonymousUser_PublishesAuditEventWithAnonymousUsername() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Setup anonymous user
        AnonymousAuthenticationToken anonAuth = new AnonymousAuthenticationToken(
            "key", "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(anonAuth);
        SecurityContextHolder.setContext(securityContext);

        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        accessDeniedHandler.handle(request, response, exception);

        // Verify audit event was published
        ArgumentCaptor<AuthorizationFailureEvent> eventCaptor = ArgumentCaptor.forClass(AuthorizationFailureEvent.class);
        verify(auditEventPublisher, times(1)).publish(eventCaptor.capture());

        AuthorizationFailureEvent capturedEvent = eventCaptor.getValue();
        assertEquals("anonymous", capturedEvent.getUsername());
        assertEquals("/api/admin", capturedEvent.getResource());
        assertNotNull(capturedEvent.getDetails().get("ipAddress"));

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testHandle_NoAuthentication_PublishesAuditEventWithAnonymousUsername() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // No authentication
        SecurityContextHolder.clearContext();

        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        accessDeniedHandler.handle(request, response, exception);

        // Verify audit event was published with anonymous username
        ArgumentCaptor<AuthorizationFailureEvent> eventCaptor = ArgumentCaptor.forClass(AuthorizationFailureEvent.class);
        verify(auditEventPublisher, times(1)).publish(eventCaptor.capture());

        AuthorizationFailureEvent capturedEvent = eventCaptor.getValue();
        assertEquals("anonymous", capturedEvent.getUsername());
    }

    @Test
    public void testHandle_AuditPublishFailure_DoesNotAffectResponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Make audit publisher throw exception
        doThrow(new RuntimeException("Audit system failure"))
            .when(auditEventPublisher).publish(any(AuthorizationFailureEvent.class));

        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        // Should not throw exception
        accessDeniedHandler.handle(request, response, exception);

        // Response should still be valid
        assertEquals(403, response.getStatus());

        // Cleanup
        SecurityContextHolder.clearContext();
    }
}
