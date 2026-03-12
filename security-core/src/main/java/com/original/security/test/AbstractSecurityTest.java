package com.original.security.test;

import com.original.security.test.util.AuthenticationTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全测试基类。
 *
 * <p>提供安全测试的通用设置和清理功能。</p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>自动清理安全上下文</li>
 *   <li>提供便捷的认证设置方法</li>
 *   <li>提供常用的测试断言方法</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * class MySecurityTest extends AbstractSecurityTest {
 *
 *     @Test
 *     void testWithAdmin() {
 *         // 设置管理员认证
 *         withAdmin();
 *
 *         // 执行测试
 *         // ...
 *
 *         // 验证当前用户角色
 *         assertHasRole("ADMIN");
 *     }
 *
 *     @Test
 *     void testWithUser() {
 *         // 设置普通用户认证
 *         withUser("testuser", "ROLE_USER");
 *
 *         // 执行测试
 *     }
 * }
 * }</pre>
 *
 * @author Claude
 * @since 1.0.0
 */
public abstract class AbstractSecurityTest {

    /**
     * 默认管理员用户名。
     */
    protected static final String DEFAULT_ADMIN = "admin";

    /**
     * 默认普通用户名。
     */
    protected static final String DEFAULT_USER = "user";

    /**
     * 默认密码。
     */
    protected static final String DEFAULT_PASSWORD = "password";

    /**
     * 每个测试前确保安全上下文为空。
     */
    @BeforeEach
    protected void setUpSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 每个测试后清理安全上下文。
     */
    @AfterEach
    protected void tearDownSecurityContext() {
        AuthenticationTestUtils.clearAuthentication();
    }

    /**
     * 设置默认管理员认证。
     *
     * <p>创建一个用户名为 "admin"，角色为 "ROLE_ADMIN" 的认证。</p>
     *
     * @return 创建的认证对象
     */
    protected Authentication withAdmin() {
        return AuthenticationTestUtils.withUser(DEFAULT_ADMIN)
                .password(DEFAULT_PASSWORD)
                .roles("ADMIN")
                .setupInContext();
    }

    /**
     * 设置管理员认证。
     *
     * @param username 管理员用户名
     * @param roles    管理员角色
     * @return 创建的认证对象
     */
    protected Authentication withAdmin(String username, String... roles) {
        return AuthenticationTestUtils.withUser(username)
                .password(DEFAULT_PASSWORD)
                .roles(roles)
                .setupInContext();
    }

    /**
     * 设置默认普通用户认证。
     *
     * <p>创建一个用户名为 "user"，角色为 "ROLE_USER" 的认证。</p>
     *
     * @return 创建的认证对象
     */
    protected Authentication withUser() {
        return AuthenticationTestUtils.withUser(DEFAULT_USER)
                .password(DEFAULT_PASSWORD)
                .roles("USER")
                .setupInContext();
    }

    /**
     * 设置普通用户认证。
     *
     * @param username 用户名
     * @param roles    用户角色
     * @return 创建的认证对象
     */
    protected Authentication withUser(String username, String... roles) {
        return AuthenticationTestUtils.withUser(username)
                .password(DEFAULT_PASSWORD)
                .roles(roles)
                .setupInContext();
    }

    /**
     * 设置具有特定权限的用户认证。
     *
     * @param username    用户名
     * @param authorities 权限列表
     * @return 创建的认证对象
     */
    protected Authentication withAuthorities(String username, String... authorities) {
        return AuthenticationTestUtils.withUser(username)
                .password(DEFAULT_PASSWORD)
                .authorities(authorities)
                .setupInContext();
    }

    /**
     * 断言当前用户具有指定角色。
     *
     * @param role 角色名
     * @throws AssertionError 如果当前用户不具有该角色
     */
    protected void assertHasRole(String role) {
        if (!AuthenticationTestUtils.hasRole(role)) {
            throw new AssertionError(
                    "Expected user to have role: " + role + ", but they don't");
        }
    }

    /**
     * 断言当前用户不具有指定角色。
     *
     * @param role 角色名
     * @throws AssertionError 如果当前用户具有该角色
     */
    protected void assertDoesNotHaveRole(String role) {
        if (AuthenticationTestUtils.hasRole(role)) {
            throw new AssertionError(
                    "Expected user NOT to have role: " + role + ", but they do");
        }
    }

    /**
     * 断言当前用户具有指定权限。
     *
     * @param authority 权限名
     * @throws AssertionError 如果当前用户不具有该权限
     */
    protected void assertHasAuthority(String authority) {
        if (!AuthenticationTestUtils.hasAuthority(authority)) {
            throw new AssertionError(
                    "Expected user to have authority: " + authority + ", but they don't");
        }
    }

    /**
     * 断言当前用户已认证。
     *
     * @throws AssertionError 如果当前用户未认证
     */
    protected void assertAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AssertionError("Expected user to be authenticated, but they are not");
        }
    }

    /**
     * 断言当前用户未认证。
     *
     * @throws AssertionError 如果当前用户已认证
     */
    protected void assertNotAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            throw new AssertionError("Expected user to NOT be authenticated, but they are: "
                    + authentication.getName());
        }
    }

    /**
     * 获取当前用户名。
     *
     * @return 当前用户名，如果未认证则返回 null
     */
    protected String getCurrentUsername() {
        return AuthenticationTestUtils.getCurrentUsername();
    }
}
