package com.original.security.core.authentication.impl;

import com.original.security.core.authentication.*;
import com.original.security.core.authentication.token.SimpleToken;
import com.original.security.core.authentication.user.SecurityUser;
import com.original.security.config.ConfigProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认认证提供者实现
 * <p>
 * 提供基于用户名密码的基本认证功能
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Service
public class DefaultAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultAuthenticationProvider.class);
    private final PasswordEncoder passwordEncoder;
    private final Map<String, SecurityUser> userStore = new HashMap<>();
    private final Map<String, String> passwordStore = new HashMap<>();
    private final ConfigProvider configProvider;
    private static final String TOKEN_EXPIRATION_HOURS_KEY = "security.token.expiration.hours";
    private static final long DEFAULT_TOKEN_EXPIRATION_HOURS = 1;

    // 构造器依赖注入
    public DefaultAuthenticationProvider(PasswordEncoder passwordEncoder, ConfigProvider configProvider) {
        Assert.notNull(passwordEncoder, "PasswordEncoder cannot be null");
        Assert.notNull(configProvider, "ConfigProvider cannot be null");
        this.passwordEncoder = passwordEncoder;
        this.configProvider = configProvider;

        // 初始化默认用户
        initDefaultUsers();
    }


    @Override
    public AuthenticationResult authenticate(Object credentials, String authenticationType) throws AuthenticationException {
        if (!(credentials instanceof Map)) {
            return AuthenticationResult.failure("Invalid credentials format", "INVALID_CREDENTIALS_FORMAT");
        }

        Map<String, Object> credentialMap = (Map<String, Object>) credentials;
        String username = (String) credentialMap.get("username");
        String password = (String) credentialMap.get("password");

        if (username == null || password == null) {
            return AuthenticationResult.failure("Username and password are required", "MISSING_CREDENTIALS");
        }

        return authenticate(username, password);
    }

    @Override
    public AuthenticationResult authenticate(String username, String password) throws AuthenticationException {
        SecurityUser user = userStore.get(username);

        if (user == null) {
            return AuthenticationResult.failure("User not found: " + username, "USER_NOT_FOUND");
        }

        if (user.getStatus() != SecurityUser.UserStatus.ACTIVE) {
            return AuthenticationResult.failure("User account is inactive", "USER_INACTIVE");
        }

        if (!passwordEncoder.matches(password, getEncodedPassword(username))) {
            return AuthenticationResult.failure("Invalid password", "INVALID_PASSWORD");
        }

        // 更新最后活跃时间
        user = SecurityUser.builder()
            .from(user)
            .lastActiveTime(LocalDateTime.now())
            .build();
        userStore.put(username, user);

        Map<String, Object> details = new HashMap<>();
        details.put("loginTime", LocalDateTime.now());
        details.put("ip", "unknown");

        return AuthenticationResult.success(user, details);
    }

    @Override
    public boolean validateToken(com.original.security.core.authentication.token.Token token) {
        if (token == null) {
            return false;
        }

        return !token.isExpired();
    }

    @Override
    public com.original.security.core.authentication.token.Token refreshToken(com.original.security.core.authentication.token.Token token) {
        if (!validateToken(token)) {
            return null;
        }

        // 创建新 token，这里简化处理
        return createNewToken(token.getSubject());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws AuthenticationException {
        SecurityUser user = userStore.get(username);

        if (user == null) {
            throw new AuthenticationException("User not found: " + username, "USER_NOT_FOUND");
        }

        if (user.getStatus() != SecurityUser.UserStatus.ACTIVE) {
            throw new AuthenticationException("User account is inactive: " + username, "USER_INACTIVE");
        }

        return User.builder()
            .username(username)
            .password(getEncodedPassword(username))
            .authorities(user.getRoles().stream()
                .flatMap(role -> Arrays.stream(role.split(",")))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()))
            .accountExpired(false)
            .accountLocked(user.getStatus() == SecurityUser.UserStatus.LOCKED)
            .credentialsExpired(false)
            .disabled(user.getStatus() == SecurityUser.UserStatus.DISABLED)
            .build();
    }

    /**
     * 获取编码后的密码
     */
    private String getEncodedPassword(String username) {
        // 首先从密码存储中获取
        String encodedPassword = passwordStore.get(username);
        if (encodedPassword != null) {
            return encodedPassword;
        }

        // 从配置中获取密码，使用 ConfigProvider
        String password = configProvider.getConfig("security.password." + username, null);
        if (password == null) {
            // 如果没有配置密码，生成一个随机密码（仅用于演示）
            password = UUID.randomUUID().toString().substring(0, 16);
            log.warn("No password configured for user: {}. Generated temporary password for demo only.", username);
        }
        return passwordEncoder.encode(password);
    }

    /**
     * 创建新 token
     */
    private com.original.security.core.authentication.token.Token createNewToken(String subject) {
        long expirationHours = configProvider.getConfig(TOKEN_EXPIRATION_HOURS_KEY, DEFAULT_TOKEN_EXPIRATION_HOURS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("iat", LocalDateTime.now());
        claims.put("exp", LocalDateTime.now().plusHours(expirationHours));

        return new SimpleToken(
            "jwt.token." + subject,
            "JWT",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "system",
            subject,
            new String[]{"web"},
            claims
        );
    }

    /**
     * 初始化默认用户
     * <p>
     * 注意：默认密码必须通过配置提供者获取，禁止硬编码。
     * 如果配置中没有提供密码，系统将使用随机生成的临时密码（仅用于开发/测试环境）
     *
     * @see #getEncodedPassword(String)
     */
    private void initDefaultUsers() {
        // 管理员用户
        SecurityUser admin = SecurityUser.builder()
            .userId("admin")
            .username("admin")
            .displayName("系统管理员")
            .email("admin@example.com")
            .roles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
            .permissions(Arrays.asList("CREATE", "READ", "UPDATE", "DELETE"))
            .lastActiveTime(LocalDateTime.now())
            .build();
        userStore.put("admin", admin);
        // 管理员密码从配置读取或使用随机临时密码
        initUserPassword("admin");

        // 普通用户
        SecurityUser user = SecurityUser.builder()
            .userId("user001")
            .username("user")
            .displayName("普通用户")
            .email("user@example.com")
            .roles(Collections.singletonList("ROLE_USER"))
            .permissions(Arrays.asList("READ", "UPDATE"))
            .lastActiveTime(LocalDateTime.now())
            .build();
        userStore.put("user", user);
        // 普通用户密码从配置读取或使用随机临时密码
        initUserPassword("user");
    }

    /**
     * 初始化用户密码
     * <p>
     * 从配置中获取用户密码，如果未配置则生成随机临时密码。
     * 随机密码仅用于开发/测试环境，生产环境必须配置密码。
     *
     * @param username 用户名
     */
    private void initUserPassword(String username) {
        String password = configProvider.getConfig("security.password." + username, null);
        if (password == null) {
            // 开发/测试环境：生成随机临时密码
            password = UUID.randomUUID().toString().substring(0, 16);
            log.warn("⚠️  SECURITY WARNING: No password configured for user '{}'. Generated temporary password: '{}' (仅用于演示)",
                     username, password);
            log.warn("请在生产环境中通过配置 'security.password.{}' 设置安全密码", username);
        }
        passwordStore.put(username, passwordEncoder.encode(password));
    }
}