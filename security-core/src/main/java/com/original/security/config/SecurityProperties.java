package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security framework root configuration properties.
 * Binds properties prefixed with "security" from application configuration.
 *
 * <p>Example configuration in application.properties:
 * <pre>
 * security.config.validation=true
 * </pre>
 *
 * @author Naulu
 * @since 1.0.0
 * @see SecurityConfigurationValidator
 */
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private final Config config = new Config();

    /**
     * Returns the configuration settings for the security framework.
     *
     * @return the config settings
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Configuration settings for security framework behavior.
     */
    public static class Config {

        /**
         * Whether to enable framework configuration validation on startup.
         * Default is true.
         */
        private boolean validation = true;

        /**
         * Returns whether configuration validation is enabled.
         *
         * @return true if validation is enabled, false otherwise
         */
        public boolean isValidation() {
            return validation;
        }

        /**
         * Sets whether configuration validation should be enabled.
         *
         * @param validation true to enable validation, false to disable
         */
        public void setValidation(boolean validation) {
            this.validation = validation;
        }
    }
}
