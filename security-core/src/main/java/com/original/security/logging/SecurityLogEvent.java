package com.original.security.logging;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 结构化安全日志事件。
 * <p>
 * 封装安全相关日志的结构化数据，提供标准的日志字段和可扩展的自定义字段。
 * 所有敏感数据在构造时自动脱敏处理。
 * <p>
 * 示例用法：
 * <pre>{@code
 * SecurityLogEvent event = SecurityLogEvent.builder()
 *     .eventType("AUTHENTICATION_SUCCESS")
 *     .level(SecurityLogLevel.INFO)
 *     .username("admin")
 *     .success(true)
 *     .message("User authenticated successfully")
 *     .field(SecurityLogField.CLIENT_IP, "192.168.1.1")
 *     .build();
 * }</pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SecurityLogEvent {

    /**
     * 全局敏感数据脱敏器，使用 AtomicReference 保证线程安全。
     */
    private static final AtomicReference<SensitiveDataMasker> maskerRef =
            new AtomicReference<>(new SensitiveDataMasker());

    /**
     * 配置全局敏感数据脱敏模式。
     * <p>
     * 此方法应在应用启动时调用，由 SecurityLoggingAutoConfiguration 自动配置。
     * 使用 AtomicReference 确保线程安全的原子更新。
     *
     * @param maskingMode 脱敏模式
     */
    public static void setGlobalMaskingMode(SensitiveDataMasker.MaskingMode maskingMode) {
        maskerRef.set(new SensitiveDataMasker(maskingMode));
    }

    /**
     * 获取当前全局敏感数据脱敏器。
     *
     * @return 敏感数据脱敏器
     */
    public static SensitiveDataMasker getGlobalMasker() {
        return maskerRef.get();
    }

    private final String eventType;
    private final SecurityLogLevel level;
    private final Instant timestamp;
    private final String message;
    private final Map<String, Object> fields;

    /**
     * 私有构造函数，使用 Builder 创建实例。
     *
     * @param builder 构建器
     */
    private SecurityLogEvent(Builder builder) {
        this.eventType = builder.eventType;
        this.level = builder.level != null ? builder.level : SecurityLogLevel.INFO;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.message = builder.message;
        this.fields = Collections.unmodifiableMap(maskSensitiveFields(builder.fields));
    }

    /**
     * 创建新的构建器。
     *
     * @return 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 获取事件类型。
     *
     * @return 事件类型字符串
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * 获取日志级别。
     *
     * @return 日志级别枚举
     */
    public SecurityLogLevel getLevel() {
        return level;
    }

    /**
     * 获取时间戳。
     *
     * @return 事件时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * 获取消息。
     *
     * @return 日志消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取所有字段。
     * <p>
     * 返回不可修改的 Map 视图，所有敏感数据已脱敏。
     *
     * @return 不可修改的字段 Map
     */
    public Map<String, Object> getFields() {
        return fields;
    }

    /**
     * 获取指定字段的值。
     *
     * @param field 字段枚举
     * @param <T>   值类型
     * @return 字段值，如果不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getField(SecurityLogField field) {
        return (T) fields.get(field.getFieldName());
    }

    /**
     * 获取指定字段的值。
     *
     * @param fieldName 字段名称
     * @param <T>       值类型
     * @return 字段值，如果不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getField(String fieldName) {
        return (T) fields.get(fieldName);
    }

    /**
     * 转换为 Map 格式。
     * <p>
     * 包含所有标准字段和自定义字段，适合 JSON 序列化。
     *
     * @return 包含所有字段的 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put(SecurityLogField.EVENT_TYPE.getFieldName(), eventType);
        result.put(SecurityLogField.TIMESTAMP.getFieldName(), timestamp.toString());
        result.put(SecurityLogField.LEVEL.getFieldName(), level.getLevel());
        if (message != null) {
            result.put(SecurityLogField.MESSAGE.getFieldName(), message);
        }
        result.putAll(fields);
        return result;
    }

    /**
     * 对敏感字段进行脱敏处理。
     *
     * @param original 原始字段 Map
     * @return 脱敏后的字段 Map
     */
    private Map<String, Object> maskSensitiveFields(Map<String, Object> original) {
        Map<String, Object> masked = new HashMap<>();
        SensitiveDataMasker currentMasker = maskerRef.get();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                masked.put(key, null);
                continue;
            }

            if (currentMasker.isSensitiveField(key)) {
                // 如果是敏感字段，统一进行脱敏处理
                if (value instanceof String) {
                    masked.put(key, currentMasker.mask(key, (String) value));
                } else {
                    // 非字符串类型也进行脱敏（转换为字符串脱敏，或直接使用默认掩码）
                    masked.put(key, SensitiveDataMasker.DEFAULT_MASK);
                }
            } else {
                masked.put(key, value);
            }
        }
        return masked;
    }

    @Override
    public String toString() {
        return "SecurityLogEvent{" +
                "eventType='" + eventType + '\'' +
                ", level=" + level +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", fields=" + fields +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityLogEvent that = (SecurityLogEvent) o;
        return Objects.equals(eventType, that.eventType) &&
                level == that.level &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(message, that.message) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, level, timestamp, message, fields);
    }

    /**
     * SecurityLogEvent 构建器。
     */
    public static class Builder {
        private String eventType;
        private SecurityLogLevel level;
        private Instant timestamp;
        private String message;
        private final Map<String, Object> fields = new HashMap<>();

        /**
         * 设置事件类型。
         *
         * @param eventType 事件类型
         * @return 构建器
         */
        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        /**
         * 设置日志级别。
         *
         * @param level 日志级别
         * @return 构建器
         */
        public Builder level(SecurityLogLevel level) {
            this.level = level;
            return this;
        }

        /**
         * 设置时间戳。
         *
         * @param timestamp 时间戳
         * @return 构建器
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * 设置消息。
         *
         * @param message 日志消息
         * @return 构建器
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置用户名。
         *
         * @param username 用户名
         * @return 构建器
         */
        public Builder username(String username) {
            this.fields.put(SecurityLogField.USERNAME.getFieldName(), username);
            return this;
        }

        /**
         * 设置成功标识。
         *
         * @param success 是否成功
         * @return 构建器
         */
        public Builder success(boolean success) {
            this.fields.put(SecurityLogField.SUCCESS.getFieldName(), success);
            return this;
        }

        /**
         * 设置客户端 IP。
         *
         * @param clientIp 客户端 IP
         * @return 构建器
         */
        public Builder clientIp(String clientIp) {
            this.fields.put(SecurityLogField.CLIENT_IP.getFieldName(), clientIp);
            return this;
        }

        /**
         * 设置错误消息。
         *
         * @param errorMessage 错误消息
         * @return 构建器
         */
        public Builder errorMessage(String errorMessage) {
            this.fields.put(SecurityLogField.ERROR_MESSAGE.getFieldName(), errorMessage);
            return this;
        }

        /**
         * 设置错误码。
         *
         * @param errorCode 错误码
         * @return 构建器
         */
        public Builder errorCode(String errorCode) {
            this.fields.put(SecurityLogField.ERROR_CODE.getFieldName(), errorCode);
            return this;
        }

        /**
         * 添加自定义字段。
         *
         * @param field 字段枚举
         * @param value 字段值
         * @return 构建器
         */
        public Builder field(SecurityLogField field, Object value) {
            this.fields.put(field.getFieldName(), value);
            return this;
        }

        /**
         * 添加自定义字段。
         *
         * @param fieldName 字段名称
         * @param value     字段值
         * @return 构建器
         */
        public Builder field(String fieldName, Object value) {
            this.fields.put(fieldName, value);
            return this;
        }

        /**
         * 添加多个字段。
         *
         * @param additionalFields 字段 Map
         * @return 构建器
         */
        public Builder fields(Map<String, Object> additionalFields) {
            if (additionalFields != null) {
                this.fields.putAll(additionalFields);
            }
            return this;
        }

        /**
         * 构建 SecurityLogEvent 实例。
         *
         * @return SecurityLogEvent 实例
         * @throws IllegalArgumentException 如果 eventType 为空
         */
        public SecurityLogEvent build() {
            if (eventType == null || eventType.isEmpty()) {
                throw new IllegalArgumentException("eventType is required");
            }
            return new SecurityLogEvent(this);
        }
    }
}
