package com.original.security.util;

import com.original.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Collection;
import java.util.Date;

/**
 * Utility class for JSON Web Token operations.
 * 
 * @author bmad
 * @since 0.1.0
 */
@Component
public class JwtUtils implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtProperties jwtProperties;
    private Key key;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void afterPropertiesSet() {
        String secret = jwtProperties.getSecret();
        if (!StringUtils.hasText(secret)) {
            log.warn("JWT secret is not configured. Please configure 'security.jwt.secret' property for production use.");
            throw new IllegalArgumentException("JWT secret cannot be null or empty. Please configure 'security.jwt.secret' property.");
        }
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (io.jsonwebtoken.security.WeakKeyException e) {
            throw new IllegalArgumentException("JWT secret is too short. It must be at least 256 bits (32 bytes) long. Please provide a valid base64-encoded secret.", e);
        } catch (Exception e) {
            // Base64 decoding failed or other error - secret is not valid base64
            log.error("JWT secret must be valid base64-encoded string. Invalid secret provided.");
            throw new IllegalArgumentException("JWT secret must be a valid base64-encoded string. " +
                "To generate a secure base64 secret, use: openssl rand -base64 32", e);
        }
    }

    /**
     * Generates a JWT token for the specified user and authorities.
     *
     * @param username the username
     * @param authorities the authorities of the user
     * @return a signed JWT token string
     */
    public String generateToken(String username, Collection<String> authorities) {
        long now = System.currentTimeMillis();
        long expirationMs = jwtProperties.getExpiration() * 1000;
        
        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", String.join(",", authorities))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Validates if the provided JWT token is well-formed, correctly signed, and not expired.
     *
     * @param token the JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Parses the given JWT token into Claims.
     *
     * @param token the JWT token string
     * @return the claims if valid
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
