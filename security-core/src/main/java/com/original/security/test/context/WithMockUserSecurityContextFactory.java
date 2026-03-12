package com.original.security.test.context;

import com.original.security.test.annotation.WithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 增强版 Mock 用户安全上下文工厂。
 *
 * <p>为 {@link WithMockUser} 注解创建 {@link SecurityContext}。</p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>支持同时配置 roles 和 authorities</li>
 *   <li>支持自定义 UserDetails 实现</li>
 *   <li>支持设置用户账户状态属性</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 * @see WithMockUser
 */
public class WithMockUserSecurityContextFactory implements WithSecurityContextFactory<WithMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockUser withUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserDetails userDetails = createUserDetails(withUser);
        Authentication authentication = createAuthentication(userDetails);
        context.setAuthentication(authentication);

        return context;
    }

    /**
     * 根据注解配置创建 UserDetails。
     *
     * @param withUser 注解配置
     * @return UserDetails 实例
     */
    private UserDetails createUserDetails(WithMockUser withUser) {
        // 如果指定了自定义 UserDetails 类，尝试使用它
        if (StringUtils.hasText(withUser.userDetailsClass())) {
            return createCustomUserDetails(withUser);
        }

        // 创建权限列表
        Collection<SimpleGrantedAuthority> authorities = createAuthorities(withUser);

        // 使用 Spring Security 的 User builder 创建 UserDetails
        return User.builder()
                .username(withUser.username())
                .password(withUser.password())
                .disabled(!withUser.enabled())
                .accountExpired(!withUser.accountNonExpired())
                .accountLocked(!withUser.accountNonLocked())
                .credentialsExpired(!withUser.credentialsNonExpired())
                .authorities(authorities)
                .build();
    }

    /**
     * 创建自定义 UserDetails 实例。
     *
     * @param withUser 注解配置
     * @return 自定义 UserDetails 实例
     * @throws IllegalArgumentException 如果类不存在、不实现 UserDetails 或无法实例化
     */
    private UserDetails createCustomUserDetails(WithMockUser withUser) {
        String className = withUser.userDetailsClass();
        Class<?> clazz;

        // 加载类
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Custom UserDetails class not found: " + className, e);
        }

        // 验证实现 UserDetails 接口
        if (!UserDetails.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(
                    "Class " + className + " does not implement UserDetails interface");
        }

        Collection<SimpleGrantedAuthority> authorities = createAuthorities(withUser);

        // 尝试使用全参数构造函数
        try {
            return (UserDetails) clazz.getConstructor(
                    String.class, String.class,
                    boolean.class, boolean.class, boolean.class, boolean.class,
                    Collection.class)
                    .newInstance(withUser.username(), withUser.password(),
                            withUser.enabled(), withUser.accountNonExpired(),
                            withUser.credentialsNonExpired(), withUser.accountNonLocked(),
                            authorities);
        } catch (NoSuchMethodException e) {
            // 继续尝试其他构造函数
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to invoke full constructor on " + className, e);
        }

        // 尝试使用简单参数构造函数
        try {
            return (UserDetails) clazz.getConstructor(String.class, String.class, Collection.class)
                    .newInstance(withUser.username(), withUser.password(), authorities);
        } catch (NoSuchMethodException e) {
            // 继续尝试无参构造函数
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to invoke simple constructor on " + className, e);
        }

        // 回退到无参构造函数，并尝试通过反射设置字段
        try {
            UserDetails instance = (UserDetails) clazz.getDeclaredConstructor().newInstance();
            org.springframework.test.util.ReflectionTestUtils.setField(instance, "username", withUser.username());
            org.springframework.test.util.ReflectionTestUtils.setField(instance, "password", withUser.password());
            org.springframework.test.util.ReflectionTestUtils.setField(instance, "authorities", authorities);
            return instance;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to create custom UserDetails instance: " + className
                            + ". Supported constructors: (String, String, Collection) or "
                            + "(String, String, boolean, boolean, boolean, boolean, Collection) or no-arg with settable fields",
                    e);
        }
    }

    /**
     * 根据注解配置创建权限列表。
     *
     * <p>同时支持 roles（自动添加 ROLE_ 前缀）和 authorities（不添加前缀）。</p>
     *
     * @param withUser 注解配置
     * @return 权限集合
     */
    private Collection<SimpleGrantedAuthority> createAuthorities(WithMockUser withUser) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 添加角色（自动添加 ROLE_ 前缀）
        for (String role : withUser.roles()) {
            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        // 添加权限（不添加前缀）
        for (String authority : withUser.authorities()) {
            authorities.add(new SimpleGrantedAuthority(authority));
        }

        // 如果没有配置任何权限，添加默认的 ROLE_USER
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    /**
     * 创建 Authentication 对象。
     *
     * @param userDetails 用户详情
     * @return Authentication 实例
     */
    private Authentication createAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );
    }
}
