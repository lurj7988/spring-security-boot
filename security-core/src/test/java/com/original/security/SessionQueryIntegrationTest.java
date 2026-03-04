package com.original.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.config.*;
import com.original.security.controller.SessionController;
import com.original.security.handler.FrameAccessDeniedHandler;
import com.original.security.handler.FrameAuthenticationEntryPoint;
import com.original.security.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
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
            WebMvcAutoConfiguration.class,
            JacksonAutoConfiguration.class,
            HttpMessageConvertersAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
            NetworkSecurityAutoConfiguration.class,
            SessionAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            SessionController.class
    })
    static class TestAppConfig {

        @Bean
        public FrameAuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
            return new FrameAuthenticationEntryPoint(objectMapper);
        }

        @Bean
        public FrameAccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper, ApplicationEventPublisher publisher) {
            return new FrameAccessDeniedHandler(objectMapper, publisher);
        }

        @Bean
        @org.springframework.context.annotation.Primary
        public ApplicationEventPublisher applicationEventPublisher() {
            return mock(ApplicationEventPublisher.class);
        }

        @Bean
        public CorsProperties corsProperties() {
            CorsProperties p = new CorsProperties();
            p.setEnabled(false);
            return p;
        }

        @Bean
        public CsrfProperties csrfProperties() {
            CsrfProperties p = new CsrfProperties();
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
