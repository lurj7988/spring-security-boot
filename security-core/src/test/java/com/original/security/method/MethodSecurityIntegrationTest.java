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
import org.springframework.security.access.annotation.Secured;
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

import javax.annotation.security.RolesAllowed;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 方法级安全注解集成测试。
 * <p>
 * 测试 @PreAuthorize 注解的角色和权限检查功能，以及其他方法级安全注解。
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

        /**
         * 需要 ADMIN 或 USER 任意角色的端点（hasAnyRole）。
         */
        @GetMapping("/any-role")
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public String anyRole() {
            return "any-role-success";
        }

        /**
         * 需要任意权限的端点（hasAnyAuthority）。
         */
        @GetMapping("/any-auth")
        @PreAuthorize("hasAnyAuthority('user:read', 'user:write')")
        public String anyAuthority() {
            return "any-auth-success";
        }

        /**
         * 使用 @Secured 注解的端点。
         */
        @GetMapping("/secured-admin")
        @Secured("ROLE_ADMIN")
        public String securedAdmin() {
            return "secured-admin-success";
        }

        /**
         * 使用 @RolesAllowed 注解的端点。
         */
        @GetMapping("/roles-allowed-admin")
        @RolesAllowed("ADMIN")
        public String rolesAllowedAdmin() {
            return "roles-allowed-admin-success";
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
                    .andExpect(status().isForbidden());
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
                    .andExpect(status().isUnauthorized());
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
     * hasAnyRole 表达式测试。
     */
    @Nested
    @DisplayName("hasAnyRole Expression Tests")
    class HasAnyRoleTests {

        @Test
        @DisplayName("hasAnyRole('ADMIN', 'USER') with ADMIN role - should succeed")
        @WithMockUser(roles = "ADMIN")
        void testHasAnyRole_AdminRole_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/any-role"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasAnyRole('ADMIN', 'USER') with USER role - should succeed")
        @WithMockUser(roles = "USER")
        void testHasAnyRole_UserRole_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/any-role"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasAnyRole('ADMIN', 'USER') with no matching role - should return 403")
        @WithMockUser(roles = "GUEST")
        void testHasAnyRole_NoMatchingRole_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/any-role"))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * hasAnyAuthority 表达式测试。
     */
    @Nested
    @DisplayName("hasAnyAuthority Expression Tests")
    class HasAnyAuthorityTests {

        @Test
        @DisplayName("hasAnyAuthority('user:read', 'user:write') with read authority - should succeed")
        @WithMockUser(authorities = "user:read")
        void testHasAnyAuthority_ReadAuthority_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/any-auth"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasAnyAuthority('user:read', 'user:write') with write authority - should succeed")
        @WithMockUser(authorities = "user:write")
        void testHasAnyAuthority_WriteAuthority_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/any-auth"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("hasAnyAuthority('user:read', 'user:write') with no matching authority - should return 403")
        @WithMockUser(authorities = "user:delete")
        void testHasAnyAuthority_NoMatchingAuthority_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/any-auth"))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * @Secured 注解测试。
     */
    @Nested
    @DisplayName("@Secured Annotation Tests")
    class SecuredAnnotationTests {

        @Test
        @DisplayName("@Secured('ROLE_ADMIN') with ADMIN role - should succeed")
        @WithMockUser(roles = "ADMIN")
        void testSecured_AdminRole_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/secured-admin"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("@Secured('ROLE_ADMIN') with USER role - should return 403")
        @WithMockUser(roles = "USER")
        void testSecured_UserRole_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/secured-admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("@Secured('ROLE_ADMIN') without authentication - should return 401")
        void testSecured_NoAuth_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/secured-admin"))
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * @RolesAllowed 注解测试。
     */
    @Nested
    @DisplayName("@RolesAllowed Annotation Tests")
    class RolesAllowedAnnotationTests {

        @Test
        @DisplayName("@RolesAllowed('ADMIN') with ADMIN role - should succeed")
        @WithMockUser(roles = "ADMIN")
        void testRolesAllowed_AdminRole_ReturnsOk() throws Exception {
            mockMvc.perform(get("/api/test/roles-allowed-admin"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("@RolesAllowed('ADMIN') with USER role - should return 403")
        @WithMockUser(roles = "USER")
        void testRolesAllowed_UserRole_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/roles-allowed-admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("@RolesAllowed('ADMIN') without authentication - should return 401")
        void testRolesAllowed_NoAuth_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/test/roles-allowed-admin"))
                    .andExpect(status().isUnauthorized());
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
