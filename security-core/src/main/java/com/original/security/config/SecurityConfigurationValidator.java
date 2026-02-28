package com.original.security.config;

import com.original.security.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Validates the critical security configurations upon application startup.
 * Listens to {@link ApplicationReadyEvent} and checks required configuration properties.
 *
 * @author Naulu
 * @since 1.0.0
 * @see ConfigurationException
 * @see SecurityProperties
 */
@Configuration
@EnableConfigurationProperties({SecurityProperties.class, CorsProperties.class, CsrfProperties.class})
public class SecurityConfigurationValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfigurationValidator.class);

    /**
     * Default documentation URL for configuration help.
     */
    private static final String DEFAULT_DOC_URL = "https://docs.example.com/config";

    private final SecurityProperties securityProperties;
    private final CorsProperties corsProperties;
    private final CsrfProperties csrfProperties;
    private final Environment environment;

    /**
     * Constructs a new SecurityConfigurationValidator.
     *
     * @param securityProperties the security configuration properties
     * @param corsProperties the CORS configuration properties
     * @param environment the Spring environment for accessing configuration
     */
    public SecurityConfigurationValidator(SecurityProperties securityProperties, CorsProperties corsProperties, CsrfProperties csrfProperties, Environment environment) {
        this.securityProperties = securityProperties;
        this.corsProperties = corsProperties;
        this.csrfProperties = csrfProperties;
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!securityProperties.getConfig().isValidation()) {
            log.info("Spring Security Boot configuration validation is disabled.");
            return;
        }

        validateDatasource();
        validateCors();
        validateCsrf();

        log.info("Spring Security Boot configuration validation passed successfully.");
        logDefaultConfigurationValues();
    }

    private void validateDatasource() {
        String url = environment.getProperty("spring.datasource.url");
        if (url == null || url.trim().isEmpty()) {
            String errorMessage = formatErrorMessage(
                    "数据库连接未配置",
                    "  1. 添加到 application.properties:\n" +
                    "     spring.datasource.url=jdbc:mysql://localhost:3306/mydb\n" +
                    "     spring.datasource.username=root\n" +
                    "     spring.datasource.password=***\n" +
                    "\n" +
                    "  2. 或者禁用验证（不推荐）:\n" +
                    "     security.config.validation=false\n",
                    DEFAULT_DOC_URL
            );
            log.error(errorMessage);
            throw new ConfigurationException(errorMessage);
        }
    }

    private void validateCors() {
        if (corsProperties.isEnabled()) {
            if (corsProperties.getAllowedOrigins() == null || corsProperties.getAllowedOrigins().isEmpty()) {
                String errorMessage = formatErrorMessage(
                        "CORS 已启用，但未配置 allowed-origins",
                        "  1. 添加到 application.properties:\n" +
                        "     security.network.cors.allowed-origins=http://localhost:8080,https://example.com\n" +
                        "     # 如果是开发环境可以使用星号（但不推荐在生产环境）\n" +
                        "     # security.network.cors.allowed-origins=*\n" +
                        "\n" +
                        "  2. 或者禁用 CORS 功能:\n" +
                        "     security.network.cors.enabled=false\n",
                        DEFAULT_DOC_URL
                );
                log.error(errorMessage);
                throw new ConfigurationException(errorMessage);
            }
        }
    }

    private void validateCsrf() {
        if (!csrfProperties.isEnabled()) {
            log.warn("=== Spring Security Boot 安全警告 === CSRF 防护已被禁用，请确认是否符合安全策略");
        }
    }

    /**
     * Logs the default configuration values being used by the framework.
     */
    private void logDefaultConfigurationValues() {
        log.info("Configuration defaults: security.config.validation={}",
                securityProperties.getConfig().isValidation());
    }

    private String formatErrorMessage(String error, String solution, String docUrl) {
        return "\n=== Spring Security Boot 配置错误 ===\n\n" +
               "错误: " + error + "\n\n" +
               "解决方案:\n" + solution + "\n" +
               "文档: " + docUrl + "\n";
    }
}
