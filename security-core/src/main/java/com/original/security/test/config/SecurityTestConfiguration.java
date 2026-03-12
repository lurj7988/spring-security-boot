package com.original.security.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.event.AuditEventPublisher;
import com.original.security.handler.FrameAccessDeniedHandler;
import com.original.security.handler.FrameAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

/**
 * 安全测试配置类。
 *
 * <p>提供测试环境中常用的安全配置 Bean。</p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>自动配置测试所需的 Handler Bean</li>
 *   <li>提供默认的 ObjectMapper 和 PasswordEncoder</li>
 *   <li>可与 @SecurityTest 注解配合使用</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @SpringBootTest
 * @Import(SecurityTestConfiguration.class)
 * class MySecurityTest {
 *     // 测试代码
 * }
 * }</pre>
 *
 * @author Claude
 * @since 1.0.0
 */
@Configuration
@Import({
        SecurityAutoConfiguration.class
})
@AutoConfigureBefore(SecurityAutoConfiguration.class)
public class SecurityTestConfiguration {

    /**
     * 创建默认的 ObjectMapper Bean。
     *
     * @return ObjectMapper 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * 创建默认的 PasswordEncoder Bean。
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建 Mock 的 AuditEventPublisher。
     *
     * @return Mock 的 AuditEventPublisher 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditEventPublisher auditEventPublisher(ApplicationEventPublisher publisher) {
        return mock(AuditEventPublisher.class);
    }

    /**
     * 创建认证入口点 Handler。
     *
     * @param objectMapper JSON 序列化器
     * @return FrameAuthenticationEntryPoint 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public FrameAuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return new FrameAuthenticationEntryPoint(objectMapper);
    }

    /**
     * 创建访问拒绝 Handler。
     *
     * @param objectMapper          JSON 序列化器
     * @param auditEventPublisher   审计事件发布器
     * @return FrameAccessDeniedHandler 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public FrameAccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper,
                                                         AuditEventPublisher auditEventPublisher) {
        return new FrameAccessDeniedHandler(objectMapper, auditEventPublisher);
    }
}
