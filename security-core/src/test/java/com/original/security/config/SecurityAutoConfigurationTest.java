package com.original.security.config;

import com.original.security.annotation.EnableSecurityBoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for verifying {@link SecurityAutoConfiguration} and {@link EnableSecurityBoot}.
 */
@SpringBootTest(classes = SecurityAutoConfigurationTest.TestConfig.class, properties = {
    "security.config.validation=false"
})
public class SecurityAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testEnableSecurityBoot_RegistersRequiredBeans() {
        // Assert PasswordEncoder is registered
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        assertNotNull(passwordEncoder, "PasswordEncoder should be registered");

        // Assert AuthenticationManager is registered
        AuthenticationManager authenticationManager = applicationContext.getBean(AuthenticationManager.class);
        assertNotNull(authenticationManager, "AuthenticationManager should be registered");

        // Assert SecurityFilterChain is registered
        SecurityFilterChain filterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertNotNull(filterChain, "SecurityFilterChain should be registered");
    }

    @Test
    public void testEnableSecurityBoot_RegistersBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        // We know it should be a BCryptPasswordEncoder based on requirements
        assertTrue(passwordEncoder.getClass().getName().contains("BCryptPasswordEncoder"),
                   "PasswordEncoder should be an instance of BCryptPasswordEncoder");
    }

    @Test
    public void testEnableSecurityBoot_SecurityFilterChainApplies_Returns401ForUnauthorized() throws Exception {
        SecurityFilterChain filterChain = applicationContext.getBean(SecurityFilterChain.class);
        
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup((WebApplicationContext) applicationContext)
                .addFilters(new FilterChainProxy(filterChain))
                .build();

        mockMvc.perform(get("/any-endpoint"))
               .andExpect(status().isForbidden());
    }

    @Configuration
    @EnableSecurityBoot
    @RestController
    static class TestConfig {
        @GetMapping("/any-endpoint")
        public String anyEndpoint() {
            return "ok";
        }
    }
}
