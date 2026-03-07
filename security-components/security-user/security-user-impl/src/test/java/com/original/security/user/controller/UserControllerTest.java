package com.original.security.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.UserDTO;
import com.original.security.user.config.TestSecurityConfig;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.repository.RoleRepository;
import com.original.security.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
     * 清理测试环境 - 确保每个测试前清空数据库
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * 清理测试环境 - 确保每个测试后清除安全上下文
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 设置已认证用户的安全上下文
     */
    private void setAuthenticatedUser(String username) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

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
     * 测试 GET /api/users/{userId} - 成功获取用户详情
     */
    @Test
    void testGetUser_ValidId_ReturnsUserDetails() throws Exception {
        // Given - 创建测试用户
        User testUser = createTestUser("testuser", "password", "test@example.com");

        // When & Then
        mockMvc.perform(get("/api/users/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    /**
     * 测试 GET /api/users/{userId} - 用户不存在
     */
    @Test
    void testGetUser_NonExistentId_ReturnsError() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{userId}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))  // UserNotFoundException 返回 404
                .andExpect(jsonPath("$.message").value(containsString("不存在")));
    }

    /**
     * 测试 GET /api/users - 获取用户列表（基本分页）
     */
    @Test
    void testListUsers_BasicPagination_ReturnsPagedList() throws Exception {
        // Given - 创建几个测试用户
        createTestUser("user1", "password", "user1@example.com");
        createTestUser("user2", "password", "user2@example.com");
        createTestUser("user3", "password", "user3@example.com");

        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    /**
     * 测试 GET /api/users - 获取空用户列表
     */
    @Test
    void testListUsers_EmptyList_ReturnsEmptyArray() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    /**
     * 测试 GET /api/users - 用户名模糊搜索
     */
    @Test
    void testListUsersWithFilters_UsernameContains_ReturnsFilteredResults() throws Exception {
        // Given - 创建测试用户
        createTestUser("alice_user", "password", "alice@example.com");
        createTestUser("bob_test", "password", "bob@example.com");
        createTestUser("charlie_user", "password", "charlie@example.com");

        // When & Then - 搜索包含"user"的用户名
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "user")
                        .param("enabled", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(2))); // 至少找到2个匹配用户

        // 验证返回的用户名都包含"test"模式
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "test")
                        .param("enabled", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1))); // 至少找到1个匹配用户
    }

    /**
     * 测试 GET /api/users - 按启用状态筛选
     */
    @Test
    void testListUsersWithFilters_EnabledFilter_ReturnsFilteredResults() throws Exception {
        // Given - 创建启用和禁用的用户
        User enabledUser = createTestUser("enabled_user", "password", "enabled@example.com");
        enabledUser.setEnabled(true);
        userRepository.save(enabledUser);

        User disabledUser = createTestUser("disabled_user", "password", "disabled@example.com");
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        // When & Then - 搜索启用的用户
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(equalTo(1))) // 应该只有1个启用的用户
                .andExpect(jsonPath("$.data.content[0].username").value("enabled_user"))
                .andExpect(jsonPath("$.data.content[0].enabled").value(true));

        // When & Then - 搜索禁用的用户
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(equalTo(1))) // 应该只有1个禁用的用户
                .andExpect(jsonPath("$.data.content[0].username").value("disabled_user"))
                .andExpect(jsonPath("$.data.content[0].enabled").value(false));
    }

    /**
     * 测试 GET /api/users - 组合筛选（用户名和状态）
     */
    @Test
    void testListUsersWithFilters_CombinedFilters_ReturnsFilteredResults() throws Exception {
        // Given - 创建测试用户
        User enabledAlice = createTestUser("alice_enabled", "password", "alice1@example.com");
        enabledAlice.setEnabled(true);
        userRepository.save(enabledAlice);

        User disabledAlice = createTestUser("alice_disabled", "password", "alice2@example.com");
        disabledAlice.setEnabled(false);
        userRepository.save(disabledAlice);

        User enabledBob = createTestUser("bob_enabled", "password", "bob@example.com");
        enabledBob.setEnabled(true);
        userRepository.save(enabledBob);

        // When & Then - 搜索包含"alice"且启用的用户
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "alice")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(equalTo(1))) // 应该只有1个匹配的用户
                .andExpect(jsonPath("$.data.content[0].username").value("alice_enabled"))
                .andExpect(jsonPath("$.data.content[0].enabled").value(true));

        // When & Then - 搜索包含"alice"且禁用的用户
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("username", "alice")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(equalTo(1))) // 应该只有1个匹配的用户
                .andExpect(jsonPath("$.data.content[0].username").value("alice_disabled"))
                .andExpect(jsonPath("$.data.content[0].enabled").value(false));
    }

    /**
     * 测试 GET /api/users/me - 获取当前用户（成功场景）
     */
    @Test
    void testGetCurrentUser_AuthenticatedUser_ReturnsUserDetails() throws Exception {
        // Given - 清除安全上下文，创建测试用户并设置认证上下文
        SecurityContextHolder.clearContext();
        User testUser = createTestUser("authenticatedUser", "password", "auth@example.com");
        setAuthenticatedUser("authenticatedUser");

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(testUser.getId().intValue()))
                .andExpect(jsonPath("$.data.username").value("authenticatedUser"))
                .andExpect(jsonPath("$.data.email").value("auth@example.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    /**
     * 测试 GET /api/users/me - 未认证用户应返回 401
     */
    @Test
    void testGetCurrentUser_UnauthenticatedUser_Returns401() throws Exception {
        // Given - 确保无认证上下文（未认证状态）
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value(containsString("未认证")));
    }

    /**
     * 测试 GET /api/users/me - 用户不存在于数据库应返回 404
     */
    @Test
    void testGetCurrentUser_UserNotInDatabase_Returns404() throws Exception {
        // Given - 清除安全上下文并设置认证上下文但用户不存在于数据库
        SecurityContextHolder.clearContext();
        setAuthenticatedUser("nonExistentUser");

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(containsString("不存在")));
    }

    /**
     * 测试 GET /api/users/me - 用户被禁用应返回 403
     */
    @Test
    void testGetCurrentUser_DisabledUser_Returns403() throws Exception {
        // Given - 清除安全上下文并创建禁用用户
        SecurityContextHolder.clearContext();
        User disabledUser = createTestUser("disabledUser", "password", "disabled@example.com");
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        // 设置认证上下文
        setAuthenticatedUser("disabledUser");

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(containsString("已禁用")));
    }

    /**
     * 辅助方法：创建测试用户
     * 使用 PasswordEncoder 加密密码，与实际业务逻辑保持一致
     */
    private User createTestUser(String username, String password, String email) {
        User user = new User(username, passwordEncoder.encode(password), email);
        user.setEnabled(true);

        // 分配默认角色
        Role defaultRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = new Role("USER", "默认用户角色");
                    return roleRepository.save(role);
                });
        user.addRole(defaultRole);

        return userRepository.save(user);
    }
}
