package com.original.security.tracing.config;

import com.original.security.tracing.AuthenticationTracingFilter;
import com.original.security.tracing.DefaultSecurityTracer;
import com.original.security.tracing.JwtValidationTracingInterceptor;
import com.original.security.tracing.SecurityTracer;
import com.original.security.tracing.TracingConstants;
import com.original.security.tracing.feign.TracingFeignInterceptor;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 分布式追踪自动配置类。
 * <p>
 * 当 Micrometer Tracing 可用且 security.tracing.enabled=true 时生效。
 * 自动配置 SecurityTracer Bean，为安全组件提供分布式追踪能力。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(Tracer.class)
@AutoConfigureAfter(name = "org.springframework.boot.actuate.autoconfigure.tracing.MicrometerTracingAutoConfiguration",
        value = {})
@EnableConfigurationProperties(SecurityTracingProperties.class)
@ConditionalOnProperty(prefix = "security.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TracingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TracingAutoConfiguration.class);

    private final SecurityTracingProperties properties;

    public TracingAutoConfiguration(SecurityTracingProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化日志。
     */
    @PostConstruct
    public void init() {
        log.info("Security Distributed Tracing initialized - enabled={}, samplingRate={}, usernameMaskLength={}",
                properties.isEnabled(),
                properties.getSamplingRate(),
                properties.getUsernameMaskLength());
    }

    /**
     * 创建 SecurityTracer Bean。
     * <p>
     * 如果 Micrometer Tracer 可用，则创建 DefaultSecurityTracer 实现；
     * 否则创建 NoOpSecurityTracer（无操作实现）。
     *
     * @param tracer Micrometer Tracer 实例
     * @return SecurityTracer 实例
     */
    @Bean
    @ConditionalOnBean(Tracer.class)
    @ConditionalOnMissingBean
    public SecurityTracer securityTracer(Tracer tracer) {
        log.info("Creating DefaultSecurityTracer with Micrometer Tracer");

        return new DefaultSecurityTracer(
                tracer,
                properties.getUsernameMaskLength(),
                properties.getTokenMaskLength()
        );
    }

    /**
     * 创建 NoOp SecurityTracer Bean（当 Micrometer Tracer 不可用时）。
     *
     * @return NoOpSecurityTracer 实例
     */
    @Bean
    @ConditionalOnMissingBean({Tracer.class, SecurityTracer.class})
    public SecurityTracer noOpSecurityTracer() {
        log.info("Micrometer Tracer not available, creating NoOpSecurityTracer");
        // 使用 DefaultSecurityTracer(null) 作为 NoOp 实现，避免重复 masking 逻辑
        return new DefaultSecurityTracer(null,
                properties.getUsernameMaskLength(),
                properties.getTokenMaskLength());
    }

    /**
     * 创建 AuthenticationTracingFilter Bean。
     * <p>
     * 自动注入 SecurityTracer 和 Tracer，为每个安全请求创建追踪 Span。
     *
     * @param securityTracer SecurityTracer 实例
     * @param tracer         Micrometer Tracer 实例（可选）
     * @return AuthenticationTracingFilter 实例
     */
    @Bean
    @ConditionalOnBean(SecurityTracer.class)
    @ConditionalOnMissingBean(AuthenticationTracingFilter.class)
    public AuthenticationTracingFilter authenticationTracingFilter(
            SecurityTracer securityTracer,
            @org.springframework.beans.factory.annotation.Autowired(required = false) Tracer tracer) {
        log.info("Creating AuthenticationTracingFilter with recordRequestPath={}, recordAuthFailureDetails={}",
                properties.isRecordRequestPath(), properties.isRecordAuthFailureDetails());
        return new AuthenticationTracingFilter(securityTracer, tracer, properties);
    }

    /**
     * 创建 JwtValidationTracingInterceptor Bean。
     *
     * @param securityTracer SecurityTracer 实例
     * @return JwtValidationTracingInterceptor 实例
     */
    @Bean
    @ConditionalOnBean(SecurityTracer.class)
    @ConditionalOnClass(JwtValidationTracingInterceptor.class)
    @ConditionalOnMissingBean(JwtValidationTracingInterceptor.class)
    public JwtValidationTracingInterceptor jwtValidationTracingInterceptor(SecurityTracer securityTracer) {
        log.info("Creating JwtValidationTracingInterceptor");
        return new JwtValidationTracingInterceptor(securityTracer);
    }

    /**
     * 创建 TracingFeignInterceptor Bean。
     * <p>
     * 仅当 propagateToFeign=true 且 Feign 在 classpath 时创建。
     *
     * @param securityTracer SecurityTracer 实例
     * @return TracingFeignInterceptor 实例
     */
    @Bean
    @ConditionalOnBean(SecurityTracer.class)
    @ConditionalOnClass(name = "feign.RequestInterceptor")
    @ConditionalOnProperty(prefix = "security.tracing", name = "propagate-to-feign", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(TracingFeignInterceptor.class)
    public TracingFeignInterceptor tracingFeignInterceptor(SecurityTracer securityTracer) {
        log.info("Creating TracingFeignInterceptor for Feign context propagation");
        return TracingFeignInterceptor.create(securityTracer);
    }

}
