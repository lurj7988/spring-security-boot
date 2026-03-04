package com.original.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.config.RememberMeAutoConfiguration;
import com.original.security.config.SecurityAutoConfiguration;
import com.original.security.config.NetworkSecurityAutoConfiguration;
import com.original.security.config.CorsProperties;
import com.original.security.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.servlet.http.Cookie;
import java.util.Date;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Remember Me 集成测试。
 * 验证 Remember Me 配置与基于 Token 的自动登录功能。
 */
@SpringBootTest(classes = RememberMeIntegrationTest.TestAppConfig.class, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "security.config.validation=false",
        "security.remember-me.enabled=true",
        "security.remember-me.key=test-secret-key",
        "security.remember-me.token-validity-seconds=3600",
        "security.network.csrf.enabled=false",
        "security.network.cors.enabled=false"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RememberMeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersistentTokenRepository tokenRepository;

    @Test
    @DisplayName("AccessProtectedResource_WithoutRememberMeCookie_Returns401")
    void testAccessProtectedResource_WithoutRememberMeCookie_Returns401() throws Exception {
        mockMvc.perform(get("/api/test/secure")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AccessProtectedResource_WithInvalidRememberMeCookie_Returns401")
    void testAccessProtectedResource_WithInvalidRememberMeCookie_Returns401() throws Exception {
        // 提供一个无效的 remember-me cookie (如格式不正确或 Token 不存在)
        mockMvc.perform(get("/api/test/secure")
                .cookie(new Cookie("remember-me", "invalid-remember-me-token-value"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login_WithRememberMe_SetsCookie")
    void testLogin_WithRememberMe_SetsCookie() throws Exception {
        // Prepare login request with rememberMe = true
        String requestBody = "{\"username\":\"testuser\",\"password\":\"pass\",\"rememberMe\":true}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie().exists("remember-me"));
    }

    @Test
    @DisplayName("AccessProtectedResource_WithValidRememberMeCookie_ReturnsSuccess")
    void testAccessProtectedResource_WithValidRememberMeCookie_ReturnsSuccess() throws Exception {
        // Base64 encode "seriesId:tokenValue"
        String cookieValue = java.util.Base64.getEncoder().encodeToString("seriesId:tokenValue".getBytes());

        mockMvc.perform(get("/api/test/secure")
                .cookie(new Cookie("remember-me", cookieValue))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("secure-data"));
    }

    @Test
    @DisplayName("Logout_WithRememberMeCookie_ClearsCookie")
    void testLogout_WithRememberMeCookie_ClearsCookie() throws Exception {
        // Prepare authentication to bypass 401
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "pass",
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/logout")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie().exists("remember-me"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie().maxAge("remember-me", 0));
    }

    @RestController
    static class TestController {
        @GetMapping("/api/test/secure")
        public Object secureEndpoint() {
            return com.original.security.core.Response.successBuilder("secure-data").build();
        }
    }

    @Configuration
    @Import({
            org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
            org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
            org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration.class,
            NetworkSecurityAutoConfiguration.class,
            RememberMeAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            com.original.security.controller.AuthenticationController.class
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

        @Bean
        public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
            return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetailsService userDetailsService = mock(UserDetailsService.class);
            UserDetails user = User.withUsername("testuser").password("pass").authorities("ROLE_USER").build();
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
            return userDetailsService;
        }

        @Bean
        public PersistentTokenRepository persistentTokenRepository() {
            PersistentTokenRepository repository = mock(PersistentTokenRepository.class);
            PersistentRememberMeToken token = new PersistentRememberMeToken(
                    "testuser", "seriesId", "tokenValue", new Date());
            when(repository.getTokenForSeries(anyString())).thenReturn(token);
            // 配置 createNewToken 方法，用于测试登录时创建 Remember Me Token
            org.mockito.Mockito.doNothing().when(repository).createNewToken(org.mockito.ArgumentMatchers.any(PersistentRememberMeToken.class));
            // 配置 updateToken 方法，用于测试 Remember Me Token 更新
            org.mockito.Mockito.doNothing().when(repository).updateToken(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(Date.class));
            return repository;
        }
    }
}
