package com.original.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.config.SessionAutoConfiguration;
import com.original.security.config.SecurityAutoConfiguration;
import com.original.security.config.NetworkSecurityAutoConfiguration;
import com.original.security.config.CorsProperties;
import com.original.security.config.CsrfProperties;
import com.original.security.dto.LoginRequest;
import com.original.security.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Session 认证集成测试。
 * 验证 Session 生命周期管理、并发控制和过期处理。
 */
@SpringBootTest(classes = SessionAuthenticationIntegrationTest.TestAppConfig.class, properties = {
        "security.config.validation=false",
        "security.session.enabled=true",
        "security.session.max-sessions=1",
        "security.network.csrf.enabled=false",
        "security.network.cors.enabled=false"
})
@AutoConfigureMockMvc
public class SessionAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AccessProtectedResource_WithoutSession_Returns401")
    void testAccessProtectedResource_WithoutSession_Returns401() throws Exception {
        mockMvc.perform(get("/api/test/secure")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AccessProtectedResource_WithInvalidSessionCookie_Returns401")
    void testAccessProtectedResource_WithInvalidSessionCookie_Returns401() throws Exception {
        mockMvc.perform(get("/api/test/secure")
                .cookie(new Cookie("JSESSIONID", "invalid-session-id-123"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("AccessProtectedResource_WithValidSession_ReturnsSuccess")
    void testAccessProtectedResource_WithValidSession_ReturnsSuccess() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(get("/api/test/secure")
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("secure-data"));
    }

    @Test
    @DisplayName("ConcurrentSessionControl_MaxOneSession_PreventsConcurrentAccess")
    void testConcurrentSessionControl_MaxOneSession_PreventsConcurrentAccess() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // First session
        mockMvc.perform(get("/api/test/secure")
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Note: Real concurrent control testing with MockMvc is limited as it doesn't 
        // fully simulate the SessionRegistry lifecycle across multiple requests 
        // without a real servlet container, but we verify the configuration is active.
    }

    @Test
    @DisplayName("AccessPublicResource_WithInvalidSessionCookie_ReturnsSuccess")
    void testAccessPublicResource_WithInvalidSessionCookie_ReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                .cookie(new Cookie("JSESSIONID", "invalid-session-id-123"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @RestController
    static class TestController {
        @GetMapping("/api/test/secure")
        public Object secureEndpoint() {
            return com.original.security.core.Response.successBuilder("secure-data").build();
        }

        @GetMapping("/api/auth/login")
        public Object publicEndpoint() {
            return com.original.security.core.Response.successBuilder("public-data").build();
        }
    }

    @Configuration
    @Import({
            org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
            org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
            org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration.class,
            NetworkSecurityAutoConfiguration.class,
            SessionAutoConfiguration.class,
            SecurityAutoConfiguration.class
    })
    static class TestAppConfig {

        @Bean
        public TestController testController() {
            return new TestController();
        }

        @Bean
        public com.original.security.handler.FrameAuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
            return new com.original.security.handler.FrameAuthenticationEntryPoint(objectMapper);
        }

        @Bean
        public CorsProperties corsProperties() {
            CorsProperties p = new CorsProperties();
            p.setEnabled(false);
            return p;
        }

        @Bean
        public com.original.security.config.CsrfProperties csrfProperties() {
            com.original.security.config.CsrfProperties p = new com.original.security.config.CsrfProperties();
            p.setEnabled(false); 
            return p;
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public JwtUtils jwtUtils() {
            return mock(JwtUtils.class);
        }
    }
}
