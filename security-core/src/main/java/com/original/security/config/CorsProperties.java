package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration properties for CORS (Cross-Origin Resource Sharing).
 * Binds properties prefixed with "security.network.cors".
 *
 * @author Naulu
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.network.cors")
public class CorsProperties {

    /**
     * Whether to enable CORS configuration. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * Allowed origins (e.g., http://localhost:8080, https://example.com).
     * Can be customized to specific origins. Use "*" for any origin if needed, but consider security implications.
     */
    private List<String> allowedOrigins;

    /**
     * Allowed methods. Defaults to GET, POST, PUT, DELETE, OPTIONS.
     */
    private List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * Allowed headers. Defaults to "*".
     */
    private List<String> allowedHeaders = Collections.singletonList("*");

    /**
     * How long the response from a pre-flight request can be cached by clients.
     * Unit: seconds. Default: 1800 (30 minutes).
     */
    private Long maxAge = 1800L;

    /**
     * List of headers that a browser is allowed to access.
     * Default: null (no exposed headers).
     */
    private List<String> exposedHeaders;

    /**
     * @return true if CORS is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled whether to enable CORS
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return list of allowed origins
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * @param allowedOrigins list of allowed origins to set
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * @return list of allowed methods
     */
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * @param allowedMethods list of allowed methods to set
     */
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * @return list of allowed headers
     */
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /**
     * @param allowedHeaders list of allowed headers to set
     */
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * @return max age for pre-flight requests in seconds
     */
    public Long getMaxAge() {
        return maxAge;
    }

    /**
     * @param maxAge max age for pre-flight requests in seconds
     */
    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * @return list of exposed headers
     */
    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /**
     * @param exposedHeaders list of exposed headers to set
     */
    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }
}
