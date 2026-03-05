package com.original.security.event;

import org.springframework.context.ApplicationEvent;

/**
 * 会话踢出事件。
 * <p>
 * 当管理员强制用户下线或踢出特定会话时发布此事件。
 * 系统可以监听此事件进行后续处理，如记录审计日志、发送通知等。
 * </p>
 *
 * @author Naulu
 * @since 0.1.0
 */
public class SessionKickEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String sessionId;
    private final String operator;
    private final String reason;

    /**
     * 创建会话踢出事件。
     *
     * @param source 事件源
     * @param userId 被踢用户的 ID
     * @param sessionId 被踢会话的 ID
     * @param operator 操作人（管理员用户名）
     * @param reason 踢出原因
     */
    public SessionKickEvent(Object source, String userId, String sessionId, String operator, String reason) {
        super(source);
        this.userId = userId;
        this.sessionId = sessionId;
        this.operator = operator;
        this.reason = reason;
    }

    /**
     * 获取被踢用户的 ID。
     *
     * @return 用户 ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取被踢会话的 ID。
     *
     * @return 会话 ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 获取操作人。
     *
     * @return 操作人（管理员用户名）
     */
    public String getOperator() {
        return operator;
    }

    /**
     * 获取踢出原因。
     *
     * @return 踢出原因
     */
    public String getReason() {
        return reason;
    }
}
