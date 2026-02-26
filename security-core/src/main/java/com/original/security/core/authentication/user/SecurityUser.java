package com.original.security.core.authentication.user;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 安全用户信息
 * <p>
 * 封装了用户的认证信息和权限数据
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SecurityUser {

    /**
     * 用户唯一标识
     */
    private final String userId;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 用户显示名称
     */
    private final String displayName;

    /**
     * 用户邮箱
     */
    private final String email;

    /**
     * 用户角色列表
     */
    private final List<String> roles;

    /**
     * 用户权限列表
     */
    private final List<String> permissions;

    /**
     * 附加属性
     */
    private final Map<String, Object> attributes;

    /**
     * 最后活跃时间
     */
    private final LocalDateTime lastActiveTime;

    /**
     * 用户状态
     */
    private final UserStatus status;

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE,      // 活跃
        INACTIVE,    // 非活跃
        LOCKED,      // 锁定
        DISABLED     // 禁用
    }

    private SecurityUser(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.displayName = builder.displayName;
        this.email = builder.email;
        this.roles = Collections.unmodifiableList(builder.roles);
        this.permissions = Collections.unmodifiableList(builder.permissions);
        this.attributes = Collections.unmodifiableMap(builder.attributes);
        this.lastActiveTime = builder.lastActiveTime;
        this.status = builder.status;
    }

    /**
     * 创建用户构建器
     *
     * @return 用户构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public UserStatus getStatus() {
        return status;
    }

    /**
     * Builder for SecurityUser
     */
    public static class Builder {
        private String userId;
        private String username;
        private String displayName;
        private String email;
        private List<String> roles = Collections.emptyList();
        private List<String> permissions = Collections.emptyList();
        private Map<String, Object> attributes = Collections.emptyMap();
        private LocalDateTime lastActiveTime;
        private UserStatus status = UserStatus.ACTIVE;

        /**
         * 从现有 SecurityUser 复制属性
         */
        public Builder from(SecurityUser user) {
            this.userId = user.userId;
            this.username = user.username;
            this.displayName = user.displayName;
            this.email = user.email;
            this.roles = new ArrayList<>(user.roles);
            this.permissions = new ArrayList<>(user.permissions);
            this.attributes = new HashMap<>(user.attributes);
            this.lastActiveTime = user.lastActiveTime;
            this.status = user.status;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(List<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder lastActiveTime(LocalDateTime lastActiveTime) {
            this.lastActiveTime = lastActiveTime;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public SecurityUser build() {
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("userId cannot be empty");
            }
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("username cannot be empty");
            }
            return new SecurityUser(this);
        }
    }
}