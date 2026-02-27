package com.original.security.config;

import com.original.security.annotation.EnableSecurityBoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Test class for verifying {@link SecurityAutoConfiguration} conditional replacement.
 */
@SpringBootTest(classes = SecurityAutoConfigurationConditionalTest.CustomConfig.class, properties = {
    "security.config.validation=false"
})
public class SecurityAutoConfigurationConditionalTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testEnableSecurityBoot_WithCustomPasswordEncoder_ShouldUseCustom() {
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        assertEquals(CustomConfig.DummyPasswordEncoder.class, passwordEncoder.getClass(),
                     "Should use the explicitly provided PasswordEncoder bean");
    }

    @Test
    public void testEnableSecurityBoot_WithCustomAuthenticationManager_ShouldUseCustom() {
        AuthenticationManager authManager = applicationContext.getBean(AuthenticationManager.class);
        assertEquals(CustomConfig.DummyAuthenticationManager.class, authManager.getClass(),
                     "Should use the explicitly provided AuthenticationManager bean");
    }

    @Test
    public void testEnableSecurityBoot_WithCustomSecurityFilterChain_ShouldUseCustom() {
        SecurityFilterChain filterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertEquals(2, filterChain.getFilters().size(),
                     "Should use the explicitly provided SecurityFilterChain bean which has 2 mapped filters");
    }

    @Configuration
    @EnableSecurityBoot
    static class CustomConfig {

        @Bean
        public PasswordEncoder customPasswordEncoder() {
            return new DummyPasswordEncoder();
        }

        @Bean
        public AuthenticationManager customAuthenticationManager() {
            return new DummyAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain customFilterChain() {
            SecurityFilterChain filterChain = mock(SecurityFilterChain.class);
            // using a dummy size to verify
            org.mockito.Mockito.when(filterChain.getFilters()).thenReturn(java.util.Arrays.asList(null, null));
            return filterChain;
        }

        static class DummyPasswordEncoder implements PasswordEncoder {
            @Override
            public String encode(CharSequence rawPassword) {
                return "dummy";
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return true;
            }
        }

        static class DummyAuthenticationManager implements AuthenticationManager {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                throw new BadCredentialsException("Dummy");
            }
        }
    }
}
