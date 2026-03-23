package com.example.security.plugin.sms;

import com.original.security.core.authentication.AuthenticationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 短信认证提供者单元测试。
 *
 * @author Example Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SmsAuthenticationProviderTest {

    @Mock
    private SmsVerifyCodeService smsVerifyCodeService;

    @Mock
    private UserDetailsService userDetailsService;

    private SmsAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SmsAuthenticationProvider(smsVerifyCodeService, userDetailsService);
    }

    // --- Spring Security AuthenticationProvider tests ---

    @Test
    @DisplayName("测试支持 SmsAuthenticationToken 类型")
    void testSupports_ValidToken_ReturnsTrue() {
        assertTrue(provider.supports(SmsAuthenticationToken.class));
    }

    @Test
    @DisplayName("测试不支持其他 Token 类型")
    void testSupports_InvalidToken_ReturnsFalse() {
        assertFalse(provider.supports(
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("测试 supports 传入 null 返回 false")
    void testSupports_Null_ReturnsFalse() {
        assertFalse(provider.supports(null));
    }

    @Test
    @DisplayName("测试认证成功")
    void testAuthenticate_ValidCode_ReturnsAuthenticatedToken() {
        // Given
        String phone = "13800138000";
        String code = "123456";
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);

        UserDetails user = new User(phone, "", Collections.emptyList());

        when(smsVerifyCodeService.verifyCode(phone, code)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(phone)).thenReturn(user);

        // When
        Authentication result = provider.authenticate(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(user, result.getPrincipal());
        verify(smsVerifyCodeService).verifyCode(phone, code);
        verify(userDetailsService).loadUserByUsername(phone);
    }

    @Test
    @DisplayName("测试验证码错误抛出异常")
    void testAuthenticate_InvalidCode_ThrowsException() {
        // Given
        String phone = "13800138000";
        String code = "wrong";
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);

        when(smsVerifyCodeService.verifyCode(phone, code)).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(token));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("测试用户禁用抛出异常")
    void testAuthenticate_DisabledUser_ThrowsException() {
        // Given
        String phone = "13800138000";
        String code = "123456";
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);

        UserDetails disabledUser = new User(phone, "", true,
            true, true, false, Collections.emptyList());

        when(smsVerifyCodeService.verifyCode(phone, code)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(phone)).thenReturn(disabledUser);

        // When & Then
        assertThrows(DisabledException.class, () -> provider.authenticate(token));
    }

    // --- Framework AuthenticationProvider tests ---

    @Test
    @DisplayName("测试框架接口认证成功")
    void testFrameworkAuthenticate_ValidCredentials_ReturnsSuccess() throws com.original.security.core.authentication.AuthenticationException {
        // Given
        String phone = "13800138000";
        String code = "123456";
        Map<String, String> credentials = new HashMap<>();
        credentials.put("phone", phone);
        credentials.put("code", code);

        UserDetails user = new User(phone, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(smsVerifyCodeService.verifyCode(phone, code)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(phone)).thenReturn(user);

        // When
        AuthenticationResult result = provider.authenticate(credentials, "sms");

        // Then
        assertTrue(result.isSuccess());
        assertEquals(phone, result.getUser().getUsername());
        assertTrue(result.getUser().getRoles().contains("ROLE_USER"));
    }

    @Test
    @DisplayName("测试框架接口认证失败")
    void testFrameworkAuthenticate_InvalidCode_ReturnsFailure() throws com.original.security.core.authentication.AuthenticationException {
        // Given
        String phone = "13800138000";
        String code = "wrong";
        Map<String, String> credentials = new HashMap<>();
        credentials.put("phone", phone);
        credentials.put("code", code);

        when(smsVerifyCodeService.verifyCode(phone, code)).thenReturn(false);

        // When
        AuthenticationResult result = provider.authenticate(credentials, "sms");

        // Then
        assertFalse(result.isSuccess());
        assertEquals("AUTH_ERROR", result.getErrorCode());
    }

    @Test
    @DisplayName("测试构造函数验证码服务为 null 抛出异常")
    void testConstructor_NullSmsService_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new SmsAuthenticationProvider(null, userDetailsService));
    }

    @Test
    @DisplayName("测试构造函数用户服务为 null 抛出异常")
    void testConstructor_NullUserService_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new SmsAuthenticationProvider(smsVerifyCodeService, null));
    }
}
