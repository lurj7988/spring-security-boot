package com.original.security.logging;

import com.original.security.event.AuthenticationFailureEvent;
import com.original.security.event.AuthenticationSuccessEvent;
import com.original.security.event.AuthorizationFailureEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SecurityLoggingListener 测试。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityLoggingListener Tests")
class SecurityLoggingListenerTest {

    @Mock
    private SecurityLogger securityLogger;

    private SecurityLoggingListener listener;

    @BeforeEach
    void setUp() {
        listener = new SecurityLoggingListener(securityLogger);
    }

    @Nested
    @DisplayName("Authentication Success Event Tests")
    class AuthenticationSuccessEventTests {

        @Test
        @DisplayName("Should log authentication success event with correct event type prefix")
        void testOnAuthenticationSuccess_LogsWithPrefixedEventType() {
            Map<String, Object> details = new HashMap<>();
            details.put("ipAddress", "192.168.1.1");

            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                    this, "testuser", "PASSWORD", details);

            listener.onAuthenticationSuccess(event);

            verify(securityLogger).info(argThat(logEvent ->
                    logEvent.getEventType().equals("SECURITY_AUTHENTICATION_SUCCESS") &&
                    logEvent.getField(SecurityLogField.USERNAME).equals("testuser") &&
                    logEvent.getField(SecurityLogField.SUCCESS).equals(true)
            ));
        }

        @Test
        @DisplayName("Should include authentication method in log event")
        void testOnAuthenticationSuccess_IncludesAuthMethod() {
            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                    this, "admin", "JWT", new HashMap<>());

            listener.onAuthenticationSuccess(event);

            verify(securityLogger).info(argThat(logEvent ->
                    "JWT".equals(logEvent.getField(SecurityLogField.AUTH_TYPE))
            ));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testOnAuthenticationSuccess_Exception_HandlesGracefully() {
            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                    this, "testuser", "PASSWORD", new HashMap<>());

            doThrow(new RuntimeException("Logger error"))
                    .when(securityLogger).info(any(SecurityLogEvent.class));

            // Should not throw exception
            listener.onAuthenticationSuccess(event);

            verify(securityLogger).info(any(SecurityLogEvent.class));
        }
    }

    @Nested
    @DisplayName("Authentication Failure Event Tests")
    class AuthenticationFailureEventTests {

        @Test
        @DisplayName("Should log authentication failure event with WARN level")
        void testOnAuthenticationFailure_LogsWithWarnLevel() {
            AuthenticationFailureEvent event = new AuthenticationFailureEvent(
                    this, "testuser", "Bad credentials", new HashMap<>());

            listener.onAuthenticationFailure(event);

            verify(securityLogger).warn(argThat(logEvent ->
                    logEvent.getEventType().equals("SECURITY_AUTHENTICATION_FAILURE") &&
                    logEvent.getField(SecurityLogField.USERNAME).equals("testuser") &&
                    logEvent.getField(SecurityLogField.SUCCESS).equals(false) &&
                    "Bad credentials".equals(logEvent.getField(SecurityLogField.ERROR_MESSAGE))
            ));
        }

        @Test
        @DisplayName("Should include failure details in log event")
        void testOnAuthenticationFailure_IncludesFailureDetails() {
            Map<String, Object> details = new HashMap<>();
            details.put("attemptCount", 3);

            AuthenticationFailureEvent event = new AuthenticationFailureEvent(
                    this, "lockeduser", "Account locked", details);

            listener.onAuthenticationFailure(event);

            verify(securityLogger).warn(argThat(logEvent ->
                    "Account locked".equals(logEvent.getField(SecurityLogField.ERROR_MESSAGE))
            ));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testOnAuthenticationFailure_Exception_HandlesGracefully() {
            AuthenticationFailureEvent event = new AuthenticationFailureEvent(
                    this, "testuser", "Bad credentials", new HashMap<>());

            doThrow(new RuntimeException("Logger error"))
                    .when(securityLogger).warn(any(SecurityLogEvent.class));

            // Should not throw exception
            listener.onAuthenticationFailure(event);

            verify(securityLogger).warn(any(SecurityLogEvent.class));
        }
    }

    @Nested
    @DisplayName("Authorization Failure Event Tests")
    class AuthorizationFailureEventTests {

        @Test
        @DisplayName("Should log authorization failure event with correct fields")
        void testOnAuthorizationFailure_LogsWithCorrectFields() {
            Map<String, Object> details = new HashMap<>();
            details.put("method", "GET");

            AuthorizationFailureEvent event = new AuthorizationFailureEvent(
                    this, "testuser", "/api/admin", "ROLE_ADMIN", details);

            listener.onAuthorizationFailure(event);

            verify(securityLogger).warn(argThat(logEvent ->
                    logEvent.getEventType().equals("SECURITY_AUTHORIZATION_FAILURE") &&
                    logEvent.getField(SecurityLogField.USERNAME).equals("testuser") &&
                    logEvent.getField(SecurityLogField.SUCCESS).equals(false) &&
                    "/api/admin".equals(logEvent.getField(SecurityLogField.RESOURCE)) &&
                    "ROLE_ADMIN".equals(logEvent.getField(SecurityLogField.PERMISSION))
            ));
        }

        @Test
        @DisplayName("Should include denial reason in log event")
        void testOnAuthorizationFailure_IncludesDenialReason() {
            AuthorizationFailureEvent event = new AuthorizationFailureEvent(
                    this, "testuser", "/api/secure", "ROLE_USER", new HashMap<>());

            listener.onAuthorizationFailure(event);

            verify(securityLogger).warn(argThat(logEvent ->
                    "Access Denied".equals(logEvent.getField(SecurityLogField.ERROR_MESSAGE))
            ));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testOnAuthorizationFailure_Exception_HandlesGracefully() {
            AuthorizationFailureEvent event = new AuthorizationFailureEvent(
                    this, "testuser", "/api/admin", "ROLE_ADMIN", new HashMap<>());

            doThrow(new RuntimeException("Logger error"))
                    .when(securityLogger).warn(any(SecurityLogEvent.class));

            // Should not throw exception
            listener.onAuthorizationFailure(event);

            verify(securityLogger).warn(any(SecurityLogEvent.class));
        }
    }
}
