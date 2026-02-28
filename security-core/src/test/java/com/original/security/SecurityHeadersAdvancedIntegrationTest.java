package com.original.security;

import com.original.security.config.CorsProperties;
import com.original.security.config.CsrfProperties;
import com.original.security.config.CspProperties;
import com.original.security.config.SecurityHeadersProperties;
import com.original.security.filter.JwtAuthenticationFilter;
import com.original.security.handler.FrameAccessDeniedHandler;
import com.original.security.util.test.SecurityFilterChainTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 高级安全响应头集成测试，覆盖边界条件和自定义配置。
 *
 * @author Naulu
 * @since 1.0.0
 */
@SpringBootTest(classes = SecurityHeadersAdvancedIntegrationTest.TestAppConfig.class, properties = {
        "security.config.validation=false",
        "security.network.headers.enabled=true",
        "security.network.headers.frame-options=SAMEORIGIN",
        "security.network.headers.hsts-max-age=0",
        "security.network.csp.enabled=true",
        "security.network.csp.policy=default-src 'self'; script-src 'self' 'unsafe-inline'"
})
@AutoConfigureMockMvc
public class SecurityHeadersAdvancedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSecurityHeaders_SameOriginFrameOptions_ReturnsSameOrigin() throws Exception {
        mockMvc.perform(get("/api/test/headers").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    public void testSecurityHeaders_HstsMaxAgeZero_DisablesHsts() throws Exception {
        mockMvc.perform(get("/api/test/headers").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
    }

    @Test
    public void testSecurityHeaders_CustomCspPolicy_ReturnsCustomPolicy() throws Exception {
        mockMvc.perform(get("/api/test/headers").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'"));
    }

    @RestController
    static class TestController {
        @GetMapping("/api/test/headers")
        public String getEndpoint() {
            return "success";
        }
    }

    @Configuration
    static class TestAppConfig {
        @Bean
        public TestController testController() {
            return new TestController();
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
        public SecurityHeadersProperties securityHeadersProperties() {
            SecurityHeadersProperties p = new SecurityHeadersProperties();
            p.setEnabled(true);
            p.setFrameOptions("SAMEORIGIN");
            p.setHstsMaxAge(0);
            return p;
        }

        @Bean
        public CspProperties cspProperties() {
            CspProperties p = new CspProperties();
            p.setEnabled(true);
            p.setPolicy("default-src 'self'; script-src 'self' 'unsafe-inline'");
            return p;
        }

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                                                         ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider,
                                                         CorsProperties corsProperties,
                                                         ObjectProvider<CsrfTokenRepository> csrfTokenRepositoryProvider,
                                                         ObjectProvider<FrameAccessDeniedHandler> accessDeniedHandlerProvider,
                                                         ObjectProvider<com.original.security.config.SecurityHeadersProperties> headersPropertiesProvider,
                                                         ObjectProvider<com.original.security.config.CspProperties> cspPropertiesProvider) throws Exception {
            return SecurityFilterChainTestHelper.createSecurityFilterChainWithHeaders(
                    http, jwtFilterProvider, corsProperties, csrfTokenRepositoryProvider,
                    accessDeniedHandlerProvider, headersPropertiesProvider, cspPropertiesProvider);
        }
    }
}
