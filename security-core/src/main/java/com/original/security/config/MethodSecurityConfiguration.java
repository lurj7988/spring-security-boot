package com.original.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * 方法级安全配置类。
 * <p>
 * 启用 Spring Security 的方法级安全注解支持，包括：
 * <ul>
 *     <li>{@code @PreAuthorize} - 方法调用前授权检查</li>
 *     <li>{@code @PostAuthorize} - 方法调用后授权检查</li>
 *     <li>{@code @PreFilter} - 方法调用前过滤参数</li>
 *     <li>{@code @PostFilter} - 方法调用后过滤返回值</li>
 *     <li>{@code @Secured} - 基于角色的授权检查</li>
 *     <li>{@code @RolesAllowed} - JSR-250 标准角色检查</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * &#064;RestController
 * &#064;RequestMapping("/api/users")
 * public class UserController {
 *
 *     &#064;GetMapping
 *     &#064;PreAuthorize("hasRole('ADMIN')")
 *     public Response&lt;List&lt;User&gt;&gt; listUsers() {
 *         // 只有 ADMIN 角色可访问
 *     }
 *
 *     &#064;PostMapping
 *     &#064;PreAuthorize("hasAuthority('user:write')")
 *     public Response&lt;User&gt; createUser(&#064;RequestBody UserRequest request) {
 *         // 需要 user:write 权限
 *     }
 * }
 * </pre>
 *
 * <h3>支持的 SpEL 表达式：</h3>
 * <ul>
 *     <li>{@code hasRole('XXX')} - 检查用户是否有 ROLE_XXX 角色</li>
 *     <li>{@code hasAnyRole('X', 'Y')} - 检查用户是否有任意一个角色</li>
 *     <li>{@code hasAuthority('xxx')} - 检查用户是否有 xxx 权限</li>
 *     <li>{@code hasAnyAuthority('x', 'y')} - 检查用户是否有任意一个权限</li>
 *     <li>{@code isAuthenticated()} - 是否已认证</li>
 *     <li>{@code isAnonymous()} - 是否匿名用户</li>
 *     <li>{@code permitAll} / {@code denyAll} - 允许/拒绝所有访问</li>
 * </ul>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
 */
@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true,   // 启用 @PreAuthorize, @PostAuthorize, @PreFilter, @PostFilter
    securedEnabled = true,   // 启用 @Secured 注解
    jsr250Enabled = true     // 启用 @RolesAllowed, @PermitAll, @DenyAll 注解
)
public class MethodSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MethodSecurityConfiguration.class);

    public MethodSecurityConfiguration() {
        log.info("Method security configuration: Enabled @PreAuthorize, @PostAuthorize, @PreFilter, @PostFilter, @Secured, @RolesAllowed annotations");
    }
}
