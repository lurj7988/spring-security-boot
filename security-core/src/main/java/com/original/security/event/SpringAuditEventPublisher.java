package com.original.security.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring ApplicationEventPublisher 的审计事件发布器实现。
 * <p>
 * 将审计事件委托给 Spring 的事件发布机制，允许应用通过 @EventListener 监听审计事件。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class SpringAuditEventPublisher implements AuditEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringAuditEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("AuditEvent cannot be null");
        }
        eventPublisher.publishEvent(event);
    }
}