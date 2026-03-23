package com.example.security.plugin.sms;

import com.original.security.core.authentication.AuthenticationResult;
import com.original.security.core.authentication.token.Token;
import com.original.security.core.authentication.user.SecurityUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短信验证码认证提供者。
 * <p>
 * 负责验证短信验证码并加载用户信息。该类实现了双向认证提供者接口，同时支持：
 * <ul>
 *   <li>Spring Security 的 {@link AuthenticationProvider} 接口</li>
 *   <li>框架自定义的 {@link com.original.security.core.authentication.AuthenticationProvider} 接口</li>
 * </ul>
 * </p>
 *
 * <p><b>Import 规范说明：</b></p>
 * <p>
 * 本类同时实现两个同名接口（Spring Security 的 AuthenticationProvider 和框架自定义的 AuthenticationProvider）。
 * 按照 Import 规范，我们导入 Spring Security 的 AuthenticationProvider（主要接口），
 * 在类声明中仅对框架自定义接口使用全包名。
 * </p>
 *
 * @author Example Team
 * @since 1.0.0
 * @see SmsAuthenticationToken
 * @see SmsVerifyCodeService
 * @see SmsAuthenticationPlugin
 */
public class SmsAuthenticationProvider implements AuthenticationProvider, com.original.security.core.authentication.AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(SmsAuthenticationProvider.class);

    private final SmsVerifyCodeService smsVerifyCodeService;
    private final UserDetailsService userDetailsService;

    /**
     * 构造认证提供者。
     *
     * @param smsVerifyCodeService 验证码服务，不能为 null
     * @param userDetailsService   用户详情服务，不能为 null
     */
    public SmsAuthenticationProvider(SmsVerifyCodeService smsVerifyCodeService,
                                     UserDetailsService userDetailsService) {
        if (smsVerifyCodeService == null) {
            throw new IllegalArgumentException("smsVerifyCodeService cannot be null");
        }
        if (userDetailsService == null) {
            throw new IllegalArgumentException("userDetailsService cannot be null");
        }
        this.smsVerifyCodeService = smsVerifyCodeService;
        this.userDetailsService = userDetailsService;
    }

    // ==================== Spring Security AuthenticationProvider ====================

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
        String phoneNumber = token.getPhoneNumber();
        String verifyCode = token.getVerifyCode();

        log.debug("Authenticating SMS login for phone: {}", maskPhone(phoneNumber));

        // 1. 验证短信验证码
        if (!smsVerifyCodeService.verifyCode(phoneNumber, verifyCode)) {
            log.warn("SMS verification failed for phone: {}", maskPhone(phoneNumber));
            throw new BadCredentialsException("验证码错误或已过期");
        }

        // 2. 加载用户信息
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        } catch (UsernameNotFoundException e) {
            log.warn("User not found for phone: {}", maskPhone(phoneNumber));
            throw new BadCredentialsException("用户不存在");
        }

        // 3. 检查用户状态
        if (!userDetails.isEnabled()) {
            log.warn("User account is disabled: {}", maskPhone(phoneNumber));
            throw new DisabledException("账户已禁用");
        }

        // 4. 返回已认证的 Token
        SmsAuthenticationToken authenticated = new SmsAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        authenticated.setDetails(authentication.getDetails());

        log.info("SMS authentication successful for phone: {}", maskPhone(phoneNumber));
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication != null
            && SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // ==================== Framework AuthenticationProvider ====================

    @Override
    public AuthenticationResult authenticate(Object credentials, String authenticationType)
            throws com.original.security.core.authentication.AuthenticationException {
        if (!(credentials instanceof Map)) {
            return AuthenticationResult.failure("Invalid credentials format", "INVALID_CREDENTIALS_FORMAT");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> credentialMap = (Map<String, String>) credentials;
        String phone = credentialMap.get("phone");
        String code = credentialMap.get("code");

        if (phone == null || code == null) {
            return AuthenticationResult.failure("Phone and code are required", "MISSING_CREDENTIALS");
        }

        return authenticate(phone, code);
    }

    @Override
    public boolean validateToken(Token token) {
        // SMS provider does not support token validation by default
        return false;
    }

    @Override
    public Token refreshToken(Token token) {
        // SMS provider does not support token refresh
        return null;
    }

    @Override
    public AuthenticationResult authenticate(String phone, String code)
            throws com.original.security.core.authentication.AuthenticationException {
        try {
            Authentication auth = new SmsAuthenticationToken(phone, code);
            Authentication result = this.authenticate(auth);

            UserDetails userDetails = (UserDetails) result.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            SecurityUser securityUser = SecurityUser.builder()
                    .userId(userDetails.getUsername())
                    .username(userDetails.getUsername())
                    .roles(roles)
                    .status(userDetails.isEnabled() ? SecurityUser.UserStatus.ACTIVE : SecurityUser.UserStatus.DISABLED)
                    .build();

            return AuthenticationResult.success(securityUser, new HashMap<>());
        } catch (AuthenticationException ex) {
            return AuthenticationResult.failure(ex.getMessage(), "AUTH_ERROR");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws com.original.security.core.authentication.AuthenticationException {
        try {
            return this.userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new com.original.security.core.authentication.AuthenticationException(e.getMessage(), "USER_NOT_FOUND");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
