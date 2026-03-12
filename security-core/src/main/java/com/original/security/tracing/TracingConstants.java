package com.original.security.tracing;

/**
 * 分布式追踪常量定义。
 * <p>
 * 定义安全组件追踪相关的常量，包括 Span 名称、Tag 键名等。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public final class TracingConstants {

    private TracingConstants() {
        // 工具类，禁止实例化
    }

    // ========== Span Names ==========

    /**
     * 认证 Span 名称
     */
    public static final String SPAN_AUTHENTICATION = "authentication/login";

    /**
     * JWT 验证 Span 名称
     */
    public static final String SPAN_JWT_VALIDATION = "security/jwt/validation";

    /**
     * 用户加载 Span 名称
     */
    public static final String SPAN_USER_LOAD = "security/user/load";

    /**
     * 权限检查 Span 名称
     */
    public static final String SPAN_AUTHORIZATION = "security/authorization";

    /**
     * JWT 刷新 Span 名称
     */
    public static final String SPAN_JWT_REFRESH = "security/jwt/refresh";

    /**
     * JWT 创建 Span 名称
     */
    public static final String SPAN_JWT_CREATE = "security/jwt/create";

    // ========== Tag Keys ==========

    /**
     * 认证类型 Tag
     */
    public static final String TAG_AUTH_TYPE = "security.auth.type";

    /**
     * 用户名 Tag（脱敏）
     */
    public static final String TAG_USERNAME = "security.user.name";

    /**
     * 用户 ID Tag
     */
    public static final String TAG_USER_ID = "security.user.id";

    /**
     * 认证结果 Tag
     */
    public static final String TAG_AUTH_RESULT = "security.auth.result";

    /**
     * 错误类型 Tag
     */
    public static final String TAG_ERROR_TYPE = "security.error.type";

    /**
     * JWT Token ID Tag（脱敏）
     */
    public static final String TAG_JWT_TOKEN_ID = "security.jwt.token_id";

    /**
     * HTTP 方法 Tag
     */
    public static final String TAG_HTTP_METHOD = "security.http.method";

    /**
     * 请求路径 Tag
     */
    public static final String TAG_REQUEST_PATH = "security.request.path";

    /**
     * 持续时间（毫秒）Tag
     */
    public static final String TAG_DURATION_MS = "security.duration.ms";

    /**
     * 用户名 Tag（兼容别名）
     */
    public static final String TAG_USERNAME_MASKED = "security.user.name_masked";

    /**
     * JWT 旧 Token ID Tag（脱敏）
     */
    public static final String TAG_JWT_OLD_TOKEN_ID = "jwt.old_token_id";

    /**
     * JWT 新 Token ID Tag（脱敏）
     */
    public static final String TAG_JWT_NEW_TOKEN_ID = "jwt.new_token_id";

    /**
     * JWT 刷新 Token ID Tag（脱敏）
     */
    public static final String TAG_JWT_REFRESH_TOKEN_ID = "jwt.refresh_token_id";

    // ========== Tag Values ==========

    /**
     * 成功值
     */
    public static final String VALUE_SUCCESS = "success";

    /**
     * 失败值
     */
    public static final String VALUE_FAILURE = "failure";

    /**
     * 认证类型 - JWT
     */
    public static final String AUTH_TYPE_JWT = "jwt";

    /**
     * 认证类型 - 用户名密码
     */
    public static final String AUTH_TYPE_USERNAME_PASSWORD = "username_password";

    /**
     * 认证类型 - Session
     */
    public static final String AUTH_TYPE_SESSION = "session";

    // ========== Event Names ==========

    /**
     * 认证开始事件
     */
    public static final String EVENT_AUTH_START = "auth.start";

    /**
     * 认证完成事件
     */
    public static final String EVENT_AUTH_COMPLETE = "auth.complete";

    /**
     * Token 验证开始事件
     */
    public static final String EVENT_TOKEN_VALIDATE_START = "token.validate.start";

    /**
     * Token 验证完成事件
     */
    public static final String EVENT_TOKEN_VALIDATE_COMPLETE = "token.validate.complete";

    // ========== HTTP Headers (B3 Format) ==========

    /**
     * B3 Trace ID Header
     */
    public static final String HEADER_B3_TRACE_ID = "X-B3-TraceId";

    /**
     * B3 Span ID Header
     */
    public static final String HEADER_B3_SPAN_ID = "X-B3-SpanId";

    /**
     * B3 Parent Span ID Header
     */
    public static final String HEADER_B3_PARENT_SPAN_ID = "X-B3-ParentSpanId";

    /**
     * B3 Sampled Header
     */
    public static final String HEADER_B3_SAMPLED = "X-B3-Sampled";

    // ========== HTTP Headers (W3C Format) ==========

    /**
     * W3C Trace Context Header
     */
    public static final String HEADER_TRACEPARENT = "traceparent";

    /**
     * W3C Trace State Header
     */
    public static final String HEADER_TRACESTATE = "tracestate";

    // ========== Configuration Defaults ==========

    /**
     * 默认追踪启用状态
     */
    public static final boolean DEFAULT_TRACING_ENABLED = true;

    /**
     * 默认用户名脱敏长度
     */
    public static final int DEFAULT_USERNAME_MASK_LENGTH = 3;

    /**
     * 默认 Token 脱敏长度
     */
    public static final int DEFAULT_TOKEN_MASK_LENGTH = 8;

    /**
     * 默认采样率 (100%)
     */
    public static final float DEFAULT_SAMPLING_RATE = 1.0f;
}
