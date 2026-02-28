package com.original.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.dto.LoginRequest;
import com.original.security.dto.RefreshRequest;
import com.original.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证控制器单元测试。
 * <p>
 * 测试 {@link AuthenticationController} 的各种场景，包括成功和失败情况。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
public class AuthenticationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AuthenticationManager authenticationManager;
    private ObjectProvider<JwtUtils> jwtUtilsProvider;
    private JwtUtils jwtUtils;

    /**
     * 测试设置：初始化 mock 对象和 MockMvc。
     */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        objectMapper = new ObjectMapper();
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtilsProvider = mock(ObjectProvider.class);
        jwtUtils = mock(JwtUtils.class);

        when(jwtUtilsProvider.getIfAvailable()).thenReturn(jwtUtils);

        AuthenticationController controller = new AuthenticationController(authenticationManager, jwtUtilsProvider);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * 测试登录成功场景。
     * 验证返回 200 状态码和正确的 token。
     */
    @Test
    void testLogin_ValidCredentials_ReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtUtils.generateToken(anyString(), any())).thenReturn("mock-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.body.token").value("mock-token"))
                .andExpect(jsonPath("$.body.user").value("admin"))
                .andExpect(jsonPath("$.body.jwtEnabled").value(true));
    }

    /**
     * 测试登录失败场景（错误密码）。
     * 验证返回错误响应且不泄露敏感信息。
     */
    @Test
    void testLogin_InvalidPassword_ReturnsError() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                // 验证不泄露具体的异常信息（如 "Bad credentials"）
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    /**
     * 测试登出场景。
     * 验证返回成功响应。
     */
    @Test
    void testLogout_ClearsContext_ReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试 Token 刷新成功场景。
     * 验证返回新的 token。
     */
    @Test
    void testRefresh_ValidToken_ReturnsNewToken() throws Exception {
        RefreshRequest request = new RefreshRequest();
        request.setToken("old-token");

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "admin");
        claimsMap.put("authorities", "ROLE_ADMIN");
        Claims claims = new DefaultClaims(claimsMap);

        when(jwtUtils.parseToken("old-token")).thenReturn(claims);
        when(jwtUtils.generateToken(anyString(), any())).thenReturn("new-token");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.body.token").value("new-token"));
    }

    /**
     * 测试刷新过期 Token。
     * 验证返回适当的错误消息。
     */
    @Test
    void testRefresh_ExpiredToken_ReturnsError() throws Exception {
        RefreshRequest request = new RefreshRequest();
        request.setToken("expired-token");

        when(jwtUtils.parseToken("expired-token")).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("Token 已过期，请重新登录"));
    }

    /**
     * 测试使用空 Token 刷新。
     * 验证返回错误消息。
     */
    @Test
    void testRefresh_EmptyToken_ReturnsError() throws Exception {
        RefreshRequest request = new RefreshRequest();
        request.setToken("");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("刷新 Token 不能为空"));
    }

    /**
     * 测试登录时 JWT 未启用。
     * 验证返回的用户信息中 token 为 null，但 jwtEnabled 标志为 false。
     */
    @Test
    void testLogin_JwtNotEnabled_ReturnsUserWithoutToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

        // 模拟 JwtUtils 不可用
        when(jwtUtilsProvider.getIfAvailable()).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.body.user").value("admin"))
                .andExpect(jsonPath("$.body.token").isEmpty())
                .andExpect(jsonPath("$.body.jwtEnabled").value(false));
    }
}
