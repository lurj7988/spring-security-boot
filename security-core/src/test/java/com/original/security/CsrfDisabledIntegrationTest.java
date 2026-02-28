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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CsrfDisabledIntegrationTest.TestAppConfig.class, properties = {
        "security.config.validation=false",
        "security.network.csrf.enabled=false"
})
@AutoConfigureMockMvc
public class CsrfDisabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCsrfDisabled_MissingToken_Returns200() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isOk());
    }

    @RestController
    static class TestController {

        @PostMapping("/api/auth/login")
        public String login() {
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
            CsrfProperties c = new CsrfProperties();
            c.setEnabled(false);
            return c;
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
