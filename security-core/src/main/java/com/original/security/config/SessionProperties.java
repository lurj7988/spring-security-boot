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
 * # 是否启用 Session 功能，默认 true（启用时系统将使用 IF_REQUIRED 策略）
 * security.session.enabled=true
 * # Session 超时时间（秒），默认 30 分钟
 * security.session.timeout=1800
 * # 单用户最大并发 Session 数，默认 1
 * security.session.max-sessions=1
 * # Session 存储方式：memory(内存) 或 redis，默认 memory
 * security.session.store-type=memory
 * # Session Cookie 名称，默认 JSESSIONID
 * security.session.cookie-name=JSESSIONID
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
     * 默认 Cookie 名称
     */
    public static final String DEFAULT_COOKIE_NAME = "JSESSIONID";

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
     * Session Cookie 名称
     */
    private String cookieName = DEFAULT_COOKIE_NAME;

    /**
     * 是否启用 Session 固定攻击防护
     */
    private boolean fixationProtection = true;

    /**
     * 是否启用 Session 功能。
     * <p>
     * 设置为 false 时，SessionAutoConfiguration 不会被加载，
     * 系统将使用默认的 STATELESS 会话策略。
     * </p>
     */
    private boolean enabled = true;

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
     * @param maxSessions 最大并发数，-1 表示不限制，必须大于 0 或等于 -1
     */
    public void setMaxSessions(int maxSessions) {
        if (maxSessions != -1 && maxSessions <= 0) {
            throw new IllegalArgumentException("Session maxSessions must be greater than 0 or equal to -1 (unlimited)");
        }
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
        if (storeType != null && !"memory".equalsIgnoreCase(storeType) && !"redis".equalsIgnoreCase(storeType)) {
            throw new IllegalArgumentException("Session storeType must be 'memory' or 'redis'");
        }
        this.storeType = storeType;
    }

    /**
     * 获取 Session Cookie 名称。
     *
     * @return Cookie 名称，默认 JSESSIONID
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * 设置 Session Cookie 名称。
     * <p>
     * 注意：当前版本中此配置未实际应用到 Servlet 容器。
     * 此属性为未来功能预留接口。
     * </p>
     *
     * @param cookieName Cookie 名称
     */
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
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
     * 检查 Session 功能是否已启用。
     *
     * @return true 表示已启用，false 表示已禁用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 Session 功能。
     *
     * @param enabled true 启用，false 禁用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
