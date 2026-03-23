package com.example.security.plugin.sms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 短信认证令牌单元测试。
 *
 * @author Example Team
 * @since 1.0.0
 */
class SmsAuthenticationTokenTest {

    @Test
    @DisplayName("测试创建未认证令牌 - 手机号和验证码正确存储")
    void testUnauthenticatedToken_StoresPhoneAndCode() {
        // Given
        String phone = "13800138000";
        String code = "123456";

        // When
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);

        // Then
        assertEquals(phone, token.getPhoneNumber());
        assertEquals(code, token.getVerifyCode());
        assertFalse(token.isAuthenticated());
    }

    @Test
    @DisplayName("测试未认证令牌 - getPrincipal 返回手机号")
    void testUnauthenticatedToken_GetPrincipalReturnsPhone() {
        // Given
        String phone = "13800138000";
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, "123456");

        // When & Then
        assertEquals(phone, token.getPrincipal());
    }

    @Test
    @DisplayName("测试未认证令牌 - getCredentials 返回验证码")
    void testUnauthenticatedToken_GetCredentialsReturnsCode() {
        // Given
        String code = "123456";
        SmsAuthenticationToken token = new SmsAuthenticationToken("13800138000", code);

        // When & Then
        assertEquals(code, token.getCredentials());
    }

    @Test
    @DisplayName("测试创建已认证令牌 - 权限正确设置")
    void testAuthenticatedToken_HasAuthorities() {
        // Given
        String phone = "13800138000";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

        // When
        SmsAuthenticationToken token = new SmsAuthenticationToken(
            phone, null, Collections.singletonList(authority));

        // Then
        assertTrue(token.isAuthenticated());
        assertEquals(1, token.getAuthorities().size());
        assertEquals("ROLE_USER", token.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("测试已认证令牌 - 验证码为 null")
    void testAuthenticatedToken_VerifyCodeIsNull() {
        // Given
        String phone = "13800138000";

        // When
        SmsAuthenticationToken token = new SmsAuthenticationToken(
            phone, null, Collections.emptyList());

        // Then
        assertNull(token.getVerifyCode());
    }

    @Test
    @DisplayName("测试已认证令牌 - getPrincipal 返回手机号")
    void testAuthenticatedToken_GetPrincipalReturnsPhone() {
        // Given
        String phone = "13800138000";

        // When
        SmsAuthenticationToken token = new SmsAuthenticationToken(
            phone, null, Collections.emptyList());

        // Then
        assertEquals(phone, token.getPrincipal());
    }

    @Test
    @DisplayName("测试已认证令牌 - getCredentials 返回 null")
    void testAuthenticatedToken_GetCredentialsReturnsNull() {
        // Given
        SmsAuthenticationToken token = new SmsAuthenticationToken(
            "13800138000", null, Collections.emptyList());

        // When & Then
        assertNull(token.getCredentials());
    }

    @Test
    @DisplayName("测试未认证令牌 - 空手机号")
    void testUnauthenticatedToken_EmptyPhone() {
        // Given
        String phone = "";
        String code = "123456";

        // When
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);

        // Then
        assertEquals("", token.getPhoneNumber());
        assertFalse(token.isAuthenticated());
    }

    @Test
    @DisplayName("测试已认证令牌 - 空权限列表")
    void testAuthenticatedToken_EmptyAuthorities() {
        // Given
        String phone = "13800138000";

        // When
        SmsAuthenticationToken token = new SmsAuthenticationToken(
            phone, null, Collections.emptyList());

        // Then
        assertTrue(token.getAuthorities().isEmpty());
        assertTrue(token.isAuthenticated());
    }
}
