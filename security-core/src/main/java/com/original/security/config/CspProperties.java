package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内容安全策略 (Content Security Policy, CSP) 相关的配置属性。
 * <p>
 * CSP 是一种额外的安全层，用于检测和缓解某些类型的攻击，包括跨站脚本 (XSS) 和数据注入攻击。
 * <p>
 * 配置示例：
 * <pre>
 * # 启用 CSP（默认禁用）
 * security.network.csp.enabled=true
 *
 * # 设置 CSP 策略（默认值）
 * # 注意：在 YAML 中，单引号内的单引号需要转义为 '' 或使用双引号包裹整个值
 * # application.properties 格式：
 * security.network.csp.policy=default-src 'self'
 *
 * # application.yml 格式：
 * security:
 *   network:
 *     csp:
 *       enabled: true
 *       policy: "default-src 'self'"
 *
 * # 更严格的策略示例
 * security.network.csp.policy=default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'
 * </pre>
 *
 * @author Naulu
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.network.csp")
public class CspProperties {

    /**
     * 是否启用内容安全策略 (CSP)
     */
    private boolean enabled = false;

    /**
     * CSP 策略内容
     */
    private String policy = "default-src 'self'";

    /**
     * 获取是否启用内容安全策略 (CSP)。
     *
     * @return true 如果启用，false 如果禁用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用内容安全策略 (CSP)。
     *
     * @param enabled true 启用，false 禁用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取 CSP 策略内容。
     *
     * @return CSP 策略字符串
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * 设置 CSP 策略内容。
     * <p>
     * 注意：此方法仅验证 policy 不为空或 null，不验证 CSP 策略语法。
     * CSP 策略的语法验证由浏览器在运行时执行。
     * </p>
     * <p>
     * 常见的 CSP 策略示例：
     * <ul>
     *   <li>{@code default-src 'self'} - 仅允许从同源加载资源</li>
     *   <li>{@code default-src 'self'; script-src 'self' 'unsafe-inline'} - 允许内联脚本</li>
     *   <li>{@code default-src *} - 允许从任何来源加载资源（不推荐用于生产）</li>
     * </ul>
     * </p>
     *
     * @param policy CSP 策略字符串，不能为 null 或空
     * @throws IllegalArgumentException 如果 policy 为 null 或空
     */
    public void setPolicy(String policy) {
        if (policy == null || policy.trim().isEmpty()) {
            throw new IllegalArgumentException("CSP policy cannot be null or empty");
        }
        this.policy = policy.trim();
    }
}
