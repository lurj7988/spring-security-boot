/**
 * 健康检查组件包。
 * <p>
 * 提供安全组件的健康状态检查功能，包括：
 * <ul>
 *     <li>{@link com.original.security.health.SecurityHealthIndicator} - 主要健康检查入口</li>
 *     <li>{@link com.original.security.health.DatabaseHealthIndicator} - 数据库健康检查</li>
 *     <li>{@link com.original.security.health.JwtValidatorHealthIndicator} - JWT 验证器检查</li>
 *     <li>{@link com.original.security.health.CacheHealthIndicator} - 缓存健康检查</li>
 * </ul>
 *
 * @author bmad
 * @since 0.1.0
 */
package com.original.security.health;
