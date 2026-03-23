package com.example.security.plugin.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 短信认证插件单元测试。
 *
 * @author Example Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SmsAuthenticationPluginTest {

    @Mock
    private SmsAuthenticationProvider authenticationProvider;

    private SmsAuthenticationPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new SmsAuthenticationPlugin(authenticationProvider);
    }

    @Test
    @DisplayName("测试获取插件名称")
    void testGetName_ReturnsCorrectName() {
        assertEquals("sms-authentication", plugin.getName());
    }

    @Test
    @DisplayName("测试获取认证提供者")
    void testGetAuthenticationProvider_ReturnsProvider() {
        assertSame(authenticationProvider, plugin.getAuthenticationProvider());
    }

    @Test
    @DisplayName("测试支持 SmsAuthenticationToken 类型")
    void testSupports_ValidToken_ReturnsTrue() {
        assertTrue(plugin.supports(SmsAuthenticationToken.class));
    }

    @Test
    @DisplayName("测试不支持其他 Token 类型")
    void testSupports_InvalidToken_ReturnsFalse() {
        assertFalse(plugin.supports(
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("测试 supports 传入 null 返回 false")
    void testSupports_Null_ReturnsFalse() {
        assertFalse(plugin.supports(null));
    }
}
