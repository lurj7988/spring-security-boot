package com.original.security.config;

import org.springframework.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for CORS disabled configuration.
 * Verifies that when security.network.cors.enabled=false,
 * no CORS headers are returned.
 */
@SpringBootTest(classes = NetworkSecurityAutoConfigurationDisabledTest.TestAppConfig.class, properties = {
    "security.config.validation=false",
    "security.network.cors.enabled=false"
})
@AutoConfigureMockMvc
public class NetworkSecurityAutoConfigurationDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCorsDisabled_OptionsRequest_NoCorsHeaders() throws Exception {
        // When CORS is disabled, OPTIONS pre-flight requests are rejected (403)
        // and no CORS headers are returned
        mockMvc.perform(options("/any-endpoint")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "GET"))
               .andExpect(status().isForbidden())
               .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
               .andExpect(header().doesNotExist("Access-Control-Allow-Methods"));
    }

    @Test
    public void testCorsDisabled_GetRequest_NoCorsHeaders() throws Exception {
        mockMvc.perform(get("/any-endpoint")
                .header("Origin", "https://example.com"))
               .andExpect(status().isOk())
               .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @RestController
    static class TestController {

        @GetMapping("/any-endpoint")
        public String anyEndpoint() {
            return "ok";
        }
    }

    @Configuration
    @Import({NetworkSecurityAutoConfiguration.class})
    static class TestAppConfig {

        @Bean
        public TestController testController() {
            return new TestController();
        }

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers("/any-endpoint").permitAll()
                    .anyRequest().authenticated()
                .and()
                .cors().disable();
            return http.build();
        }
    }
}
