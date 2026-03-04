package com.original.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.RememberMeServices;

import javax.sql.DataSource;

/**
 * Remember Me 自动配置类。
 * <p>
 * 提供 PersistentTokenRepository 实现，默认使用 JdbcTokenRepositoryImpl 进行 Token 的持久化读写。
 * 依赖于 DataSource 存在。
 * 并且暴露 RememberMeServices 供自定义控制器调用。
 * </p>
 */
@Configuration
@EnableConfigurationProperties(RememberMeProperties.class)
@ConditionalOnProperty(prefix = "security.remember-me", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({JdbcTokenRepositoryImpl.class, org.springframework.jdbc.core.support.JdbcDaoSupport.class})
public class RememberMeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RememberMeAutoConfiguration.class);

    /**
     * 注册持久化 Token 仓库
     *
     * @param dataSource 数据源
     * @return PersistentTokenRepository 实现
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(PersistentTokenRepository.class)
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        log.info("Security auto-configuration: Registering JdbcTokenRepositoryImpl for Remember Me");
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    /**
     * 注册 RememberMeServices 供 AuthenticationController 使用。
     *
     * <p><b>安全说明：</b></p>
     * <ul>
     *   <li>Cookie 的 Secure 属性：当前 Spring Security 5.7.11 的
     *       {@code PersistentTokenBasedRememberMeServices} 不直接支持设置 Cookie 的 Secure 属性。
     *       建议在 Servlet 容器配置或反向代理中强制使用 HTTPS，
     *       或使用自定义 {@code CookieSerializer} 来设置该属性。</li>
     *   <li>Cookie 的 SameSite 属性：Spring Security 5.7.11 不直接支持，
     *       建议在 Servlet 容器或反向代理配置中设置，
     *       或使用自定义 {@code CookieSerializer}。</li>
     * </ul>
     *
     * @param properties 配置属性
     * @param userDetailsService 用户服务
     * @param persistentTokenRepository Token 仓库
     * @return RememberMeServices
     */
    @Bean
    @ConditionalOnMissingBean(RememberMeServices.class)
    public RememberMeServices rememberMeServices(
            RememberMeProperties properties,
            org.springframework.beans.factory.ObjectProvider<UserDetailsService> userDetailsService,
            org.springframework.beans.factory.ObjectProvider<PersistentTokenRepository> persistentTokenRepository) {

        UserDetailsService uds = userDetailsService.getIfAvailable();
        PersistentTokenRepository ptr = persistentTokenRepository.getIfAvailable();

        if (uds == null || ptr == null) {
            log.warn("RememberMeServices not created because UserDetailsService or PersistentTokenRepository is missing");
            return null;
        }

        String key = properties.getKey();
        if (key == null || key.isEmpty()) {
            // 强制要求用户配置 Remember Me 密钥，避免每次重启生成新密钥导致 Cookie 失效
            log.error("RememberMeServices key is not configured. Please set 'security.remember-me.key' property with a secure key for production use.");
            throw new IllegalStateException("RememberMeServices key must be configured via 'security.remember-me.key' property. " +
                    "For development, you may set it in application.properties or environment variables.");
        }
        PersistentTokenBasedRememberMeServices rememberMeServices = new PersistentTokenBasedRememberMeServices(
                key, uds, ptr);
        rememberMeServices.setParameter("rememberMe");
        rememberMeServices.setCookieName(properties.getCookieName());
        rememberMeServices.setTokenValiditySeconds(properties.getTokenValiditySeconds());
        return rememberMeServices;
    }
}
