package com.original.security.config;

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
import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = NetworkSecurityAutoConfigurationTest.TestAppConfig.class, properties = {
    "security.config.validation=false",
    "security.network.cors.enabled=true",
    "security.network.cors.allowed-origins=https://example.com"
})
@AutoConfigureMockMvc
public class NetworkSecurityAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEnableSecurityBoot_RegistersCorsConfigurationSource() {
        // Assert CorsConfigurationSource is registered
        CorsConfigurationSource corsSource = applicationContext.getBean(CorsConfigurationSource.class);
        assertNotNull(corsSource, "CorsConfigurationSource should be registered");
    }

    @Test
    public void testCorsFilterChain_AllowsOptionsRequestWithCorrectOrigin() throws Exception {
        // Perform Pre-flight OPTIONS request
        mockMvc.perform(options("/any-endpoint")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "GET"))
               .andExpect(status().isOk())
               .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
               .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    public void testCorsFilterChain_RejectsOptionsRequestWithWrongOrigin() throws Exception {
        // Perform Pre-flight OPTIONS request with forbidden origin
        mockMvc.perform(options("/any-endpoint")
                .header("Origin", "https://evil.com")
                .header("Access-Control-Request-Method", "GET"))
               .andExpect(status().isForbidden())
               .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    public void testCorsEnabled_ReturnsCorsHeaders() throws Exception {
        mockMvc.perform(get("/any-endpoint")
                .header("Origin", "https://example.com"))
               .andExpect(status().isOk())
               .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"));
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
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
            http
                .cors().configurationSource(corsConfigurationSource)
                .and()
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/any-endpoint").permitAll()
                    .anyRequest().authenticated();
            return http.build();
        }
    }
}
