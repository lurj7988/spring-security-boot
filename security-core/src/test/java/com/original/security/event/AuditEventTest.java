package com.original.security.event;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for audit event classes.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuditEventTest {

    @Test
    public void testAuthenticationSuccessEvent_ValidInput_AllFieldsCorrect() {
        Map<String, Object> details = new HashMap<>();
        details.put("ipAddress", "127.0.0.1");

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, "user1", "jwt", details);

        assertEquals("user1", event.getUsername());
        assertEquals("jwt", event.getAuthenticationMethod());
        assertEquals("127.0.0.1", event.getDetails().get("ipAddress"));
        assertNotNull(event.getAuditTimestamp());
        assertEquals("AUTHENTICATION_SUCCESS", event.getEventType());
    }

    @Test
    public void testAuthenticationFailureEvent_ValidInput_AllFieldsCorrect() {
        Map<String, Object> details = new HashMap<>();

        AuthenticationFailureEvent event = new AuthenticationFailureEvent(this, "user1", "Bad credentials", details);

        assertEquals("user1", event.getUsername());
        assertEquals("Bad credentials", event.getFailureReason());
        assertNotNull(event.getAuditTimestamp());
        assertEquals("AUTHENTICATION_FAILURE", event.getEventType());
    }

    @Test
    public void testAuthorizationFailureEvent_ValidInput_AllFieldsCorrect() {
        Map<String, Object> details = new HashMap<>();

        AuthorizationFailureEvent event = new AuthorizationFailureEvent(this, "user1", "/api/admin", "ROLE_ADMIN", details);

        assertEquals("user1", event.getUsername());
        assertEquals("/api/admin", event.getResource());
        assertEquals("ROLE_ADMIN", event.getRequiredPermission());
        assertNotNull(event.getAuditTimestamp());
        assertEquals("AUTHORIZATION_FAILURE", event.getEventType());
    }

    @Test
    public void testAuditEvent_NullUsername_DefaultsToAnonymous() {
        Map<String, Object> details = new HashMap<>();
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, null, "jwt", details);

        assertEquals("anonymous", event.getUsername());
    }

    @Test
    public void testAuditEvent_NullDetails_ReturnsEmptyMap() {
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, "user1", "jwt", null);

        assertNotNull(event.getDetails());
        assertTrue(event.getDetails().isEmpty());
    }

    @Test
    public void testAuditEvent_DetailsAreUnmodifiable() {
        Map<String, Object> details = new HashMap<>();
        details.put("key", "value");
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, "user1", "jwt", details);

        assertThrows(UnsupportedOperationException.class, () -> {
            event.getDetails().put("newKey", "newValue");
        });
    }

    @Test
    public void testAuditEvent_SensitivePasswordIsFiltered() {
        Map<String, Object> details = new HashMap<>();
        details.put("password", "secret123");
        details.put("ipAddress", "127.0.0.1");

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, "user1", "jwt", details);

        assertFalse(event.getDetails().containsKey("password"));
        assertEquals("127.0.0.1", event.getDetails().get("ipAddress"));
    }

    @Test
    public void testAuditEvent_SensitiveTokenIsFiltered() {
        Map<String, Object> details = new HashMap<>();
        details.put("token", "bearer.jwt.token");
        details.put("accessToken", "access.token");
        details.put("refreshToken", "refresh.token");

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, "user1", "jwt", details);

        assertFalse(event.getDetails().containsKey("token"));
        assertFalse(event.getDetails().containsKey("accessToken"));
        assertFalse(event.getDetails().containsKey("refreshToken"));
    }

    @Test
    public void testAuditEvent_SecretAndCredentialsAreFiltered() {
        Map<String, Object> details = new HashMap<>();
        details.put("secret", "mysecret");
        details.put("credential", "mycredential");
        details.put("credentials", "mycredentials");

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, "user1", "jwt", details);

        assertFalse(event.getDetails().containsKey("secret"));
        assertFalse(event.getDetails().containsKey("credential"));
        assertFalse(event.getDetails().containsKey("credentials"));
    }

    @Test
    public void testAuthorizationFailureEvent_ExtractUsername_NullAuthentication() {
        String username = AuthorizationFailureEvent.extractUsername(null);
        assertEquals("anonymous", username);
    }

    @Test
    public void testAuthorizationFailureEvent_GetRequiredAuthority_DeprecatedButWorks() {
        Map<String, Object> details = new HashMap<>();
        AuthorizationFailureEvent event = new AuthorizationFailureEvent(this, "user1", "/api/admin", "ROLE_ADMIN", details);

        // Deprecated method should still work
        assertEquals("ROLE_ADMIN", event.getRequiredAuthority());
        assertEquals(event.getRequiredPermission(), event.getRequiredAuthority());
    }

    @Test
    public void testAnonymousUserConstant() {
        assertEquals("anonymous", AuditEvent.ANONYMOUS_USER);
    }
}