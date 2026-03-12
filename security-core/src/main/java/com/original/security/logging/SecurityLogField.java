package com.original.security.logging;

/**
 * 安全日志标准字段定义。
 * <p>
 * 提供结构化日志中使用的常用字段名称，确保日志中关键信息的命名一致性。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public enum SecurityLogField {

    /**
     * 事件类型字段。
     */
    EVENT_TYPE("event_type"),

    /**
     * 时间戳字段。
     */
    TIMESTAMP("timestamp"),

    /**
     * 日志级别字段。
     */
    LEVEL("level"),

    /**
     * 消息字段。
     */
    MESSAGE("message"),

    /**
     * 用户名字段。
     */
    USERNAME("username"),

    /**
     * 成功标识字段。
     */
    SUCCESS("success"),

    /**
     * 客户端 IP 字段。
     */
    CLIENT_IP("client_ip"),

    /**
     * 请求 ID 字段（用于全链路追踪）。
     */
    REQUEST_ID("request_id"),

    /**
     * 会话 ID 字段。
     */
    SESSION_ID("session_id"),

    /**
     * 认证方式字段。
     */
    AUTH_TYPE("auth_type"),

    /**
     * 错误码字段。
     */
    ERROR_CODE("error_code"),

    /**
     * 错误消息字段。
     */
    ERROR_MESSAGE("error_message"),

    /**
     * 耗时字段（单位：毫秒）。
     */
    DURATION_MS("duration_ms"),

    /**
     * 资源路径字段。
     */
    RESOURCE("resource"),

    /**
     * 所需权限字段。
     */
    PERMISSION("permission"),

    /**
     * 角色字段。
     */
    ROLE("role"),

    /**
     * 用户代理字段。
     */
    USER_AGENT("user_agent"),

    /**
     * 环境标识字段。
     */
    ENVIRONMENT("environment");

    private final String fieldName;

    SecurityLogField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 获取字段名称。
     *
     * @return 字段名称字符串
     */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
