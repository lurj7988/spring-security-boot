package com.original.security.logging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.logging.DefaultSecurityLogger;
import com.original.security.logging.SecurityLogEvent;
import com.original.security.logging.SecurityLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 安全日志自动配置类。
 * <p>
 * 当 security.logging.enabled=true 时生效，注册 SecurityLogger Bean。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({SecurityLogger.class, ObjectMapper.class})
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@EnableConfigurationProperties(SecurityLoggingProperties.class)
@ConditionalOnProperty(prefix = "security.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityLoggingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityLoggingAutoConfiguration.class);

    private final SecurityLoggingProperties properties;

    public SecurityLoggingAutoConfiguration(SecurityLoggingProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化全局敏感数据脱敏器配置。
     */
    @PostConstruct
    public void initGlobalMaskingMode() {
        SecurityLogEvent.setGlobalMaskingMode(properties.getMaskingMode());
        log.info("Configured global sensitive data masking mode: {}", properties.getMaskingMode());
    }

    /**
     * 创建 SecurityLogger Bean。
     *
     * @param objectMapper JSON 序列化器
     * @return SecurityLogger 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityLogger securityLogger(ObjectMapper objectMapper) {
        log.info("Initializing SecurityLogger with jsonOutput={}, maskingMode={}, includeStackTrace={}",
                properties.isJsonOutput(), properties.getMaskingMode(), properties.isIncludeStackTrace());

        return new DefaultSecurityLogger(objectMapper, properties.isJsonOutput(), properties.isIncludeStackTrace());
    }
}
