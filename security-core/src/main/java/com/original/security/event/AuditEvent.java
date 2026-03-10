package com.original.security.event;

import org.springframework.context.ApplicationEvent;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计事件基础类。
 * <p>
 * 为所有安全审计相关的事件提供统一的基础结构。
 * 所有审计事件继承此类，提供用户名、时间戳和详细信息的基础字段。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public abstract class AuditEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 匿名用户标识常量。
     */
    public static final String ANONYMOUS_USER = "anonymous";

    private final String username;
    private final Instant auditTimestamp;
    private final Map<String, Object> details;

    public AuditEvent(Object source, String username, Map<String, Object> details) {
        super(source);
        this.username = username != null ? username : ANONYMOUS_USER;
        this.auditTimestamp = Instant.now();
        // 防御性复制并过滤敏感信息
        this.details = filterSensitiveDetails(details != null ? details : Collections.emptyMap());
    }

    public String getUsername() {
        return username;
    }

    public Instant getAuditTimestamp() {
        return auditTimestamp;
    }

    /**
     * 获取事件详细信息。
     * <p>
     * 返回不可修改的 Map 视图，防止外部修改。
     * 敏感信息（如密码、token）已被过滤。
     *
     * @return 不可修改的详细信息 Map
     */
    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }

    /**
     * 获取事件类型标识。
     *
     * @return 事件类型字符串
     */
    public abstract String getEventType();

    /**
     * 过滤敏感信息，防止密码、token 等被记录到审计日志。
     *
     * @param original 原始详细信息
     * @return 过滤后的详细信息
     */
    private Map<String, Object> filterSensitiveDetails(Map<String, Object> original) {
        Map<String, Object> filtered = new HashMap<>(original);
        // 移除敏感字段
        filtered.remove("password");
        filtered.remove("pwd");
        filtered.remove("token");
        filtered.remove("accessToken");
        filtered.remove("refreshToken");
        filtered.remove("secret");
        filtered.remove("credential");
        filtered.remove("credentials");
        return filtered;
    }
}