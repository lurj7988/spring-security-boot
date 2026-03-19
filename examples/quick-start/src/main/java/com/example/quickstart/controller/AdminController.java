package com.example.quickstart.controller;

import com.example.quickstart.entity.UserInfo;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 管理员控制器示例。
 * <p>
 * 展示如何使用 {@link PreAuthorize} 注解进行角色级别的访问控制。
 * 所有端点都需要 ADMIN 角色才能访问。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    /** 示例用户 ID。 */
    private static final Long EXAMPLE_USER_ID_1 = 1L;
    private static final Long EXAMPLE_USER_ID_2 = 2L;
    private static final Long EXAMPLE_USER_ID_3 = 3L;

    /** 示例邮箱后缀。 */
    private static final String EXAMPLE_EMAIL_DOMAIN = "@example.com";
    private static final String EXAMPLE_PHONE = "13800138000";

    /**
     * 获取所有用户列表。
     * <p>
     * 只有拥有 ADMIN 角色的用户才能访问此端点。
     * 示例中使用模拟数据，实际项目应从数据库获取。
     * </p>
     *
     * @return 用户列表响应
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<List<UserInfo>> getAllUsers() {
        log.info("Getting all users - ADMIN access required");

        List<UserInfo> userInfos = new ArrayList<>();

        // 示例数据 - 实际应从数据库获取
        userInfos.add(new UserInfo(
                EXAMPLE_USER_ID_1,
                "admin",
                "admin" + EXAMPLE_EMAIL_DOMAIN,
                EXAMPLE_PHONE,
                "active",
                Arrays.asList("ROLE_ADMIN")
        ));
        userInfos.add(new UserInfo(
                EXAMPLE_USER_ID_2,
                "user",
                "user" + EXAMPLE_EMAIL_DOMAIN,
                "13800138001",
                "active",
                Arrays.asList("ROLE_USER")
        ));
        userInfos.add(new UserInfo(
                EXAMPLE_USER_ID_3,
                "test",
                "test" + EXAMPLE_EMAIL_DOMAIN,
                "13800138002",
                "active",
                Arrays.asList("ROLE_USER")
        ));

        return Response.successBuilder(userInfos).build();
    }

    /**
     * 管理员欢迎信息。
     * <p>
     * 只有拥有 ADMIN 角色的用户才能访问此端点。
     * </p>
     *
     * @return 欢迎信息
     */
    @GetMapping("/welcome")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<String> adminWelcome() {
        log.info("Admin welcome endpoint accessed");

        return Response.successBuilder("Welcome, Administrator!")
                .msg("You have access to admin-only features")
                .build();
    }

    /**
     * 获取用户统计信息。
     * <p>
     * 只有拥有 ADMIN 角色的用户才能访问此端点。
     * 示例中使用模拟数据，实际项目应从数据库获取。
     * </p>
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Response<AdminStats> getStats() {
        log.info("Getting admin statistics");

        // 示例数据 - 实际应从数据库获取
        AdminStats stats = new AdminStats(3L, 3L, 0L);
        return Response.successBuilder(stats).build();
    }

    /**
     * 管理员统计数据 DTO。
     * <p>
     * 包含用户总数、活跃用户数和非活跃用户数。
     * </p>
     */
    public static class AdminStats {

        /** 用户总数。 */
        private long totalUsers;

        /** 活跃用户数。 */
        private long activeUsers;

        /** 非活跃用户数。 */
        private long inactiveUsers;

        /**
         * 全参构造函数。
         *
         * @param totalUsers 用户总数
         * @param activeUsers 活跃用户数
         * @param inactiveUsers 非活跃用户数
         */
        public AdminStats(long totalUsers, long activeUsers, long inactiveUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public long getActiveUsers() {
            return activeUsers;
        }

        public void setActiveUsers(long activeUsers) {
            this.activeUsers = activeUsers;
        }

        public long getInactiveUsers() {
            return inactiveUsers;
        }

        public void setInactiveUsers(long inactiveUsers) {
            this.inactiveUsers = inactiveUsers;
        }
    }
}