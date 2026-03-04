package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Session 认证配置属性。
 * <p>
 * 绑定以 "security.session" 为前缀的配置项，用于配置 Session 认证相关参数。
 * </p>
 *
 * <p>配置示例 (application.properties)：</p>
 * <pre>
 * # Session 超时时间（秒），默认 30 分钟
 * security.session.timeout=1800
 * # 单用户最大并发 Session 数，默认 1
 * security.session.max-sessions=1
 * # Session 存储方式：memory(内存) 或 redis，默认 memory
 * security.session.store-type=memory
 * # 是否启用 Session 固定攻击防护，默认 true
 * security.session.fixation-protection=true
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see SecurityProperties
 */
@ConfigurationProperties(prefix = "security.session")
public class SessionProperties {

    /**
     * 默认 Session 超时时间：30 分钟（1800 秒）
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 1800;

    /**
     * 默认单用户最大并发 Session 数
     */
    public static final int DEFAULT_MAX_SESSIONS = 1;

    /**
     * 默认存储类型
     */
    public static final String DEFAULT_STORE_TYPE = "memory";

    /**
     * Session 超时时间（秒）
     */
    private int timeout = DEFAULT_TIMEOUT_SECONDS;

    /**
     * 单用户最大并发 Session 数
     * 设置为 -1 表示不限制
     */
    private int maxSessions = DEFAULT_MAX_SESSIONS;

    /**
     * Session 存储方式
     * 支持：memory（内存）、redis
     * <p>注意：使用 redis 存储方式需要额外引入 'spring-session-data-redis' 依赖并进行 Redis 配置。</p>
     */
    private String storeType = DEFAULT_STORE_TYPE;

    /**
     * 是否启用 Session 固定攻击防护
     */
    private boolean fixationProtection = true;

    /**
     * 获取 Session 超时时间（秒）。
     *
     * @return Session 超时时间，默认 1800 秒（30 分钟）
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 设置 Session 超时时间（秒）。
     *
     * @param timeout 超时时间，必须大于 0
     */
    public void setTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Session timeout must be greater than 0");
        }
        this.timeout = timeout;
    }

    /**
     * 获取单用户最大并发 Session 数。
     *
     * @return 最大并发 Session 数，-1 表示不限制
     */
    public int getMaxSessions() {
        return maxSessions;
    }

    /**
     * 设置单用户最大并发 Session 数。
     *
     * @param maxSessions 最大并发数，-1 表示不限制
     */
    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    /**
     * 获取 Session 存储方式。
     *
     * @return 存储方式：memory 或 redis
     */
    public String getStoreType() {
        return storeType;
    }

    /**
     * 设置 Session 存储方式。
     *
     * @param storeType 存储方式，支持 memory 或 redis
     */
    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    /**
     * 检查是否启用 Session 固定攻击防护。
     *
     * @return true 表示启用防护，false 表示禁用
     */
    public boolean isFixationProtection() {
        return fixationProtection;
    }

    /**
     * 设置是否启用 Session 固定攻击防护。
     *
     * @param fixationProtection true 启用，false 禁用
     */
    public void setFixationProtection(boolean fixationProtection) {
        this.fixationProtection = fixationProtection;
    }

    /**
     * 检查是否使用内存存储。
     *
     * @return true 表示使用内存存储
     */
    public boolean isMemoryStore() {
        return DEFAULT_STORE_TYPE.equalsIgnoreCase(storeType);
    }

    /**
     * 检查是否使用 Redis 存储。
     *
     * @return true 表示使用 Redis 存储
     */
    public boolean isRedisStore() {
        return "redis".equalsIgnoreCase(storeType);
    }
}
