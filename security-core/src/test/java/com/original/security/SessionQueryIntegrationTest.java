package com.original.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.config.SecurityAutoConfiguration;
import com.original.security.config.SessionAutoConfiguration;
import com.original.security.config.NetworkSecurityAutoConfiguration;
import com.original.security.config.CorsProperties;
import com.original.security.controller.SessionController;
import com.original.security.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SessionQueryIntegrationTest.TestAppConfig.class, properties = {
        "security.config.validation=false",
        "security.network.csrf.enabled=false",
        "security.network.cors.enabled=false"
})
@AutoConfigureMockMvc
public class SessionQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Admin Access Get All Sessions - Returns OK")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllSessionsWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/sessions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("User Access Get All Sessions - Returns 403 Forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAllSessionsWithUserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/sessions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Access Get My Sessions - Returns OK")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetMySessionsWithUserRole() throws Exception {
        mockMvc.perform(get("/api/sessions/me")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("Unauthenticated Access Get My Sessions - Returns 401")
    void testGetMySessionsUnauthenticated_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/sessions/me")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Configuration
    @Import({
            org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
            org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
            org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
            NetworkSecurityAutoConfiguration.class,
            SessionAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            SessionController.class
    })
    static class TestAppConfig {

        @Bean
        public com.original.security.handler.FrameAuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
            return new com.original.security.handler.FrameAuthenticationEntryPoint(objectMapper);
        }

        @Bean
        public com.original.security.handler.FrameAccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper, org.springframework.context.ApplicationEventPublisher publisher) {
            return new com.original.security.handler.FrameAccessDeniedHandler(objectMapper, publisher);
        }

        @Bean
        public org.springframework.context.ApplicationEventPublisher applicationEventPublisher() {
            return mock(org.springframework.context.ApplicationEventPublisher.class);
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

        @Bean
        public UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }
    }
}
