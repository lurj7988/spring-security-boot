package com.original.security.test.annotation;

import com.original.security.test.context.WithMockUserSecurityContextFactory;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 增强版 Mock 用户注解。
 *
 * <p>基于 Spring Security 的 {@link org.springframework.security.test.context.support.WithMockUser}
 * 扩展，提供更灵活的用户配置能力。</p>
 *
 * <h3>增强功能</h3>
 * <ul>
 *   <li>支持同时配置 roles 和 authorities</li>
 *   <li>支持自定义 UserDetails 实现</li>
 *   <li>支持设置用户额外属性</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @WithMockUser(username = "admin", roles = {"ADMIN"})
 * public void testAdminAccess() {
 *     // 测试管理员权限
 * }
 *
 * @WithMockUser(username = "user", authorities = {"user:read", "user:write"})
 * public void testUserWithSpecificAuthorities() {
 *     // 测试特定权限
 * }
 * }</pre>
 *
 * @author Claude
 * @since 1.0.0
 * @see org.springframework.security.test.context.support.WithMockUser
 * @see WithMockUserSecurityContextFactory
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithMockUserSecurityContextFactory.class)
public @interface WithMockUser {

    /**
     * 用户名，默认为 "user"。
     *
     * @return 用户名
     */
    String username() default "user";

    /**
     * 密码，默认为 "password"。
     *
     * @return 密码
     */
    String password() default "password";

    /**
     * 角色列表。
     *
     * <p>角色会自动添加 "ROLE_" 前缀。</p>
     *
     * @return 角色数组
     */
    String[] roles() default {};

    /**
     * 权限列表。
     *
     * <p>权限不会添加任何前缀。</p>
     *
     * @return 权限数组
     */
    String[] authorities() default {};

    /**
     * 账户是否启用，默认为 true。
     *
     * @return 是否启用
     */
    boolean enabled() default true;

    /**
     * 账户是否未过期，默认为 true。
     *
     * @return 是否未过期
     */
    boolean accountNonExpired() default true;

    /**
     * 账户是否未锁定，默认为 true。
     *
     * @return 是否未锁定
     */
    boolean accountNonLocked() default true;

    /**
     * 凭证是否未过期，默认为 true。
     *
     * @return 是否未过期
     */
    boolean credentialsNonExpired() default true;

    /**
     * 自定义 UserDetails 实现类的全限定名。
     *
     * <p>如果指定，将使用该类创建用户详情，忽略其他用户属性配置。</p>
     *
     * @return UserDetails 实现类名
     */
    String userDetailsClass() default "";

    /**
     * 安全上下文的设置时机。
     *
     * @return 测试执行事件
     */
    @AliasFor(annotation = WithSecurityContext.class)
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;
}
