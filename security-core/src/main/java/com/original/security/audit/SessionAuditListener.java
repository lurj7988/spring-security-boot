package com.original.security.audit;

import com.original.security.event.SessionKickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 会话审计事件监听器。
 * <p>
 * 监听会话相关的审计事件并记录审计日志。
 * </p>
 *
 * @author Naulu
 * @since 0.1.0
 */
@Component
public class SessionAuditListener {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    /**
     * 处理会话踢出事件。
     *
     * @param event 会话踢出事件
     */
    @EventListener
    public void handleSessionKickEvent(SessionKickEvent event) {
        auditLog.info("AUDIT_SESSION_KICK: userId={}, sessionId={}, operator={}, reason={}, timestamp={}",
                event.getUserId(),
                event.getSessionId(),
                event.getOperator(),
                event.getReason(),
                System.currentTimeMillis());
    }
}
