package com.original.security.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CorsPropertiesTest {

    @Test
    void testDefaultValues() {
        CorsProperties properties = new CorsProperties();
        
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getAllowedOrigins()).isNull();
        assertThat(properties.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(properties.getAllowedHeaders()).containsExactly("*");
    }

    @Test
    void testSetters() {
        CorsProperties properties = new CorsProperties();
        
        properties.setEnabled(false);
        properties.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        properties.setAllowedMethods(Arrays.asList("GET", "POST"));
        properties.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getAllowedOrigins()).containsExactly("http://localhost:8080");
        assertThat(properties.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST");
        assertThat(properties.getAllowedHeaders()).containsExactly("Authorization", "Content-Type");
    }
}
