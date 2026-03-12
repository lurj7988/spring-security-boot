package com.original.security.tracing.feign;

import com.original.security.tracing.SecurityTracer;
import com.original.security.tracing.TracingConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.original.security.tracing.TracingConstants.*;

/**
 * Feign 追踪拦截器。
 * <p>
 * 自动将追踪上下文传播到 Feign 调用，确保分布式追踪链路完整。
 * 支持 B3 和 W3C 两种追踪格式。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class TracingFeignInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TracingFeignInterceptor.class);

    private final SecurityTracer securityTracer;

    /**
     * 构造函数。
     *
     * @param securityTracer 安全追踪器
     */
    public TracingFeignInterceptor(SecurityTracer securityTracer) {
        this.securityTracer = securityTracer;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!securityTracer.isAvailable()) {
            log.debug("Tracing not available, skipping Feign context propagation");
            return;
        }

        String traceId = securityTracer.getCurrentTraceId();
        String spanId = securityTracer.getCurrentSpanId();

        if (traceId == null && spanId == null) {
            log.debug("No active trace context (both traceId and spanId are null), skipping Feign context propagation");
            return;
        }

        if (traceId == null) {
            log.warn("Trace ID is null but Span ID exists, skipping incomplete context propagation");
            return;
        }

        if (spanId == null) {
            log.warn("Span ID is null but Trace ID exists, skipping incomplete context propagation");
            return;
        }

        // 传播 B3 格式 Headers
        propagateB3Headers(template, traceId, spanId);

        // 传播 W3C 格式 Headers
        propagateW3CHeaders(template, traceId, spanId);

        // 添加安全相关标签
        addSecurityTags(template);

        log.debug("Propagated trace context to Feign call: traceId={}, spanId={}",
                traceId, spanId);
    }

    /**
     * 传播 B3 格式追踪 Headers。
     *
     * @param template Feign 请求模板
     * @param traceId  追踪 ID
     * @param spanId   Span ID
     */
    private void propagateB3Headers(RequestTemplate template, String traceId, String spanId) {
        // 添加 B3 Headers
        template.header(HEADER_B3_TRACE_ID, traceId);
        template.header(HEADER_B3_SPAN_ID, spanId);
        template.header(HEADER_B3_SAMPLED, "1");

        log.trace("Added B3 headers: X-B3-TraceId={}, X-B3-SpanId={}", traceId, spanId);
    }

    /**
     * 传播 W3C 格式追踪 Headers。
     * <p>
     * 构建符合 W3C Trace Context 规范的 traceparent header。
     * 格式: version-traceid-parentid-flags (版本固定为 00，flags 固定为 01 表示采样)。
     * 注意: trace-id 应为 32 位 hex，parent-id 应为 16 位 hex。
     *
     * @param template Feign 请求模板
     * @param traceId  追踪 ID
     * @param spanId   Span ID
     */
    private void propagateW3CHeaders(RequestTemplate template, String traceId, String spanId) {
        // 构建 W3C traceparent: 00-{32位traceId}-{16位spanId}-01
        // 对 traceId 和 spanId 做长度补齐以符合规范
        String paddedTraceId = padHexId(traceId, 32);
        String paddedSpanId = padHexId(spanId, 16);
        String traceparent = String.format("00-%s-%s-01", paddedTraceId, paddedSpanId);
        template.header(HEADER_TRACEPARENT, traceparent);
        log.trace("Added W3C traceparent: {}", traceparent);
    }

    /**
     * 将 hex ID 补齐到指定长度。
     * <p>
     * 使用高效的字符串格式化方式，避免循环拼接。
     *
     * @param id     原始 ID
     * @param length 目标长度
     * @return 补齐后的 ID
     */
    private String padHexId(String id, int length) {
        if (id == null) {
            return String.format("%" + length + "s", "").replace(' ', '0');
        }
        if (id.length() >= length) {
            return id.substring(0, length);
        }
        return String.format("%" + length + "s", id).replace(' ', '0');
    }

    /**
     * 添加安全相关标签到请求。
     *
     * @param template Feign 请求模板
     */
    private void addSecurityTags(RequestTemplate template) {
        // 记录 Feign 调用信息
        securityTracer.addTag("feign.method", template.method());
        securityTracer.addTag("feign.url", maskSensitiveUrl(template.url()));
    }

    /**
     * 脱敏 URL 中的敏感信息。
     *
     * @param url 原始 URL
     * @return 脱敏后的 URL
     */
    private String maskSensitiveUrl(String url) {
        if (url == null) {
            return "[EMPTY]";
        }

        // 脱敏 URL 中的 token 参数
        if (url.contains("token=")) {
            return url.replaceAll("token=[^&]+", "token=****");
        }

        // 脱敏 URL 中的密钥参数
        if (url.contains("secret=")) {
            return url.replaceAll("secret=[^&]+", "secret=****");
        }

        return url;
    }

    /**
     * 创建 Feign 追踪拦截器的工厂方法。
     *
     * @param securityTracer 安全追踪器
     * @return TracingFeignInterceptor 实例
     */
    public static TracingFeignInterceptor create(SecurityTracer securityTracer) {
        return new TracingFeignInterceptor(securityTracer);
    }
}
