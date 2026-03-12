package com.original.security.test.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 认证测试工具类。
 *
 * <p>提供便捷的方法来创建和设置测试用的认证对象。</p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>{@link #mockAuthentication(String, String...)} - 快速创建 Mock 认证</li>
 *   <li>{@link #withUser(String)} - 使用构建器模式创建用户</li>
 *   <li>{@link #mockJwtToken(String, String...)} - 创建 JWT Token Mock</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 方式1：快速 Mock
 * Authentication auth = AuthenticationTestUtils.mockAuthentication("admin", "ROLE_ADMIN");
 *
 * // 方式2：构建器模式
 * Authentication auth = AuthenticationTestUtils.withUser("admin")
 *     .password("secret")
 *     .roles("ADMIN", "USER")
 *     .authorities("user:read", "user:write")
 *     .buildAuthentication();
 *
 * // 方式3：JWT Token Mock
 * String token = AuthenticationTestUtils.mockJwtToken("admin", "ROLE_ADMIN");
 * }</pre>
 *
 * @author Claude
 * @since 1.0.0
 */
public final class AuthenticationTestUtils {

    private AuthenticationTestUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 快速创建 Mock 认证对象。
     *
     * <p>创建一个简单的 {@link UsernamePasswordAuthenticationToken}，
     * 并设置到当前安全上下文中。</p>
     *
     * @param username 用户名
     * @param roles    角色列表（自动添加 ROLE_ 前缀）
     * @return 创建的认证对象
     * @throws IllegalArgumentException 如果用户名为空
     */
    public static Authentication mockAuthentication(String username, String... roles) {
        Assert.hasText(username, "Username must not be empty");

        Collection<SimpleGrantedAuthority> authorities = createAuthoritiesFromRoles(roles);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    /**
     * 创建用户构建器。
     *
     * <p>使用构建器模式创建具有更多自定义选项的认证对象。</p>
     *
     * @param username 用户名
     * @return 用户构建器
     * @throws IllegalArgumentException 如果用户名为空
     */
    public static TestUserBuilder withUser(String username) {
        Assert.hasText(username, "Username must not be empty");
        return new TestUserBuilder(username);
    }

    /**
     * 测试用的默认 JWT Secret（Base64 编码，至少 32 字节）。
     * 与 @SecurityTest 注入的 mock 环境变量一致。
     */
    public static final String MOCK_JWT_SECRET = "ZGVmYXVsdE1vY2tTZWNyZXRGb3JKd3RUZXN0aW5nUHVycG9zZXMxMjM0NTY3ODkw";

    /**
     * 创建 Mock JWT Token。
     *
     * <p>创建一个测试用的 JWT Token 字符串。
     * 自动使用测试用密钥进行签名，配合 @SecurityTest 时可通过校验。</p>
     *
     * @param username 用户名
     * @param roles    角色列表
     * @return Mock JWT Token 字符串
     * @throws IllegalArgumentException 如果用户名为空
     */
    public static String mockJwtToken(String username, String... roles) {
        Assert.hasText(username, "Username must not be empty");

        try {
            byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(MOCK_JWT_SECRET);
            java.security.Key key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
            
            Collection<SimpleGrantedAuthority> authorities = createAuthoritiesFromRoles(roles);
            String authoritiesString = authorities.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

            return io.jsonwebtoken.Jwts.builder()
                    .setSubject(username)
                    .claim("authorities", authoritiesString)
                    .setIssuedAt(new java.util.Date())
                    .setExpiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate mock JWT token", e);
        }
    }

    /**
     * 清除当前安全上下文。
     *
     * <p>在测试的 @After 或 @AfterEach 方法中调用，确保测试之间隔离。</p>
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 获取当前认证用户名。
     *
     * @return 当前用户名，如果未认证则返回 null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 检查当前用户是否具有指定角色。
     *
     * @param role 角色名（可以不带 ROLE_ 前缀）
     * @return 如果具有该角色则返回 true
     */
    public static boolean hasRole(String role) {
        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleName));
    }

    /**
     * 检查当前用户是否具有指定权限。
     *
     * @param authority 权限名
     * @return 如果具有该权限则返回 true
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }

    /**
     * 从角色数组创建权限集合。
     *
     * @param roles 角色数组
     * @return 权限集合
     */
    private static Collection<SimpleGrantedAuthority> createAuthoritiesFromRoles(String... roles) {
        if (roles == null || roles.length == 0) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return Arrays.stream(roles)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
