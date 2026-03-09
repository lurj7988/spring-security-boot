package com.original.security.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户模块配置属性
 *
 * <p>配置示例 (application.properties):
 * <pre>
 * security.user.default-role.name=USER
 * security.user.default-role.first-user-role=ADMIN
 * security.user.password.length=12
 * security.user.password.min-length=8
 * security.user.password.max-length=50
 * security.user.password.include-uppercase=true
 * security.user.password.include-lowercase=true
 * security.user.password.include-numbers=true
 * security.user.password.include-special-chars=true
 * security.user.password.special-characters=!@#$%^&*()_+-=[]{}|;:,.<>?
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
     * 密码配置
     */
    private final Password password = new Password();

    /**
     * 获取默认角色配置
     *
     * @return 默认角色配置
     */
    public DefaultRole getDefaultRole() {
        return defaultRole;
    }

    /**
     * 获取密码配置
     *
     * @return 密码配置
     */
    public Password getPassword() {
        return password;
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
         */
        private String name = "USER";

        /**
         * 第一个注册用户的角色名称
         *
         * <p>默认值: "ADMIN"
         * <p>第一个注册的用户将获得此角色
         */
        private String firstUserRole = "ADMIN";

        /**
         * 获取默认角色名称
         *
         * @return 默认角色名称
         */
        public String getName() {
            return name;
        }

        /**
         * 设置默认角色名称
         *
         * @param name 角色名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取首用户角色名称
         *
         * @return 首用户角色名称
         */
        public String getFirstUserRole() {
            return firstUserRole;
        }

        /**
         * 设置首用户角色名称
         *
         * @param firstUserRole 角色名称
         */
        public void setFirstUserRole(String firstUserRole) {
            this.firstUserRole = firstUserRole;
        }
    }

    /**
     * 密码配置
     *
     * <p>用于配置密码生成规则
     *
     * @author Original Security Team
     * @since 1.0.0
     */
    public static class Password {
        /**
         * 默认密码长度
         */
        private int length = 12;

        /**
         * 最小密码长度
         */
        private int minLength = 8;

        /**
         * 最大密码长度
         */
        private int maxLength = 50;

        /**
         * 是否包含大写字母
         */
        private boolean includeUppercase = true;

        /**
         * 是否包含小写字母
         */
        private boolean includeLowercase = true;

        /**
         * 是否包含数字
         */
        private boolean includeNumbers = true;

        /**
         * 是否包含特殊字符
         */
        private boolean includeSpecialChars = true;

        /**
         * 特殊字符集合
         * 注意：必须与密码复杂度正则表达式中的特殊字符保持一致
         */
        private String specialCharacters = "@#$%^&+=!";

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            if (length < minLength || length > maxLength) {
                throw new IllegalArgumentException(
                    String.format("密码长度必须在 %d 到 %d 之间", minLength, maxLength));
            }
            this.length = length;
        }

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            if (minLength < 1) {
                throw new IllegalArgumentException("最小密码长度必须大于0");
            }
            if (maxLength < minLength) {
                throw new IllegalArgumentException("最小密码长度不能大于最大密码长度");
            }
            this.minLength = minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            if (maxLength < minLength) {
                throw new IllegalArgumentException("最大密码长度不能小于最小密码长度");
            }
            this.maxLength = maxLength;
        }

        public boolean isIncludeUppercase() {
            return includeUppercase;
        }

        public void setIncludeUppercase(boolean includeUppercase) {
            this.includeUppercase = includeUppercase;
        }

        public boolean isIncludeLowercase() {
            return includeLowercase;
        }

        public void setIncludeLowercase(boolean includeLowercase) {
            this.includeLowercase = includeLowercase;
        }

        public boolean isIncludeNumbers() {
            return includeNumbers;
        }

        public void setIncludeNumbers(boolean includeNumbers) {
            this.includeNumbers = includeNumbers;
        }

        public boolean isIncludeSpecialChars() {
            return includeSpecialChars;
        }

        public void setIncludeSpecialChars(boolean includeSpecialChars) {
            this.includeSpecialChars = includeSpecialChars;
        }

        public String getSpecialCharacters() {
            return specialCharacters;
        }

        public void setSpecialCharacters(String specialCharacters) {
            if (specialCharacters == null || specialCharacters.trim().isEmpty()) {
                throw new IllegalArgumentException("特殊字符集合不能为空");
            }
            this.specialCharacters = specialCharacters;
        }
    }
}
