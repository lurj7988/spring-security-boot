package com.original.security.user.config;

import com.original.security.config.SecurityProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 测试环境的安全配置
 * 提供测试所需的 SecurityProperties 和 PasswordEncoder bean
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@TestConfiguration
public class TestSecurityConfig {

    private static final int DEFAULT_PASSWORD_STRENGTH = 10;

    @Bean
    @Primary
    public SecurityProperties securityProperties() {
        SecurityProperties properties = new SecurityProperties();
        // 使用默认配置
        properties.getConfig().setValidation(true);
        properties.getCache().setMaximumSize(1000);
        properties.getCache().setTtlMinutes(30);
        return properties;
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(DEFAULT_PASSWORD_STRENGTH);
    }
}
