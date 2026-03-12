package com.original.security.logging;

/**
 * 安全日志记录器接口。
 * <p>
 * 定义结构化安全日志的记录方法，支持多种日志级别和上下文传播。
 * 实现类应确保线程安全和性能优化。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface SecurityLogger {

    /**
     * 记录 DEBUG 级别日志。
     *
     * @param event 日志事件
     */
    void debug(SecurityLogEvent event);

    /**
     * 记录 DEBUG 级别日志。
     *
     * @param eventType 事件类型
     * @param message   日志消息
     */
    void debug(String eventType, String message);

    /**
     * 记录 INFO 级别日志。
     *
     * @param event 日志事件
     */
    void info(SecurityLogEvent event);

    /**
     * 记录 INFO 级别日志。
     *
     * @param eventType 事件类型
     * @param message   日志消息
     */
    void info(String eventType, String message);

    /**
     * 记录 WARN 级别日志。
     *
     * @param event 日志事件
     */
    void warn(SecurityLogEvent event);

    /**
     * 记录 WARN 级别日志。
     *
     * @param eventType 事件类型
     * @param message   日志消息
     */
    void warn(String eventType, String message);

    /**
     * 记录 ERROR 级别日志。
     *
     * @param event 日志事件
     */
    void error(SecurityLogEvent event);

    /**
     * 记录 ERROR 级别日志。
     *
     * @param eventType 事件类型
     * @param message   日志消息
     */
    void error(String eventType, String message);

    /**
     * 记录 ERROR 级别日志（带异常）。
     *
     * @param eventType 事件类型
     * @param message   日志消息
     * @param throwable 异常对象
     */
    void error(String eventType, String message, Throwable throwable);

    /**
     * 记录 ERROR 级别日志（带异常）。
     *
     * @param event     日志事件
     * @param throwable 异常对象
     */
    void error(SecurityLogEvent event, Throwable throwable);

    /**
     * 记录认证成功日志。
     *
     * @param username 用户名
     * @param message  日志消息
     */
    void logAuthenticationSuccess(String username, String message);

    /**
     * 记录认证失败日志。
     *
     * @param username 用户名
     * @param message  日志消息
     */
    void logAuthenticationFailure(String username, String message);

    /**
     * 记录认证失败日志（带异常）。
     *
     * @param username  用户名
     * @param message   日志消息
     * @param throwable 异常对象
     */
    void logAuthenticationFailure(String username, String message, Throwable throwable);

    /**
     * 记录授权失败日志。
     *
     * @param username   用户名
     * @param resource   资源
     * @param permission 所需权限
     * @param message    日志消息
     */
    void logAuthorizationFailure(String username, String resource, String permission, String message);

    /**
     * 设置 MDC 上下文。
     *
     * @param key   上下文键
     * @param value 上下文值
     */
    void putMdc(String key, String value);

    /**
     * 移除 MDC 上下文。
     *
     * @param key 上下文键
     */
    void removeMdc(String key);

    /**
     * 清除所有 MDC 上下文。
     */
    void clearMdc();
}
