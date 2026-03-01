package com.original.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.original.security.filter.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import com.original.security.handler.FrameAccessDeniedHandler;
import com.original.security.handler.FrameAuthenticationEntryPoint;
import com.original.security.plugin.SecurityFilterPlugin;

import javax.servlet.Filter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security Boot 核心自动配置类。
 * <p>
 * 该类由 {@link com.original.security.annotation.EnableSecurityBoot} 注解导入加载。
 * 负责提供安全框架所需的基础核心 Bean，包括：
 * <ul>
 *     <li>{@link PasswordEncoder}: 默认使用符合安全规范的 BCryptPasswordEncoder</li>
 *     <li>{@link AuthenticationManager}: 暴露核心认证管理器</li>
 *     <li>{@link SecurityFilterChain}: 配置默认的基础拦截链，提供无状态(Stateless)等基础支撑</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@Import({NetworkSecurityAutoConfiguration.class, MethodSecurityConfiguration.class})
public class SecurityAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityAutoConfiguration.class);

    /**
     * 最大 BCrypt 强度（根据项目规范和环境性能可能会有所不同，此处提供默认强度 10）
     */
    private static final int DEFAULT_PASSWORD_STRENGTH = 10;

    /**
     * 实例化密码编码器。
     * 默认采用 BCrypt 加密算法以符合安全需求。
     * <p>
     * 使用 {@code @ConditionalOnMissingBean} 以便当用户在上下文中提供自定义配置时自动退让。
     *
     * @return 实例化的 PasswordEncoder
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        log.info("Security auto-configuration: Registering default BCryptPasswordEncoder with strength {}", DEFAULT_PASSWORD_STRENGTH);
        return new BCryptPasswordEncoder(DEFAULT_PASSWORD_STRENGTH);
    }

    /**
     * 注册认证管理器 AuthenticationManager。
     * 通过 Spring Security 的 AuthenticationConfiguration 直接获取。
     *
     * @param authenticationConfiguration Security 注入的认证配置
     * @return 实例化的 AuthenticationManager
     * @throws Exception 如果获取失败抛出异常
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        log.info("Security auto-configuration: Registering AuthenticationManager");
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 注册授权审计监听器。
     */
    @Bean
    @ConditionalOnMissingBean(com.original.security.event.AuthorizationAuditListener.class)
    public com.original.security.event.AuthorizationAuditListener authorizationAuditListener() {
        log.info("Security auto-configuration: Registering AuthorizationAuditListener");
        return new com.original.security.event.AuthorizationAuditListener();
    }

    /**
     * 构建并在容器中装配默认的 SecurityFilterChain。
     * <ul>
     *     <li>禁用默认的表单登录和 Basic 认证（后续由插件式架构接管）</li>
     *     <li>将 Session 会话管理策略配置为 STATELESS，以支持 API 无状态特性或 JWT</li>
     *     <li>为后续的网络安全配置（如 CORS、CSRF）预留口子</li>
     * </ul>
     *
     * @param http HttpSecurity 构建器
     * @param jwtFilterProvider JWT认证过滤器提供者，可能为 null
     * @param corsProperties CORS 属性配置
     * @param csrfTokenRepositoryProvider CSRF Token 存储库提供者，可能为 null
     * @param accessDeniedHandlerProvider 访问拒绝处理器提供者，可能为 null
     * @param headersPropertiesProvider 安全响应头属性提供者，用于配置 X-Frame-Options、X-Content-Type-Options、X-XSS-Protection、HSTS
     * @param cspPropertiesProvider CSP 属性提供者，用于配置 Content-Security-Policy 头
     * @return 构建完毕的 SecurityFilterChain
     * @throws Exception 如果配置过程中出错
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, 
            ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider, 
            CorsProperties corsProperties,
            ObjectProvider<CsrfTokenRepository> csrfTokenRepositoryProvider,
            ObjectProvider<FrameAccessDeniedHandler> accessDeniedHandlerProvider,
            ObjectProvider<SecurityHeadersProperties> headersPropertiesProvider,
            ObjectProvider<CspProperties> cspPropertiesProvider,
            ObjectProvider<FrameAuthenticationEntryPoint> authenticationEntryPointProvider,
            ObjectProvider<SecurityFilterPlugin> filterPluginsProvider
    ) throws Exception {
        log.info("Security auto-configuration: Initializing basic SecurityFilterChain");
        
        if (corsProperties.isEnabled()) {
            http.cors(); // 自动按照规定名称获取 corsConfigurationSource Bean
        } else {
            http.cors().disable();
        }

        CsrfTokenRepository csrfTokenRepository = csrfTokenRepositoryProvider.getIfAvailable();
        if (csrfTokenRepository != null) {
            http.csrf().csrfTokenRepository(csrfTokenRepository);
        } else {
            http.csrf().disable();
        }

        FrameAccessDeniedHandler accessDeniedHandler = accessDeniedHandlerProvider.getIfAvailable();
        FrameAuthenticationEntryPoint authenticationEntryPoint = authenticationEntryPointProvider.getIfAvailable();
        
        if (accessDeniedHandler != null || authenticationEntryPoint != null) {
            http.exceptionHandling(exceptionHandling -> {
                if (accessDeniedHandler != null) {
                    exceptionHandling.accessDeniedHandler(accessDeniedHandler);
                }
                if (authenticationEntryPoint != null) {
                    exceptionHandling.authenticationEntryPoint(authenticationEntryPoint);
                }
            });
        }

        SecurityHeadersProperties headersProperties = headersPropertiesProvider.getIfAvailable();
        if (headersProperties != null && headersProperties.isEnabled()) {
            http.headers(headers -> {
                if ("DENY".equalsIgnoreCase(headersProperties.getFrameOptions())) {
                    headers.frameOptions().deny();
                } else if ("SAMEORIGIN".equalsIgnoreCase(headersProperties.getFrameOptions())) {
                    headers.frameOptions().sameOrigin();
                } else {
                    headers.frameOptions().disable();
                }
                
                if (headersProperties.isContentTypeOptions()) {
                    headers.contentTypeOptions();
                }
                
                if (headersProperties.isXssProtection()) {
                    headers.xssProtection().block(true);
                } else {
                    headers.xssProtection().disable();
                }
                
                if (headersProperties.getHstsMaxAge() > 0) {
                    headers.httpStrictTransportSecurity()
                            .maxAgeInSeconds(headersProperties.getHstsMaxAge())
                            .includeSubDomains(headersProperties.isHstsIncludeSubDomains())
                            .preload(headersProperties.isHstsPreload());
                } else {
                    headers.httpStrictTransportSecurity().disable();
                }

                CspProperties cspProperties = cspPropertiesProvider.getIfAvailable();
                if (cspProperties != null && cspProperties.isEnabled()) {
                    headers.contentSecurityPolicy(cspProperties.getPolicy());
                }
            });
        } else {
            http.headers().disable();
        }

        http
            // 禁用基础认证和表单登录
            .httpBasic().disable()
            .formLogin().disable()

            // 基础退出禁用，之后会使用我们的 Logout机制或者直接不管理
            .logout().disable()
            // 设置默认的会话策略为无状态
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 所有请求都需要认证（默认极简策略），由应用自定义更详细的权限放行
            .authorizeHttpRequests()
                .antMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
                .anyRequest().authenticated();
            
        List<SecurityFilterPlugin> filterPlugins = filterPluginsProvider.orderedStream()
                .filter(SecurityFilterPlugin::isEnabled)
                .collect(Collectors.toList());

        if (!filterPlugins.isEmpty()) {
            log.info("Security auto-configuration: Registering {} custom filter plugin(s)", filterPlugins.size());

            for (SecurityFilterPlugin plugin : filterPlugins) {
                log.debug("Security auto-configuration: Registering filter plugin [{}] at position {} relative to {}",
                        plugin.getName(), plugin.getPosition(), plugin.getTargetFilterClass().getSimpleName());

                // 开发模式下验证 getFilter() 返回相同实例
                if (log.isDebugEnabled()) {
                    Filter filter1 = plugin.getFilter();
                    Filter filter2 = plugin.getFilter();
                    if (filter1 != filter2) {
                        log.warn("Security auto-configuration: Plugin [{}] getFilter() returns different instances! " +
                                "This may cause duplicate filters in the chain. Please cache the filter instance.",
                                plugin.getName());
                    }
                }

                switch (plugin.getPosition()) {
                    case BEFORE:
                        http.addFilterBefore(plugin.getFilter(), plugin.getTargetFilterClass());
                        break;
                    case AFTER:
                        http.addFilterAfter(plugin.getFilter(), plugin.getTargetFilterClass());
                        break;
                    case AT:
                        http.addFilterAt(plugin.getFilter(), plugin.getTargetFilterClass());
                        break;
                }
            }
        }
        
        JwtAuthenticationFilter jwtFilter = jwtFilterProvider.getIfAvailable();
        if (jwtFilter != null) {
            log.info("Security auto-configuration: Registering JwtAuthenticationFilter");
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }
            
        SecurityFilterChain filterChain = http.build();
        
        if (log.isDebugEnabled()) {
            log.debug("Security auto-configuration: Built SecurityFilterChain with the following filters:");
            int order = 1;
            for (Filter filter : filterChain.getFilters()) {
                log.debug("  {} - {}", order++, filter.getClass().getSimpleName());
            }
        }
            
        return filterChain;
    }
}
