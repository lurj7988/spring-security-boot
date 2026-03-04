package com.original.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.handler.InvalidSessionHandler;
import com.original.security.handler.SessionExpiredHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * Session 认证自动配置类。
 * <p>
 * 该配置类负责配置 Session 认证相关的 Bean，包括：
 * <ul>
 *     <li>{@link SessionRegistry}: Session 注册表，用于跟踪活跃的 Session</li>
 *     <li>{@link HttpSessionEventPublisher}: Session 事件发布器</li>
 *     <li>{@link SessionInformationExpiredStrategy}: Session 过期处理策略</li>
 *     <li>{@link InvalidSessionStrategy}: 无效 Session 处理策略</li>
 * </ul>
 * </p>
 *
 * <p>配置示例 (application.properties)：</p>
 * <pre>
 * security.session.timeout=1800
 * security.session.max-sessions=1
 * security.session.store-type=memory
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see SessionProperties
 * @see SessionExpiredHandler
 */
@Configuration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@EnableConfigurationProperties(SessionProperties.class)
@ConditionalOnProperty(prefix = "security.session", name = "enabled", havingValue = "true")
public class SessionAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SessionAutoConfiguration.class);

    private final SessionProperties sessionProperties;

    /**
     * 构造 Session 自动配置类。
     *
     * @param sessionProperties Session 配置属性
     */
    public SessionAutoConfiguration(SessionProperties sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    /**
     * 配置 Servlet 容器的 Session 超时时间。
     * <p>
     * 将 {@link SessionProperties#getTimeout()} 配置的超时时间（秒）
     * 应用到 Servlet 容器中，确保 Session 超时配置实际生效。
     * </p>
     *
     * @return ServletContextInitializer 实例
     */
    @Bean
    public ServletContextInitializer sessionTimeoutInitializer() {
        return servletContext -> {
            int timeoutMinutes = Math.max(1, sessionProperties.getTimeout() / 60);
            servletContext.setSessionTimeout(timeoutMinutes);
            log.info("Session auto-configuration: Session timeout set to {} minutes ({} seconds)",
                    timeoutMinutes, sessionProperties.getTimeout());
        };
    }

    @Bean
    @ConditionalOnMissingBean(SessionRegistry.class)
    public SessionRegistry sessionRegistry() {
        if (sessionProperties.isRedisStore()) {
            throw new IllegalStateException(
                    "Session store-type 'redis' requires 'spring-session-data-redis' dependency and Redis configuration. " +
                    "Please add the dependency or change store-type to 'memory'.");
        }
        log.info("Session auto-configuration: Registering in-memory SessionRegistry");
        return new SessionRegistryImpl();
    }

    /**
     * 创建 HttpSession 事件发布器。
     * <p>
     * 该发布器将 HttpSession 事件转换为 Spring 应用事件，
     * 使得应用可以监听 Session 创建、销毁等事件。
     * </p>
     *
     * @return HttpSessionEventPublisher 实例
     */
    @Bean
    @ConditionalOnMissingBean(HttpSessionEventPublisher.class)
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        log.info("Session auto-configuration: Registering HttpSessionEventPublisher");
        return new HttpSessionEventPublisher();
    }

    /**
     * 创建 Session 信息过期策略 Bean。
     * <p>
     * 当用户的 Session 过期或因并发登录被踢出时，该策略负责处理响应。
     * 返回 401 Unauthorized 状态码和 JSON 格式的错误信息。
     * </p>
     *
     * @param objectMapper JSON 序列化器
     * @return SessionInformationExpiredStrategy 实例
     */
    @Bean
    @ConditionalOnMissingBean(SessionInformationExpiredStrategy.class)
    public SessionInformationExpiredStrategy sessionInformationExpiredStrategy(ObjectMapper objectMapper) {
        log.info("Session auto-configuration: Registering SessionExpiredHandler");
        return new SessionExpiredHandler(objectMapper);
    }

    /**
     * 创建无效 Session 处理策略 Bean。
     * <p>
     * 当请求携带无效的 Session ID 时，该策略负责处理响应。
     * 返回 401 Unauthorized 状态码和 JSON 格式的错误信息。
     * </p>
     *
     * @param objectMapper JSON 序列化器
     * @return InvalidSessionStrategy 实例
     */
    @Bean
    @ConditionalOnMissingBean(InvalidSessionStrategy.class)
    public InvalidSessionStrategy invalidSessionStrategy(ObjectMapper objectMapper) {
        log.info("Session auto-configuration: Registering InvalidSessionHandler");
        return new InvalidSessionHandler(objectMapper);
    }
}
