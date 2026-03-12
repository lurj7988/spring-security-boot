/**
 * 分布式追踪配置包。
 * <p>
 * 提供追踪功能的自动配置和属性配置。
 * <p>
 * 核心配置：
 * <ul>
 *     <li>{@link com.original.security.tracing.config.TracingAutoConfiguration} - 自动配置</li>
 *     <li>{@link com.original.security.tracing.config.SecurityTracingProperties} - 配置属性</li>
 * </ul>
 * <p>
 * 配置属性 (application.yml):
 * <pre>
 * security:
 *   tracing:
 *     enabled: true
 *     username-mask-length: 3
 *     token-mask-length: 8
 *     record-auth-failure-details: false
 *     sampling-rate: 1.0
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
package com.original.security.tracing.config;
