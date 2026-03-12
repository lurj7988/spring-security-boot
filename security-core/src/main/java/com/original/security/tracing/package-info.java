/**
 * 分布式追踪支持包。
 * <p>
 * 提供安全组件的分布式追踪能力，基于 Micrometer Tracing API。
 * 支持 Brave (Zipkin) 和 OpenTelemetry 两种追踪实现。
 * <p>
 * 核心组件：
 * <ul>
 *     <li>{@link com.original.security.tracing.SecurityTracer} - 追踪器接口</li>
 *     <li>{@link com.original.security.tracing.DefaultSecurityTracer} - 默认实现</li>
 *     <li>{@link com.original.security.tracing.TracingConstants} - 追踪常量</li>
 * </ul>
 * <p>
 * 配置前缀：security.tracing
 * <p>
 * 使用示例：
 * <pre>
 * &#064;Autowired
 * private SecurityTracer securityTracer;
 *
 * public void authenticate(String username) {
 *     Span span = securityTracer.startAuthenticationSpan("jwt", username);
 *     try {
 *         // 认证逻辑
 *         securityTracer.addTag("result", "success");
 *     } finally {
 *         span.end();
 *     }
 * }
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see com.original.security.tracing.SecurityTracer
 * @see com.original.security.tracing.config.TracingAutoConfiguration
 */
package com.original.security.tracing;
