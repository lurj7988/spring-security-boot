package com.original.security.user.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 用户模块配置
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(UserProperties.class)
public class UserConfig {
}
