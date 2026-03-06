package com.original.security.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户模块配置属性
 *
 * <p>配置示例 (application.properties):
 * <pre>
 * security.user.default-role.name=USER
 * security.user.default-role.first-user-role=ADMIN
 * </pre>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "security.user")
public class UserProperties {

    /**
     * 默认角色配置
     */
    private final DefaultRole defaultRole = new DefaultRole();

    /**
     * 获取默认角色配置
     *
     * @return 默认角色配置
     */
    public DefaultRole getDefaultRole() {
        return defaultRole;
    }

    /**
     * 默认角色配置
     *
     * <p>用于配置新用户的默认角色分配规则
     *
     * @author Original Security Team
     * @since 1.0.0
     */
    public static class DefaultRole {

        /**
         * 普通用户的默认角色名称
         *
         * <p>默认值: "USER"
         *
         * @return 默认角色名称
         */
        private String name = "USER";

        /**
         * 设置默认角色名称
         *
         * @param name 角色名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取第一个注册用户的角色名称
         *
         * <p>默认值: "ADMIN"
         * <p>第一个注册的用户将获得此角色
         *
         * @return 首用户角色名称
         */
        private String firstUserRole = "ADMIN";

        /**
         * 设置首用户角色名称
         *
         * @param firstUserRole 角色名称
         */
        public void setFirstUserRole(String firstUserRole) {
            this.firstUserRole = firstUserRole;
        }
    }
}
