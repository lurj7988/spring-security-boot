package com.original.security.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 默认安全日志记录器实现。
 * <p>
 * 基于 SLF4J 和 Logback 实现结构化日志记录，支持 MDC 上下文传播。
 * 所有日志事件以 JSON 格式输出，便于日志收集和分析。
 * <p>
 * 线程安全：所有方法都是线程安全的。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class DefaultSecurityLogger implements SecurityLogger {

    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityLogger.class);

    /**
     * 事件类型前缀。
     */
    private static final String EVENT_TYPE_PREFIX = "SECURITY_";

    /**
     * 认证成功事件类型。
     */
    private static final String AUTH_SUCCESS_EVENT = "AUTHENTICATION_SUCCESS";

    /**
     * 认证失败事件类型。
     */
    private static final String AUTH_FAILURE_EVENT = "AUTHENTICATION_FAILURE";

    /**
     * 授权失败事件类型。
     */
    private static final String AUTHZ_FAILURE_EVENT = "AUTHORIZATION_FAILURE";

    private final ObjectMapper objectMapper;
    private final boolean jsonOutput;
    private final boolean includeStackTrace;

    /**
     * 构造函数（默认使用 JSON 输出）。
     */
    public DefaultSecurityLogger() {
        this(new ObjectMapper(), true, true);
    }

    /**
     * 构造函数。
     *
     * @param objectMapper JSON 序列化器
     * @param jsonOutput   是否使用 JSON 格式输出
     */
    public DefaultSecurityLogger(ObjectMapper objectMapper, boolean jsonOutput) {
        this(objectMapper, jsonOutput, true);
    }

    /**
     * 构造函数。
     *
     * @param objectMapper      JSON 序列化器
     * @param jsonOutput        是否使用 JSON 格式输出
     * @param includeStackTrace 是否包含堆栈跟踪（仅在 ERROR 级别）
     */
    public DefaultSecurityLogger(ObjectMapper objectMapper, boolean jsonOutput, boolean includeStackTrace) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.jsonOutput = jsonOutput;
        this.includeStackTrace = includeStackTrace;
    }

    /**
     * 是否使用 JSON 格式输出。
     *
     * @return true 表示使用 JSON 格式
     */
    public boolean isJsonOutput() {
        return jsonOutput;
    }

    /**
     * 是否包含堆栈跟踪。
     *
     * @return true 表示包含堆栈跟踪
     */
    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }

    @Override
    public void debug(SecurityLogEvent event) {
        if (log.isDebugEnabled()) {
            log.debug(formatLogMessage(event));
        }
    }

    @Override
    public void debug(String eventType, String message) {
        if (log.isDebugEnabled()) {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + eventType)
                    .level(SecurityLogLevel.DEBUG)
                    .message(message)
                    .build();
            log.debug(formatLogMessage(event));
        }
    }

    @Override
    public void info(SecurityLogEvent event) {
        if (log.isInfoEnabled()) {
            log.info(formatLogMessage(event));
        }
    }

    @Override
    public void info(String eventType, String message) {
        if (log.isInfoEnabled()) {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + eventType)
                    .level(SecurityLogLevel.INFO)
                    .message(message)
                    .build();
            log.info(formatLogMessage(event));
        }
    }

    @Override
    public void warn(SecurityLogEvent event) {
        if (log.isWarnEnabled()) {
            log.warn(formatLogMessage(event));
        }
    }

    @Override
    public void warn(String eventType, String message) {
        if (log.isWarnEnabled()) {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + eventType)
                    .level(SecurityLogLevel.WARN)
                    .message(message)
                    .build();
            log.warn(formatLogMessage(event));
        }
    }

    @Override
    public void error(SecurityLogEvent event) {
        if (log.isErrorEnabled()) {
            log.error(formatLogMessage(event));
        }
    }

    @Override
    public void error(String eventType, String message) {
        if (log.isErrorEnabled()) {
            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + eventType)
                    .level(SecurityLogLevel.ERROR)
                    .message(message)
                    .build();
            log.error(formatLogMessage(event));
        }
    }

    @Override
    public void error(String eventType, String message, Throwable throwable) {
        if (log.isErrorEnabled()) {
            String errorMessage;
            if (throwable == null) {
                errorMessage = "Unknown error (null throwable)";
            } else {
                errorMessage = throwable.getMessage() != null
                        ? throwable.getMessage()
                        : throwable.getClass().getSimpleName();
            }

            SecurityLogEvent event = SecurityLogEvent.builder()
                    .eventType(EVENT_TYPE_PREFIX + eventType)
                    .level(SecurityLogLevel.ERROR)
                    .message(message)
                    .field(SecurityLogField.ERROR_MESSAGE, errorMessage)
                    .build();

            if (includeStackTrace && throwable != null) {
                log.error(formatLogMessage(event), throwable);
            } else {
                log.error(formatLogMessage(event));
            }
        }
    }
    @Override
    public void error(SecurityLogEvent event, Throwable throwable) {
        if (log.isErrorEnabled()) {
            if (includeStackTrace) {
                log.error(formatLogMessage(event), throwable);
            } else {
                log.error(formatLogMessage(event));
            }
        }
    }

    @Override
    public void logAuthenticationSuccess(String username, String message) {
        SecurityLogEvent event = SecurityLogEvent.builder()
                .eventType(EVENT_TYPE_PREFIX + AUTH_SUCCESS_EVENT)
                .level(SecurityLogLevel.INFO)
                .username(username)
                .success(true)
                .message(message)
                .build();
        info(event);
    }

    @Override
    public void logAuthenticationFailure(String username, String message) {
        SecurityLogEvent event = SecurityLogEvent.builder()
                .eventType(EVENT_TYPE_PREFIX + AUTH_FAILURE_EVENT)
                .level(SecurityLogLevel.WARN)
                .username(username)
                .success(false)
                .message(message)
                .build();
        warn(event);
    }

    @Override
    public void logAuthenticationFailure(String username, String message, Throwable throwable) {
        String errorMsg = (throwable != null && throwable.getMessage() != null)
                ? throwable.getMessage()
                : (throwable != null ? throwable.getClass().getSimpleName() : "Unknown error");
        SecurityLogEvent event = SecurityLogEvent.builder()
                .eventType(EVENT_TYPE_PREFIX + AUTH_FAILURE_EVENT)
                .level(SecurityLogLevel.ERROR)
                .username(username)
                .success(false)
                .message(message)
                .errorMessage(errorMsg)
                .build();
        error(event, throwable);
    }

    @Override
    public void logAuthorizationFailure(String username, String resource, String permission, String message) {
        SecurityLogEvent event = SecurityLogEvent.builder()
                .eventType(EVENT_TYPE_PREFIX + AUTHZ_FAILURE_EVENT)
                .level(SecurityLogLevel.WARN)
                .username(username)
                .success(false)
                .field(SecurityLogField.RESOURCE, resource)
                .field(SecurityLogField.PERMISSION, permission)
                .message(message)
                .build();
        warn(event);
    }

    @Override
    public void putMdc(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    @Override
    public void removeMdc(String key) {
        if (key != null) {
            MDC.remove(key);
        }
    }

    @Override
    public void clearMdc() {
        MDC.clear();
    }

    /**
     * 格式化日志消息。
     * <p>
     * 根据 jsonOutput 配置，输出 JSON 格式或普通格式的日志消息。
     *
     * @param event 日志事件
     * @return 格式化后的日志消息
     */
    private String formatLogMessage(SecurityLogEvent event) {
        if (jsonOutput) {
            try {
                return objectMapper.writeValueAsString(event.toMap());
            } catch (JsonProcessingException e) {
                // JSON 序列化失败，降级为简单格式
                log.warn("Failed to serialize log event to JSON: {}", e.getMessage());
                return event.toString();
            }
        }
        return event.toString();
    }
}
