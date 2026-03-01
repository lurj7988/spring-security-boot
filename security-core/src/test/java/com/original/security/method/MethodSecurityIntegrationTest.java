package com.original.security.method;

import com.original.security.config.MethodSecurityConfiguration;
import com.original.security.config.SecurityAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 方法级安全注解集成测试。
 * <p>
 * 测试 @PreAuthorize 注解的角色和权限检查功能。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class MethodSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    /**
     * 测试应用配置类。
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            SecurityAutoConfiguration.class, 
            MethodSecurityConfiguration.class, 
            TestControllers.class,
            com.original.security.handler.FrameAccessDeniedHandler.class,
            com.original.security.handler.FrameAuthenticationEntryPoint.class
    })
    static class TestApplication {
    }

    /**
     * 测试控制器类集合。
     */
    @RestController
    @RequestMapping("/api/test")
    static class TestControllers {

        /**
         * 需要 ADMIN 角色的端点。
         */
        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminOnly() {
            return "admin-success";
        }

        /**
         * 需要 USER 角色的端点。
         */
        @GetMapping("/user")
        @PreAuthorize("hasRole('USER')")
        public String userOnly() {
            return "user-success";
        }

        /**
         * 需要特定权限的端点。
         */
        @PostMapping("/write")
        @PreAuthorize("hasAuthority('user:write')")
        public String writePermission() {
            return "write-success";
        }

        /**
         * 需要组合权限的端点。
         */
        @GetMapping("/combined")
        @PreAuthorize("hasRole('ADMIN') and hasAuthority('user:read')")
        public String combinedPermissions() {
            return "combined-success";
        }

        /**
         * 允许任意已认证用户的端点。
         */
        @GetMapping("/authenticated")
        @PreAuthorize("isAuthenticated()")
        public String authenticatedOnly() {
            return "authenticated-success";
        }
    }

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /**
     * hasRole 表达式测试。
     */
    @Nested
    @DisplayName("hasRole Expression Tests")
    class HasRoleTests {

        @Test
        @DisplayName("hasRole('ADMIN') with ADMIN role - should succeed")
        @WithMockUser(roles = "ADMIN")
        void testHasRole_AdminRole_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/admin"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasRole('ADMIN') with USER role - should return 403")
        @WithMockUser(roles = "USER")
        void testHasRole_UserRole_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/admin"))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(403));
        }

        @Test
        @DisplayName("hasRole('USER') with USER role - should succeed")
        @WithMockUser(roles = "USER")
        void testHasRole_UserRole_UserEndpoint_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/user"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasRole('USER') with ADMIN role - should return 403 (ADMIN != USER)")
        @WithMockUser(roles = "ADMIN")
        void testHasRole_AdminRole_UserEndpoint_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/user"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("hasRole without authentication - should return 401")
        void testHasRole_NoAuth_ReturnsForbidden() throws Exception {
            // 在添加了 AuthenticationEntryPoint 后，未认证请求被正确拦截并返回 401
            mockMvc.perform(get("/api/test/admin"))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }

    /**
     * hasAuthority 表达式测试。
     */
    @Nested
    @DisplayName("hasAuthority Expression Tests")
    class HasAuthorityTests {

        @Test
        @DisplayName("hasAuthority('user:write') with correct authority - should succeed")
        @WithMockUser(authorities = "user:write")
        void testHasAuthority_CorrectAuthority_ReturnsOk() throws Exception {
            mockMvc.perform(post("/api/test/write").with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasAuthority('user:write') with wrong authority - should return 403")
        @WithMockUser(authorities = "user:read")
        void testHasAuthority_WrongAuthority_ReturnsForbidden() throws Exception {
            mockMvc.perform(post("/api/test/write").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("hasAuthority with role instead - should return 403 (ROLE_ADMIN != user:write)")
        @WithMockUser(roles = "ADMIN")
        void testHasAuthority_RoleOnly_ReturnsForbidden() throws Exception {
            mockMvc.perform(post("/api/test/write").with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * 组合表达式测试。
     */
    @Nested
    @DisplayName("Combined Expression Tests")
    class CombinedExpressionTests {

        @Test
        @DisplayName("hasRole('ADMIN') and hasAuthority('user:read') - both satisfied")
        @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN", "user:read"})
        void testCombinedExpression_BothSatisfied_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/combined"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasRole('ADMIN') and hasAuthority('user:read') - only role")
        @WithMockUser(username = "admin", roles = "ADMIN")
        void testCombinedExpression_OnlyRole_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/combined"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("hasRole('ADMIN') and hasAuthority('user:read') - only authority")
        @WithMockUser(username = "user", authorities = "user:read")
        void testCombinedExpression_OnlyAuthority_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/combined"))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * isAuthenticated 表达式测试。
     */
    @Nested
    @DisplayName("isAuthenticated Expression Tests")
    class IsAuthenticatedTests {

        @Test
        @DisplayName("isAuthenticated() with authenticated user - should succeed")
        @WithMockUser
        void testIsAuthenticated_WithUser_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/authenticated"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("isAuthenticated() without authentication - should return 401")
        void testIsAuthenticated_NoAuth_ReturnsForbidden() throws Exception {
            // 在添加了 AuthenticationEntryPoint 后，未认证请求被正确拦截并返回 401
            mockMvc.perform(get("/api/test/authenticated"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
