package com.original.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 独立配置网络级别的安全组件，例如 CORS 配置等。
 *
 * @author Naulu
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties({CorsProperties.class, CsrfProperties.class})
public class NetworkSecurityAutoConfiguration {

    private final CorsProperties corsProperties;

    public NetworkSecurityAutoConfiguration(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /**
     * 生成跨域配置源（CorsConfigurationSource），只要 enabled 不为 false 就生效。
     * 当应用未配置 allowed-origins 且 enable=true 时，将在 SecurityConfigurationValidator 阶段报错。
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.network.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (corsProperties.getAllowedOrigins() != null) {
            configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        }

        if (corsProperties.getAllowedMethods() != null) {
            configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        }

        if (corsProperties.getAllowedHeaders() != null) {
            configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        }

        // Apply max age for pre-flight requests
        if (corsProperties.getMaxAge() != null) {
            configuration.setMaxAge(corsProperties.getMaxAge());
        }

        // Apply exposed headers
        if (corsProperties.getExposedHeaders() != null && !corsProperties.getExposedHeaders().isEmpty()) {
            configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        }

        // 应对现代安全：如果配置了具体域名而不是 *，可以允许携带凭证
        if (corsProperties.getAllowedOrigins() != null && !corsProperties.getAllowedOrigins().contains("*")) {
            configuration.setAllowCredentials(true);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 生成 CSRF Token 存储库
     */
    @Bean
    @ConditionalOnProperty(prefix = "security.network.csrf", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CsrfTokenRepository csrfTokenRepository(CsrfProperties csrfProperties) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        if (csrfProperties.getTokenHeader() != null && !csrfProperties.getTokenHeader().isEmpty()) {
            repository.setHeaderName(csrfProperties.getTokenHeader());
        }
        return repository;
    }
}
