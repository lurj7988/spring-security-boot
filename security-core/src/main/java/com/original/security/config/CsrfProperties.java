package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.network.csrf")
public class CsrfProperties {

    /**
     * Whether to enable CSRF protection.
     */
    private boolean enabled = true;

    /**
     * The token header name.
     */
    private String tokenHeader = "X-CSRF-TOKEN";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }
}
