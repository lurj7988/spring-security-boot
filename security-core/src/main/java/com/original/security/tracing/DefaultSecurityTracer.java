package com.original.security.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.original.security.tracing.TracingConstants.*;

/**
 * SecurityTracer 的默认实现。
 * <p>
 * 基于 Micrometer Tracing API 实现，支持 Brave 和 OpenTelemetry 两种桥接方式。
 * 提供安全组件专用的追踪功能，包括用户名和 Token 脱敏。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class DefaultSecurityTracer implements SecurityTracer {

    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityTracer.class);

    private final Tracer tracer;
    private final int usernameMaskLength;
    private final int tokenMaskLength;

    /**
     * 构造函数。
     *
     * @param tracer             Micrometer Tracer 实例
     * @param usernameMaskLength 用户名脱敏长度
     * @param tokenMaskLength    Token 脱敏长度
     */
    public DefaultSecurityTracer(Tracer tracer, int usernameMaskLength, int tokenMaskLength) {
        this.tracer = tracer;
        this.usernameMaskLength = usernameMaskLength > 0 ? usernameMaskLength : DEFAULT_USERNAME_MASK_LENGTH;
        this.tokenMaskLength = tokenMaskLength > 0 ? tokenMaskLength : DEFAULT_TOKEN_MASK_LENGTH;

        if (tracer != null) {
            log.info("SecurityTracer initialized with usernameMaskLength={}, tokenMaskLength={}",
                    this.usernameMaskLength, this.tokenMaskLength);
        } else {
            log.info("SecurityTracer initialized in no-op mode (tracing not available)");
        }
    }

    /**
     * 使用默认脱敏长度的构造函数。
     *
     * @param tracer Micrometer Tracer 实例
     */
    public DefaultSecurityTracer(Tracer tracer) {
        this(tracer, DEFAULT_USERNAME_MASK_LENGTH, DEFAULT_TOKEN_MASK_LENGTH);
    }

    // ========== Span Creation ==========

    @Override
    public Span startSpan(String spanName) {
        return startSpan(spanName, null);
    }

    @Override
    public Span startSpan(String spanName, Map<String, String> tags) {
        if (!isAvailable()) {
            return null;
        }

        Span span = tracer.nextSpan().name(spanName);

        if (tags != null && !tags.isEmpty()) {
            tags.forEach(span::tag);
        }

        span.start();
        log.debug("Started span: {}", spanName);
        return span;
    }

    // ========== Span Context Operations ==========

    @Override
    public <T> T withSpan(String spanName, Supplier<T> operation) {
        if (!isAvailable()) {
            return operation.get();
        }

        Span span = startSpan(spanName);
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            T result = operation.get();
            return result;
        } catch (Exception e) {
            recordError(span, e);
            throw e;
        } finally {
            endSpan(span);
        }
    }

    @Override
    public void withSpan(String spanName, Runnable operation) {
        if (!isAvailable()) {
            operation.run();
            return;
        }

        Span span = startSpan(spanName);
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            operation.run();
        } catch (Exception e) {
            recordError(span, e);
            throw e;
        } finally {
            endSpan(span);
        }
    }

    // ========== Security-Specific Spans ==========

    @Override
    public Span startAuthenticationSpan(String authType, String username) {
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_AUTH_TYPE, authType);
        if (username != null) {
            tags.put(TAG_USERNAME, maskUsername(username));
        }
        return startSpan(SPAN_AUTHENTICATION, tags);
    }

    @Override
    public Span startJwtValidationSpan(String tokenId) {
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_AUTH_TYPE, AUTH_TYPE_JWT);
        if (tokenId != null) {
            tags.put(TAG_JWT_TOKEN_ID, maskToken(tokenId));
        }
        return startSpan(SPAN_JWT_VALIDATION, tags);
    }

    @Override
    public Span startUserLoadSpan(String username) {
        Map<String, String> tags = new HashMap<>();
        if (username != null) {
            tags.put(TAG_USERNAME, maskUsername(username));
        }
        return startSpan(SPAN_USER_LOAD, tags);
    }

    // ========== Context Access ==========

    @Override
    public Span getCurrentSpan() {
        if (!isAvailable()) {
            return null;
        }
        return tracer.currentSpan();
    }

    @Override
    public String getCurrentTraceId() {
        if (!isAvailable()) {
            return null;
        }
        Span span = tracer.currentSpan();
        return span != null ? span.context().traceId() : null;
    }

    @Override
    public String getCurrentSpanId() {
        if (!isAvailable()) {
            return null;
        }
        Span span = tracer.currentSpan();
        return span != null ? span.context().spanId() : null;
    }

    // ========== Span Operations ==========

    @Override
    public void addTag(String key, String value) {
        if (!isAvailable()) {
            return;
        }
        Span span = tracer.currentSpan();
        if (span != null) {
            span.tag(key, value);
        }
    }

    @Override
    public void addEvent(String eventName) {
        addEvent(eventName, null);
    }

    @Override
    public void addEvent(String eventName, Map<String, String> attributes) {
        if (!isAvailable()) {
            return;
        }
        Span span = tracer.currentSpan();
        if (span != null) {
            span.event(eventName);
            if (attributes != null) {
                attributes.forEach(span::tag);
            }
        }
    }

    @Override
    public void recordError(Throwable throwable) {
        if (!isAvailable()) {
            return;
        }
        Span span = tracer.currentSpan();
        recordError(span, throwable);
    }

    /**
     * 记录指定 Span 的错误。
     *
     * @param span      目标 Span
     * @param throwable 错误对象
     */
    private void recordError(Span span, Throwable throwable) {
        if (span == null || throwable == null) {
            return;
        }
        span.error(throwable);
        span.tag(TAG_ERROR_TYPE, throwable.getClass().getSimpleName());
        log.debug("Recorded error in span: {}", throwable.getMessage());
    }

    // ========== Data Masking ==========

    @Override
    public String maskUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "[EMPTY]";
        }
        if (username.length() <= usernameMaskLength) {
            return repeat("*", username.length());
        }
        String prefix = username.substring(0, usernameMaskLength);
        String masked = repeat("*", username.length() - usernameMaskLength);
        return prefix + masked;
    }

    @Override
    public String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "[EMPTY]";
        }
        if (token.length() <= tokenMaskLength) {
            return repeat("*", token.length());
        }
        String prefix = token.substring(0, tokenMaskLength);
        String masked = repeat("*", token.length() - tokenMaskLength);
        return prefix + masked;
    }

    /**
     * Java 8 兼容的字符串重复方法。
     *
     * @param str   要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // ========== Status ==========

    @Override
    public boolean isAvailable() {
        return tracer != null;
    }

    // ========== Private Helpers ==========

    /**
     * 结束 Span。
     *
     * @param span 要结束的 Span
     */
    private void endSpan(Span span) {
        if (span != null) {
            span.end();
            log.debug("Ended span");
        }
    }
}
