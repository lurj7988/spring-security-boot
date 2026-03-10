package com.original.security.event;

/**
 * 审计事件发布器接口。
 * <p>
 * 为框架提供统一的事件发布入口。
 * 实现类应将事件委托给 Spring 的 {@link org.springframework.context.ApplicationEventPublisher}。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface AuditEventPublisher {

    /**
     * 发布审计事件。
     *
     * @param event 审计事件，不能为 null
     * @throws IllegalArgumentException 如果 event 为 null
     */
    void publish(AuditEvent event);
}