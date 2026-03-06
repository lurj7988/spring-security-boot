package com.original.security.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.UserDTO;
import com.original.security.user.config.TestSecurityConfig;
import com.original.security.user.entity.User;
import com.original.security.user.repository.RoleRepository;
import com.original.security.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 集成测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // 禁用安全过滤器以进行集成测试
@Transactional
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 测试 POST /api/users 完整流程 - 成功创建用户
     */
    @Test
    void testCreateUser_ValidInput_ReturnsSuccess() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("newuser@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    /**
     * 测试用户名已存在场景
     */
    @Test
    void testCreateUser_UsernameExists_ReturnsError() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("newuser2@example.com");

        // 先创建一个用户
        createTestUser("newuser", "pass", "old@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("已存在")));
    }

    /**
     * 测试邮箱已存在场景
     */
    @Test
    void testCreateUser_EmailExists_ReturnsError() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser2");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        // 先创建一个用户
        createTestUser("olduser", "pass", "test@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("已存在")));
    }

    /**
     * 测试参数验证失败场景 - 用户名为空
     */
    @Test
    void testCreateUser_EmptyUsername_ReturnsValidationError() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("不能为空")));
    }

    /**
     * 测试参数验证失败场景 - 邮箱格式不正确
     */
    @Test
    void testCreateUser_InvalidEmail_ReturnsValidationError() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("格式")));
    }

    /**
     * 测试参数验证失败场景 - 密码太短
     */
    @Test
    void testCreateUser_ShortPassword_ReturnsValidationError() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("short");
        request.setEmail("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("长度")));
    }

    /**
     * 测试响应格式验证
     */
    @Test
    void testCreateUser_ResponseFormatValidation() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("testuser@example.com");

        // When
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Then - 验证响应结构
        String response = result.getResponse().getContentAsString();
        assertNotNull(response);

        // 验证必要字段存在
        assertTrue(response.contains("\"code\":"));
        assertTrue(response.contains("\"message\":"));
        assertTrue(response.contains("\"data\":"));
        assertTrue(response.contains("\"timestamp\":"));

        // 验证密码不在响应中
        assertFalse(response.contains("\"password\""));
    }

    /**
     * 辅助方法：创建测试用户
     * 使用 PasswordEncoder 加密密码，与实际业务逻辑保持一致
     */
    private void createTestUser(String username, String password, String email) {
        User user = new User(username, passwordEncoder.encode(password), email);
        user.setEnabled(true);
        userRepository.save(user);
    }
}
