package com.original.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

/**
 * JWT Configuration properties.
 *
 * @author bmad
 * @since 0.1.0
 */
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * JWT signature secret key. Must be sufficiently secure.
     */
    private String secret;

    /**
     * JWT token expiration time in seconds. Default is 3600 (1 hour).
     */
    private Long expiration = 3600L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
