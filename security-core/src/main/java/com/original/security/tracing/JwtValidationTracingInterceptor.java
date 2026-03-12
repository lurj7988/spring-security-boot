package com.original.security.tracing;

import io.micrometer.tracing.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.original.security.tracing.TracingConstants.*;

/**
 * JWT 验证追踪拦截器。
 * <p>
 * 为 JWT Token 验证提供追踪能力，记录验证过程和结果。
 * 支持计时、错误记录和上下文传播。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class JwtValidationTracingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationTracingInterceptor.class);

    private final SecurityTracer securityTracer;

    /**
     * 构造函数。
     *
     * @param securityTracer 安全追踪器
     */
    public JwtValidationTracingInterceptor(SecurityTracer securityTracer) {
        this.securityTracer = securityTracer;
    }

    /**
     * 在追踪上下文中执行 JWT 验证。
     * <p>
     * 自动创建和结束 Span，记录验证耗时和结果。
     *
     * @param tokenId           Token ID（会被脱敏）
     * @param validationOperation 验证操作
     * @param <T>               返回类型
     * @return 验证操作的返回值
     */
    public <T> T withJwtValidation(String tokenId, Supplier<T> validationOperation) {
        if (!securityTracer.isAvailable()) {
            return validationOperation.get();
        }

        Span span = securityTracer.startSpan(SPAN_JWT_VALIDATION, buildJwtTags(tokenId));
        long startTime = System.currentTimeMillis();

        try {
            securityTracer.addEvent(EVENT_TOKEN_VALIDATE_START);

            T result = validationOperation.get();

            long duration = System.currentTimeMillis() - startTime;
            securityTracer.addTag(TAG_DURATION_MS, String.valueOf(duration));
            securityTracer.addTag(TAG_AUTH_RESULT, VALUE_SUCCESS);
            securityTracer.addEvent(EVENT_TOKEN_VALIDATE_COMPLETE);

            log.debug("JWT validation completed successfully in {}ms, tokenId: {}",
                    duration, securityTracer.maskToken(tokenId));

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            securityTracer.addTag(TAG_DURATION_MS, String.valueOf(duration));
            securityTracer.addTag(TAG_AUTH_RESULT, VALUE_FAILURE);
            securityTracer.addTag(TAG_ERROR_TYPE, e.getClass().getSimpleName());
            securityTracer.recordError(e);

            log.warn("JWT validation failed in {}ms, tokenId: {}, error: {}",
                    duration, securityTracer.maskToken(tokenId), e.getMessage());

            throw e;
        } finally {
            if (span != null) {
                span.end();
            }
        }
    }

    /**
     * 在追踪上下文中执行无返回值的 JWT 验证。
     *
     * @param tokenId           Token ID（会被脱敏）
     * @param validationOperation 验证操作
     */
    public void withJwtValidation(String tokenId, Runnable validationOperation) {
        withJwtValidation(tokenId, () -> {
            validationOperation.run();
            return null;
        });
    }

    /**
     * 记录 JWT 刷新操作。
     *
     * @param oldTokenId 旧 Token ID
     * @param newTokenId 新 Token ID
     * @param refreshTokenId 刷新 Token ID
     */
    public void recordTokenRefresh(String oldTokenId, String newTokenId, String refreshTokenId) {
        if (!securityTracer.isAvailable()) {
            return;
        }

        securityTracer.withSpan(SPAN_JWT_REFRESH, () -> {
            if (oldTokenId != null) {
                securityTracer.addTag(TAG_JWT_OLD_TOKEN_ID, securityTracer.maskToken(oldTokenId));
            }
            if (newTokenId != null) {
                securityTracer.addTag(TAG_JWT_NEW_TOKEN_ID, securityTracer.maskToken(newTokenId));
            }
            if (refreshTokenId != null) {
                securityTracer.addTag(TAG_JWT_REFRESH_TOKEN_ID, securityTracer.maskToken(refreshTokenId));
            }

            log.debug("JWT token refresh recorded");
        });
    }

    /**
     * 记录 JWT Token 创建。
     *
     * @param username 用户名
     * @param tokenId  Token ID
     */
    public void recordTokenCreation(String username, String tokenId) {
        if (!securityTracer.isAvailable()) {
            return;
        }

        securityTracer.withSpan(SPAN_JWT_CREATE, () -> {
            if (username != null) {
                securityTracer.addTag(TAG_USERNAME, securityTracer.maskUsername(username));
            }
            if (tokenId != null) {
                securityTracer.addTag(TAG_JWT_TOKEN_ID, securityTracer.maskToken(tokenId));
            }
            securityTracer.addTag(TAG_AUTH_RESULT, VALUE_SUCCESS);

            log.debug("JWT token creation recorded for user: {}", securityTracer.maskUsername(username));
        });
    }

    /**
     * 构建 JWT 验证标签。
     *
     * @param tokenId Token ID
     * @return 标签 Map
     */
    private Map<String, String> buildJwtTags(String tokenId) {
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_AUTH_TYPE, AUTH_TYPE_JWT);
        if (tokenId != null) {
            tags.put(TAG_JWT_TOKEN_ID, securityTracer.maskToken(tokenId));
        }
        return tags;
    }
}
