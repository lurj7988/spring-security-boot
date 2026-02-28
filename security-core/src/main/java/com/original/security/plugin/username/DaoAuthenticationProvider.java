package com.original.security.plugin.username;

import com.original.security.core.authentication.AuthenticationResult;
import com.original.security.core.authentication.token.Token;
import com.original.security.core.authentication.user.SecurityUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户名密码认证提供者。
 * <p>
 * 该类实现了双向认证提供者接口，同时支持：
 * <ul>
 *   <li>Spring Security 的 {@link AuthenticationProvider} 接口</li>
 *   <li>框架自定义的 {@link com.original.security.core.authentication.AuthenticationProvider} 接口</li>
 * </ul>
 * 作为两个认证系统的桥接器，将 Spring Security 的认证机制适配到框架的认证模型。
 * </p>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see AuthenticationProvider
 * @see com.original.security.core.authentication.AuthenticationProvider
 */
@Component
public class DaoAuthenticationProvider
        implements AuthenticationProvider, com.original.security.core.authentication.AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(DaoAuthenticationProvider.class);

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造一个新的 DAO 认证提供者。
     *
     * @param userDetailsService 用户详情服务，用于加载用户信息
     * @param passwordEncoder 密码编码器，用于验证密码
     * @throws IllegalArgumentException 如果任一参数为 null
     */
    public DaoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== Spring Security AuthenticationProvider ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        String password = (String) authentication.getCredentials();

        if (password == null) {
            log.warn("认证失败: 没有提供密码");
            throw new BadCredentialsException("用户名或密码错误");
        }

        UserDetails user;
        try {
            user = this.userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            log.warn("用户认证失败: 用户不存在");
            throw new BadCredentialsException("用户名或密码错误");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("账号已被禁用");
        }

        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            log.warn("用户认证失败: 密码不匹配");
            throw new BadCredentialsException("用户名或密码错误");
        }

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                user, authentication.getCredentials(), user.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication != null && UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // ==================== Framework AuthenticationProvider ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationResult authenticate(Object credentials, String authenticationType)
            throws com.original.security.core.authentication.AuthenticationException {
        if (!(credentials instanceof Map)) {
            return AuthenticationResult.failure("Invalid credentials format", "INVALID_CREDENTIALS_FORMAT");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> credentialMap = (Map<String, String>) credentials;
        String username = credentialMap.get("username");
        String password = credentialMap.get("password");

        if (username == null || password == null) {
            return AuthenticationResult.failure("Username and password are required", "MISSING_CREDENTIALS");
        }

        return authenticate(username, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateToken(Token token) {
        // Username-password provider does not support token validation
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token refreshToken(Token token) {
        // Username-password provider does not support token refresh
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationResult authenticate(String username, String password)
            throws com.original.security.core.authentication.AuthenticationException {
        try {
            Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
            Authentication result = this.authenticate(auth);

            UserDetails userDetails = (UserDetails) result.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            SecurityUser securityUser = SecurityUser.builder()
                    .userId(userDetails.getUsername())
                    .username(userDetails.getUsername())
                    .displayName(userDetails.getUsername())
                    .email(null) // Email not available from UserDetails
                    .roles(roles)
                    .status(userDetails.isEnabled() ? SecurityUser.UserStatus.ACTIVE : SecurityUser.UserStatus.DISABLED)
                    .build();

            Map<String, Object> details = new HashMap<>();
            if (result.getDetails() != null) {
                details.put("details", result.getDetails());
            }
            return AuthenticationResult.success(securityUser, details);
        } catch (AuthenticationException ex) {
            return AuthenticationResult.failure(ex.getMessage(), "AUTH_ERROR");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws com.original.security.core.authentication.AuthenticationException {
        try {
            return this.userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new com.original.security.core.authentication.AuthenticationException(e.getMessage(), "USER_NOT_FOUND");
        }
    }
}
