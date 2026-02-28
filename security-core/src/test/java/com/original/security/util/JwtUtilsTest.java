package com.original.security.util;

import com.original.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for JwtUtils.
 * 
 * @author bmad
 * @since 0.1.0
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() throws Exception {
        jwtProperties = new JwtProperties();
        // A standard 256-bit secure key in base64 format for HMAC-SHA256, required by jjwt
        // "thisisaverysecuresecretkeythatismorethan256bitslong1234567890=" encoded
        jwtProperties.setSecret("dGhpc2lzYXZlcnlzZWN1cmVzZWNyZXRrZXl0aGF0aXNtb3JldGhhbjI1NmJpdHNsb25nMTIzNDU2Nzg5MD0=");
        jwtProperties.setExpiration(3600L);

        jwtUtils = new JwtUtils(jwtProperties);
        jwtUtils.afterPropertiesSet();
    }

    @Test
    void testFailFastOnMissingSecret() {
        JwtProperties badProps = new JwtProperties();
        badProps.setSecret(null);
        JwtUtils badUtils = new JwtUtils(badProps);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, badUtils::afterPropertiesSet);
        assertTrue(StringUtils.hasText(ex.getMessage()));
    }

    @Test
    void testFailFastOnShortSecret_Base64DecodedKeyTooShort() {
        JwtProperties badProps = new JwtProperties();
        // A very short secret - base64 encoded "short" (5 bytes, too short for HS256)
        badProps.setSecret("c2hvcnQ=");
        JwtUtils badUtils = new JwtUtils(badProps);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, badUtils::afterPropertiesSet);
        assertTrue(ex.getMessage().contains("256 bits"));
    }

    @Test
    void testFailFastOnInvalidBase64Secret() {
        JwtProperties badProps = new JwtProperties();
        // An invalid base64 string (not valid base64 at all)
        badProps.setSecret("this-is-not-valid-base64!");
        JwtUtils badUtils = new JwtUtils(badProps);

        Exception ex = assertThrows(Exception.class, badUtils::afterPropertiesSet);
        assertTrue(ex.getMessage().contains("base64-encoded") || ex.getCause() != null);
    }

    @Test
    void testGenerateTokenAndParse() {
        String username = "admin";
        Collection<String> authorities = Arrays.asList("ROLE_ADMIN", "user:write");

        String token = jwtUtils.generateToken(username, authorities);
        assertNotNull(token);
        
        Claims claims = jwtUtils.parseToken(token);
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        
        String authStr = claims.get("authorities", String.class);
        assertNotNull(authStr);
        assertTrue(authStr.contains("ROLE_ADMIN"));
        assertTrue(authStr.contains("user:write"));
    }

    @Test
    void testValidateValidToken() {
        String token = jwtUtils.generateToken("testuser", Arrays.asList("ROLE_USER"));
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void testValidateExpiredToken() throws Exception {
        // Set an expiration of 0 ms (so it expires immediately)
        jwtProperties.setExpiration(0L); 
        // Need to recreate utils to apply properties
        jwtUtils = new JwtUtils(jwtProperties);
        jwtUtils.afterPropertiesSet();

        String token = jwtUtils.generateToken("testuser", Arrays.asList("ROLE_USER"));
        
        // Wait a tiny bit just in case
        Thread.sleep(10);
        
        assertFalse(jwtUtils.validateToken(token));
        
        // Attempting to parse an expired token should throw ExpiredJwtException usually, 
        // but currently we just want to ensure validateToken handles it
    }

    @Test
    void testValidateMalformedToken() {
        assertFalse(jwtUtils.validateToken("bad.malformed.token"));
    }

    @Test
    void testValidateTamperedToken() {
        String token = jwtUtils.generateToken("admin", Arrays.asList("ROLE_ADMIN"));
        // modify token payload
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // tamper with payload part
        String tampered = parts[0] + "." + "eyJzdWIiOiJhZG1pbiIsImF1dGhvcml0aWVzIjoiUk9MRV9BQ0tFUiIsImV4cCI6MjAwMDAwMDAwMH0" + "." + parts[2];

        assertFalse(jwtUtils.validateToken(tampered));
    }

    @Test
    void testGenerateToken_EmptyAuthorities_ReturnsValidToken() {
        String username = "testuser";
        Collection<String> authorities = Collections.emptyList();

        String token = jwtUtils.generateToken(username, authorities);
        assertNotNull(token);

        Claims claims = jwtUtils.parseToken(token);
        assertEquals(username, claims.getSubject());

        String authStr = claims.get("authorities", String.class);
        assertEquals("", authStr);
    }
}
