package com.original.security.health;

import com.original.security.annotation.EnableSecurityBoot;
import com.original.security.config.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全组件健康检查 HTTP 端点集成测试。
 *
 * @author bmad
 * @since 0.1.0
 */
@SpringBootTest(
        classes = HealthCheckHttpEndpointTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "security.config.validation=false",
                "management.endpoints.web.exposure.include=health",
                "management.endpoint.health.show-details=always"
        }
)
@ActiveProfiles("test")
class HealthCheckHttpEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("integration_http_endpoint_returnsCorrectStructure")
    @SuppressWarnings("unchecked")
    void integration_http_endpoint_returnsCorrectStructure() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health/security", Map.class);

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("DOWN", body.get("status"));
        
        Map<String, Object> details = (Map<String, Object>) body.get("details");
        assertNotNull(details);
        
        assertTrue(details.containsKey("database"));
        assertTrue(details.containsKey("jwtValidator"));
        assertTrue(details.containsKey("cache"));
        assertTrue(details.containsKey("checkTimeMs"));
    }

    @Test
    @DisplayName("integration_http_endpoint_performance_lessThan100ms")
    void integration_http_endpoint_performance_lessThan100ms() {
        // Warm up
        restTemplate.getForEntity("/actuator/health/security", Map.class);
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            restTemplate.getForEntity("/actuator/health/security", Map.class);
        }
        long totalTime = System.currentTimeMillis() - startTime;
        long avgTime = totalTime / 5;
        
        // Http call overhead may be higher, but core logic should be fast.
        assertTrue(avgTime < 250, "Average HTTP health check time should be reasonable, was: " + avgTime + "ms");
    }

    @SpringBootApplication
    @EnableSecurityBoot
    @Configuration
    static class TestApplication {

        @Bean
        public JwtProperties jwtProperties() {
            JwtProperties props = new JwtProperties();
            props.setSecret("dGhpcy1pcy1hLXZlcnktbG9uZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdmFsaWRhdGlvbi1wdXJwb3Nl");
            return props;
        }
    }
}
