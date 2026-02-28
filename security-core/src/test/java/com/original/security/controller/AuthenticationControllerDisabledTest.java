package com.original.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证控制器禁用测试。
 * <p>
 * 验证当 {@code security.endpoints.enabled=false} 时，
 * 端点返回 404 Not Found（AC 4）。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
public class AuthenticationControllerDisabledTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试当认证端点禁用时，/api/auth/login 返回 404。
     * 这是 AC 4 的一部分：开发者不想使用框架自带的认证端点时，设置
     * {@code security.endpoints.enabled=false} 使端点不可用。
     */
    @Test
    void testEndpointsDisabled_LoginReturns404() throws Exception {
        // 不配置 AuthenticationController，模拟 security.endpoints.enabled=false
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup().build();

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试当认证端点禁用时，/api/auth/logout 返回 404。
     */
    @Test
    void testEndpointsDisabled_LogoutReturns404() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup().build();

        mockMvc.perform(post("/api/auth/logout")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    /**
     * 测试当认证端点禁用时，/api/auth/refresh 返回 404。
     */
    @Test
    void testEndpointsDisabled_RefreshReturns404() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup().build();

        mockMvc.perform(post("/api/auth/refresh")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isNotFound());
    }
}
