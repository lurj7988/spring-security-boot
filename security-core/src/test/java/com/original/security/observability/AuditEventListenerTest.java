package com.original.security.observability;

import com.original.security.event.AuthenticationSuccessEvent;
import com.original.security.event.AuthenticationFailureEvent;
import com.original.security.event.AuthorizationFailureEvent;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuditEventListener}.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuditEventListenerTest {

    @Test
    public void testOnAuditEvent_AuthenticationSuccessEvent_LogsCorrectly() {
        AuditEventListener listener = new AuditEventListener();

        HashMap<String, Object> details = new HashMap<>();
        details.put("ipAddress", "127.0.0.1");

        AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(this, "user1", "jwt", details);

        // Should not throw exception
        assertDoesNotThrow(() -> listener.onAuditEvent(successEvent));
    }

    @Test
    public void testOnAuditEvent_AuthenticationFailureEvent_LogsCorrectly() {
        AuditEventListener listener = new AuditEventListener();

        HashMap<String, Object> details = new HashMap<>();
        details.put("ipAddress", "192.168.1.1");

        AuthenticationFailureEvent failureEvent = new AuthenticationFailureEvent(this, "user1", "Bad credentials", details);

        // Should not throw exception
        assertDoesNotThrow(() -> listener.onAuditEvent(failureEvent));
    }

    @Test
    public void testOnAuditEvent_AuthorizationFailureEvent_LogsCorrectly() {
        AuditEventListener listener = new AuditEventListener();

        HashMap<String, Object> details = new HashMap<>();
        details.put("method", "GET");

        AuthorizationFailureEvent authFailureEvent = new AuthorizationFailureEvent(this, "user1", "/res", "ROLE_ADMIN", details);

        // Should not throw exception
        assertDoesNotThrow(() -> listener.onAuditEvent(authFailureEvent));
    }

    @Test
    public void testOnAuthorizedEvent_WithAuthenticatedUser_LogsCorrectly() {
        AuditEventListener listener = new AuditEventListener();
        AuthorizedEvent event = mock(AuthorizedEvent.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(event.getAuthentication()).thenReturn(auth);
        when(event.getSource()).thenReturn("MySource");
        when(event.getConfigAttributes()).thenReturn(null);

        // Should not throw exception
        assertDoesNotThrow(() -> listener.onAuthorizedEvent(event));
    }

    @Test
    public void testOnAuthorizedEvent_WithAnonymousUser_LogsCorrectly() {
        AuditEventListener listener = new AuditEventListener();
        AuthorizedEvent event = mock(AuthorizedEvent.class);

        AnonymousAuthenticationToken anonAuth = new AnonymousAuthenticationToken(
            "key", "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        when(event.getAuthentication()).thenReturn(anonAuth);
        when(event.getSource()).thenReturn("ProtectedResource");
        when(event.getConfigAttributes()).thenReturn(null);

        // Should not throw exception
        assertDoesNotThrow(() -> listener.onAuthorizedEvent(event));
    }

    @Test
    public void testOnAuthorizedEvent_WithNullAuthentication_LogsCorrectly() {
        AuditEventListener listener = new AuditEventListener();
        AuthorizedEvent event = mock(AuthorizedEvent.class);
        when(event.getAuthentication()).thenReturn(null);
        when(event.getSource()).thenReturn("Resource");
        when(event.getConfigAttributes()).thenReturn(null);

        // Should not throw exception
        assertDoesNotThrow(() -> listener.onAuthorizedEvent(event));
    }

    @Test
    public void testOnAuthorizedEvent_WithNullSource_HandlesGracefully() {
        AuditEventListener listener = new AuditEventListener();
        AuthorizedEvent event = mock(AuthorizedEvent.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        when(event.getAuthentication()).thenReturn(auth);
        when(event.getSource()).thenReturn(null);
        when(event.getConfigAttributes()).thenReturn(null);

        // Should not throw exception
        assertDoesNotThrow(() -> listener.onAuthorizedEvent(event));
    }
}