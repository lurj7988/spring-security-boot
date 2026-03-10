package com.original.security.event;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SpringAuditEventPublisher}.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SpringAuditEventPublisherTest {

    @Test
    public void testPublishEvent_ValidEvent_DelegatesToEventPublisher() {
        ApplicationEventPublisher mockPublisher = mock(ApplicationEventPublisher.class);
        SpringAuditEventPublisher auditPublisher = new SpringAuditEventPublisher(mockPublisher);

        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(this, "user1", "jwt", new HashMap<>());
        auditPublisher.publish(event);

        verify(mockPublisher, times(1)).publishEvent(event);
    }

    @Test
    public void testPublishEvent_NullEvent_ThrowsIllegalArgumentException() {
        ApplicationEventPublisher mockPublisher = mock(ApplicationEventPublisher.class);
        SpringAuditEventPublisher auditPublisher = new SpringAuditEventPublisher(mockPublisher);

        assertThrows(IllegalArgumentException.class, () -> {
            auditPublisher.publish(null);
        });

        verify(mockPublisher, never()).publishEvent(any());
    }

    @Test
    public void testPublishEvent_MultipleEvents_AllDelegated() {
        ApplicationEventPublisher mockPublisher = mock(ApplicationEventPublisher.class);
        SpringAuditEventPublisher auditPublisher = new SpringAuditEventPublisher(mockPublisher);

        AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(this, "user1", "jwt", new HashMap<>());
        AuthenticationFailureEvent failureEvent = new AuthenticationFailureEvent(this, "user2", "Bad credentials", new HashMap<>());

        auditPublisher.publish(successEvent);
        auditPublisher.publish(failureEvent);

        verify(mockPublisher, times(2)).publishEvent(any(AuditEvent.class));
    }
}