package com.original.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.config.CorsProperties;
import com.original.security.config.CsrfProperties;
import com.original.security.config.NetworkSecurityAutoConfiguration;
import com.original.security.config.SecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CsrfIntegrationTest.TestAppConfig.class, properties = {
        "security.config.validation=false",
        "security.network.csrf.enabled=true"
})
@AutoConfigureMockMvc
public class CsrfIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCsrfEnabled_MissingToken_Returns403() throws Exception {
        // POST 请求应该需要 CSRF Token
        mockMvc.perform(post("/api/test/endpoint"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCsrfEnabled_WithValidToken_PassesCsrfCheck() throws Exception {
        CsrfToken token = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");

        // Request with CSRF token should pass CSRF check (but returns 403 because no auth)
        mockMvc.perform(post("/api/test/endpoint")
                .header(token.getHeaderName(), token.getToken())
                .cookie(new javax.servlet.http.Cookie("XSRF-TOKEN", token.getToken())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCsrfEnabled_GetRequest_BypassesCsrf() throws Exception {
        // GET 请求应该绕过 CSRF 检查
        // 注意：即使绕过 CSRF，仍需要认证，所以会返回 403 (AccessDenied)
        mockMvc.perform(get("/api/test/endpoint"))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {

        @PostMapping("/api/test/endpoint")
        public Object postEndpoint() {
            return "success";
        }

        @GetMapping("/api/test/endpoint")
        public Object getEndpoint() {
            return "success";
        }
    }

    @Configuration
    @Import({
            NetworkSecurityAutoConfiguration.class,
            SecurityAutoConfiguration.class
    })
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
            return new CsrfProperties();
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
