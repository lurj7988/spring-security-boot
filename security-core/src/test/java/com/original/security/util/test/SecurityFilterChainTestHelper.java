package com.original.security.util.test;

import com.original.security.config.CorsProperties;
import com.original.security.config.CspProperties;
import com.original.security.config.CsrfProperties;
import com.original.security.config.SecurityHeadersProperties;
import com.original.security.filter.JwtAuthenticationFilter;
import com.original.security.handler.FrameAccessDeniedHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * 测试辅助类，用于创建配置了安全响应头的 SecurityFilterChain。
 * <p>
 * 该类消除了多个集成测试中重复的 SecurityFilterChain 配置代码，
 * 确保测试配置的一致性和可维护性。
 * <p>
 * <b>设计说明：</b>此类的配置逻辑与 {@code SecurityAutoConfiguration} 中的 headers 配置
 * 保持同步。虽然存在代码重复，但这种设计是有意为之的：
 * <ul>
 *   <li>测试独立性：测试不依赖生产配置类，可以独立控制测试环境</li>
 *   <li>测试隔离：修改生产代码不会意外破坏测试</li>
 *   <li>明确性：测试意图在测试代码中清晰可见</li>
 * </ul>
 * <p>
 * 当更新 {@code SecurityAutoConfiguration} 中的 headers 配置时，请同步更新此类。
 *
 * @author Naulu
 * @since 1.0.0
 * @see com.original.security.config.SecurityAutoConfiguration
 */
public final class SecurityFilterChainTestHelper {

    private SecurityFilterChainTestHelper() {
        // 工具类，不允许实例化
    }

    /**
     * 创建配置了安全响应头的 SecurityFilterChain。
     *
     * @param http HttpSecurity 构建器
     * @param jwtFilterProvider JWT认证过滤器提供者
     * @param corsProperties CORS 属性配置
     * @param csrfTokenRepositoryProvider CSRF Token 存储库提供者
     * @param accessDeniedHandlerProvider 访问拒绝处理器提供者
     * @param headersPropertiesProvider 安全响应头属性提供者
     * @param cspPropertiesProvider CSP 属性提供者
     * @return 构建完毕的 SecurityFilterChain
     * @throws Exception 如果配置过程中出错
     */
    public static SecurityFilterChain createSecurityFilterChainWithHeaders(
            HttpSecurity http,
            ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider,
            CorsProperties corsProperties,
            ObjectProvider<CsrfTokenRepository> csrfTokenRepositoryProvider,
            ObjectProvider<FrameAccessDeniedHandler> accessDeniedHandlerProvider,
            ObjectProvider<SecurityHeadersProperties> headersPropertiesProvider,
            ObjectProvider<CspProperties> cspPropertiesProvider
    ) throws Exception {
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
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .cors().disable()
                .authorizeHttpRequests()
                    .antMatchers("/api/test/**").permitAll()
                    .anyRequest().authenticated();

        return http.build();
    }
}
