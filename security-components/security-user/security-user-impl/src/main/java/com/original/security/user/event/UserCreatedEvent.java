package com.original.security.user.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 用户创建事件
 * 用于审计日志记录
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class UserCreatedEvent extends ApplicationEvent {

    private final Long userId;
    private final String username;
    private final String email;
    private final LocalDateTime timestamp;

    public UserCreatedEvent(Object source, Long userId, String username, String email) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.timestamp = LocalDateTime.now();
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getEventTimestamp() {
        return timestamp;
    }
}
