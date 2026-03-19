package com.example.quickstart.controller;

import com.example.quickstart.entity.UserInfo;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器示例。
 * <p>
 * 展示如何获取当前登录用户的信息。
 * 所有端点都需要认证才能访问。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /** 示例用户 ID。 */
    private static final Long EXAMPLE_USER_ID = 1L;

    /** 示例邮箱后缀。 */
    private static final String EXAMPLE_EMAIL_SUFFIX = "@example.com";

    /** 示例手机号。 */
    private static final String EXAMPLE_PHONE = "13800138000";

    /** 示例用户状态。 */
    private static final String EXAMPLE_STATUS = "active";

    /** 错误消息：未授权。 */
    private static final String MSG_UNAUTHORIZED = "Authentication required";

    /**
     * 获取当前登录用户信息。
     * <p>
     * 使用 {@link AuthenticationPrincipal} 注解自动注入当前用户详情。
     * 示例中使用简化的用户信息，实际项目可从数据库获取。
     * </p>
     *
     * @param userDetails 当前登录用户详情
     * @return 用户信息响应
     */
    @GetMapping("/me")
    public Response<UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        // 空值检查
        if (userDetails == null) {
            log.warn("Unauthorized access attempt to /api/users/me");
            return Response.<UserInfo>errorBuilder().msg(MSG_UNAUTHORIZED).build();
        }

        log.info("Getting current user info: {}", userDetails.getUsername());

        // 从 UserDetails 提取角色信息
        List<String> roles = userDetails.getAuthorities() != null
                ? userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        // 使用示例数据 - 实际项目应从数据库获取
        UserInfo userInfo = new UserInfo(
                EXAMPLE_USER_ID,
                userDetails.getUsername(),
                userDetails.getUsername() + EXAMPLE_EMAIL_SUFFIX,
                EXAMPLE_PHONE,
                EXAMPLE_STATUS,
                roles
        );

        return Response.successBuilder(userInfo).build();
    }

    /**
     * 获取当前用户的邮箱信息。
     * <p>
     * 演示如何返回简单的用户数据。
     * </p>
     *
     * @param userDetails 当前登录用户详情
     * @return 邮箱信息
     */
    @GetMapping("/email")
    public Response<String> getUserEmail(@AuthenticationPrincipal UserDetails userDetails) {
        // 空值检查
        if (userDetails == null) {
            log.warn("Unauthorized access attempt to /api/users/email");
            return Response.<String>errorBuilder().msg(MSG_UNAUTHORIZED).build();
        }

        log.info("Getting email for user: {}", userDetails.getUsername());

        // 示例返回拼接邮箱 - 实际项目应从数据库获取
        return Response.successBuilder(userDetails.getUsername() + EXAMPLE_EMAIL_SUFFIX).build();
    }
}