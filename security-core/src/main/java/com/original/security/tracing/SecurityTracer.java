package com.original.security.tracing;

import io.micrometer.tracing.Span;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 安全组件追踪器接口。
 * <p>
 * 提供安全相关操作的分布式追踪能力，支持创建和管理追踪 Span。
 * 此接口是对 Micrometer Tracing API 的封装，提供安全组件专用的追踪功能。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface SecurityTracer {

    // ========== Span Creation ==========

    /**
     * 创建一个新的 Span。
     *
     * @param spanName Span 名称
     * @return 创建的 Span
     */
    Span startSpan(String spanName);

    /**
     * 创建一个带有初始标签的 Span。
     *
     * @param spanName Span 名称
     * @param tags     初始标签
     * @return 创建的 Span
     */
    Span startSpan(String spanName, Map<String, String> tags);

    // ========== Span Context Operations ==========

    /**
     * 在指定 Span 上下文中执行操作。
     * <p>
     * 自动处理 Span 的启动和关闭，并捕获异常。
     *
     * @param spanName  Span 名称
     * @param operation 要执行的操作
     * @param <T>       返回类型
     * @return 操作的返回值
     */
    <T> T withSpan(String spanName, Supplier<T> operation);

    /**
     * 在指定 Span 上下文中执行无返回值操作。
     *
     * @param spanName  Span 名称
     * @param operation 要执行的操作
     */
    void withSpan(String spanName, Runnable operation);

    // ========== Security-Specific Spans ==========

    /**
     * 创建认证相关的 Span。
     * <p>
     * 预设认证相关的标签。
     *
     * @param authType 认证类型（如 jwt, username_password）
     * @param username 用户名（会被脱敏）
     * @return 创建的 Span
     */
    Span startAuthenticationSpan(String authType, String username);

    /**
     * 创建 JWT 验证相关的 Span。
     * <p>
     * 预设 JWT 验证相关的标签。
     *
     * @param tokenId Token ID（会被脱敏）
     * @return 创建的 Span
     */
    Span startJwtValidationSpan(String tokenId);

    /**
     * 创建用户加载相关的 Span。
     *
     * @param username 用户名
     * @return 创建的 Span
     */
    Span startUserLoadSpan(String username);

    // ========== Context Access ==========

    /**
     * 获取当前活跃的 Span。
     *
     * @return 当前 Span，如果没有则返回 null
     */
    Span getCurrentSpan();

    /**
     * 获取当前追踪 ID。
     *
     * @return 当前追踪 ID，如果没有则返回 null
     */
    String getCurrentTraceId();

    /**
     * 获取当前 Span ID。
     *
     * @return 当前 Span ID，如果没有则返回 null
     */
    String getCurrentSpanId();

    // ========== Span Operations ==========

    /**
     * 向当前 Span 添加标签。
     *
     * @param key   标签键
     * @param value 标签值
     */
    void addTag(String key, String value);

    /**
     * 向当前 Span 添加事件。
     *
     * @param eventName 事件名称
     */
    void addEvent(String eventName);

    /**
     * 向当前 Span 添加带有属性的事件。
     *
     * @param eventName  事件名称
     * @param attributes 事件属性
     */
    void addEvent(String eventName, Map<String, String> attributes);

    /**
     * 记录当前 Span 的错误。
     *
     * @param throwable 错误对象
     */
    void recordError(Throwable throwable);

    // ========== Data Masking ==========

    /**
     * 脱敏用户名。
     * <p>
     * 保留前几个字符，其余用 * 替换。
     *
     * @param username 原始用户名
     * @return 脱敏后的用户名
     */
    String maskUsername(String username);

    /**
     * 脱敏 Token。
     * <p>
     * 保留前几个字符，其余用 * 替换。
     *
     * @param token 原始 Token
     * @return 脱敏后的 Token
     */
    String maskToken(String token);

    // ========== Status ==========

    /**
     * 检查追踪是否可用。
     *
     * @return 如果追踪可用返回 true
     */
    boolean isAvailable();
}
