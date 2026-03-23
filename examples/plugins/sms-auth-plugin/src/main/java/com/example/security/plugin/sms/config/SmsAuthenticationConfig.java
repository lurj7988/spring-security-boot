package com.example.security.plugin.sms.config;

import com.example.security.plugin.sms.SmsAuthenticationPlugin;
import com.example.security.plugin.sms.SmsAuthenticationProvider;
import com.example.security.plugin.sms.SmsVerifyCodeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 短信认证自动配置类。
 * <p>
 * 当 {@code security.sms.enabled=true}（默认）时自动配置短信认证相关组件。
 * </p>
 *
 * <p>配置项：</p>
 * <pre>
 * # application.properties
 * security.sms.enabled=true
 * security.sms.expire-seconds=300
 * security.sms.max-attempts=5
 * </pre>
 *
 * @author Example Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "security.sms.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SmsProperties.class)
public class SmsAuthenticationConfig {

    /**
     * 配置短信认证提供者。
     *
     * @param smsVerifyCodeService 验证码服务
     * @param userDetailsService   用户详情服务
     * @return 短信认证提供者
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsAuthenticationProvider smsAuthenticationProvider(
            SmsVerifyCodeService smsVerifyCodeService,
            UserDetailsService userDetailsService) {
        return new SmsAuthenticationProvider(smsVerifyCodeService, userDetailsService);
    }

    /**
     * 配置短信认证插件。
     *
     * @param authenticationProvider 短信认证提供者
     * @return 短信认证插件
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsAuthenticationPlugin smsAuthenticationPlugin(
            SmsAuthenticationProvider authenticationProvider) {
        return new SmsAuthenticationPlugin(authenticationProvider);
    }
}
