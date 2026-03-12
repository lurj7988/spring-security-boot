package com.original.security.test.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@SecurityTest(classes = SecurityTestTest.TestApp.class)
class SecurityTestTest {

    @SpringBootApplication
    static class TestApp {
    }

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private MockMvc mockMvc;

    @Test
    void testSecurityTestContextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void testMockMvcIsAutoConfigured() {
        assertThat(mockMvc).isNotNull();
    }
}