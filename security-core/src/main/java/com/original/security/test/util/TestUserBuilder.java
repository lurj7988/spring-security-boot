package com.original.security.test.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 测试用户构建器。
 *
 * <p>使用构建器模式创建测试用户和认证对象。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建认证对象
 * Authentication auth = AuthenticationTestUtils.withUser("admin")
 *     .password("secret")
 *     .roles("ADMIN")
 *     .authorities("user:read", "user:write")
 *     .buildAuthentication();
 *
 * // 创建 UserDetails
 * UserDetails user = AuthenticationTestUtils.withUser("user")
 *     .roles("USER")
 *     .buildUserDetails();
 *
 * // 设置到安全上下文
 * AuthenticationTestUtils.withUser("admin")
 *     .roles("ADMIN")
 *     .setupInContext();
 * }</pre>
 *
 * @author Claude
 * @since 1.0.0
 */
public class TestUserBuilder {

    private final String username;
    private String password = "password";
    private final List<String> roles = new ArrayList<>();
    private final List<String> authorities = new ArrayList<>();
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    /**
     * 创建构建器。
     *
     * @param username 用户名
     */
    TestUserBuilder(String username) {
        Assert.hasText(username, "Username must not be empty");
        this.username = username;
    }

    /**
     * 设置密码。
     *
     * @param password 密码
     * @return this
     */
    public TestUserBuilder password(String password) {
        this.password = password != null ? password : "password";
        return this;
    }

    /**
     * 添加角色。
     *
     * <p>角色会自动添加 "ROLE_" 前缀。</p>
     *
     * @param roles 角色数组
     * @return this
     */
    public TestUserBuilder roles(String... roles) {
        if (roles != null) {
            for (String role : roles) {
                if (role != null && !role.isEmpty()) {
                    this.roles.add(role);
                }
            }
        }
        return this;
    }

    /**
     * 添加权限。
     *
     * <p>权限不会添加任何前缀。</p>
     *
     * @param authorities 权限数组
     * @return this
     */
    public TestUserBuilder authorities(String... authorities) {
        if (authorities != null) {
            for (String authority : authorities) {
                if (authority != null && !authority.isEmpty()) {
                    this.authorities.add(authority);
                }
            }
        }
        return this;
    }

    /**
     * 设置账户是否启用。
     *
     * @param enabled 是否启用
     * @return this
     */
    public TestUserBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * 设置账户是否未过期。
     *
     * @param accountNonExpired 是否未过期
     * @return this
     */
    public TestUserBuilder accountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
        return this;
    }

    /**
     * 设置账户是否未锁定。
     *
     * @param accountNonLocked 是否未锁定
     * @return this
     */
    public TestUserBuilder accountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
        return this;
    }

    /**
     * 设置凭证是否未过期。
     *
     * @param credentialsNonExpired 是否未过期
     * @return this
     */
    public TestUserBuilder credentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
        return this;
    }

    /**
     * 构建 UserDetails 对象。
     *
     * @return UserDetails 实例
     */
    public UserDetails buildUserDetails() {
        Collection<SimpleGrantedAuthority> grantedAuthorities = buildAuthorities();

        return User.builder()
                .username(username)
                .password(password)
                .disabled(!enabled)
                .accountExpired(!accountNonExpired)
                .accountLocked(!accountNonLocked)
                .credentialsExpired(!credentialsNonExpired)
                .authorities(grantedAuthorities)
                .build();
    }

    /**
     * 构建 Authentication 对象。
     *
     * @return Authentication 实例
     */
    public Authentication buildAuthentication() {
        UserDetails userDetails = buildUserDetails();

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );
    }

    /**
     * 构建并设置到当前安全上下文。
     *
     * <p>创建 Authentication 并设置到 {@link SecurityContextHolder}。</p>
     *
     * @return 创建的认证对象
     */
    public Authentication setupInContext() {
        Authentication authentication = buildAuthentication();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return authentication;
    }

    /**
     * 构建权限集合。
     *
     * @return 权限集合
     */
    private Collection<SimpleGrantedAuthority> buildAuthorities() {
        List<SimpleGrantedAuthority> result = new ArrayList<>();

        // 添加角色（自动添加 ROLE_ 前缀）
        for (String role : roles) {
            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            result.add(new SimpleGrantedAuthority(roleName));
        }

        // 添加权限（不添加前缀）
        for (String authority : authorities) {
            result.add(new SimpleGrantedAuthority(authority));
        }

        // 如果没有配置任何权限，添加默认的 ROLE_USER
        if (result.isEmpty()) {
            result.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return result;
    }
}
