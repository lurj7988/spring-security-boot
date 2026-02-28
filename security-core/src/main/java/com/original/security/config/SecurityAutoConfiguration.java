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
@Import(NetworkSecurityAutoConfiguration.class)
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
     * 构建并在容器中装配默认的 SecurityFilterChain。
     * <ul>
     *     <li>禁用默认的表单登录和 Basic 认证（后续由插件式架构接管）</li>
     *     <li>将 Session 会话管理策略配置为 STATELESS，以支持 API 无状态特性或 JWT</li>
     *     <li>为后续的网络安全配置（如 CORS、CSRF）预留口子</li>
     * </ul>
     *
     * @param http HttpSecurity 构建器
     * @param jwtFilterProvider JWT认证过滤器提供者
     * @param corsProperties CORS 属性配置
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
            ObjectProvider<FrameAccessDeniedHandler> accessDeniedHandlerProvider
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
        if (accessDeniedHandler != null) {
            http.exceptionHandling().accessDeniedHandler(accessDeniedHandler);
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
            
        JwtAuthenticationFilter jwtFilter = jwtFilterProvider.getIfAvailable();
        if (jwtFilter != null) {
            log.info("Security auto-configuration: Registering JwtAuthenticationFilter");
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }
            
        return http.build();
    }
}
