package com.original.security.filter;

import com.original.security.config.SecurityMetricsProperties;
import com.original.security.observability.SecurityMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthenticationMetricsFilter 单元测试。
 * <p>
 * 测试覆盖：
 * - 认证请求的时间记录
 * - 非认证请求的性能优化
 * - 过滤器正确配置和执行
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class AuthenticationMetricsFilterTest {

    private static final String AUTH_LOGIN_PATH = "/api/auth/login";
    private static final String LOGIN_PATH = "/login";
    private static final String NON_AUTH_PATH = "/api/user/profile";

    @Mock
    private SecurityMetrics securityMetrics;

    @Mock
    private SecurityMetricsProperties properties;

    private AuthenticationMetricsFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 配置属性
        when(properties.isAuthenticationDurationEnabled()).thenReturn(true);

        // 创建过滤器
        filter = new AuthenticationMetricsFilter(securityMetrics, properties);

        // 创建测试请求和响应
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void testAuthenticationRequest_RecordsTime() throws ServletException, IOException {
        // 设置认证请求
        request.setRequestURI(AUTH_LOGIN_PATH);

        // 执行过滤器
        filter.doFilterInternal(request, response, filterChain);

        // 验证认证耗时被记录
        verify(securityMetrics).recordAuthenticationDuration(anyString(), anyLong());
    }

    @Test
    void testNonAuthenticationRequest_SkipsTimeRecording() throws ServletException, IOException {
        // 设置非认证请求
        request.setRequestURI(NON_AUTH_PATH);

        // 执行过滤器
        filter.doFilterInternal(request, response, filterChain);

        // 验证认证耗时没有被记录（性能优化）
        verify(securityMetrics, never()).recordAuthenticationDuration(anyString(), anyLong());
    }

    @Test
    void testLoginRequest_RecordsTime() throws ServletException, IOException {
        // 设置登录请求
        request.setRequestURI(LOGIN_PATH);

        // 执行过滤器
        filter.doFilterInternal(request, response, filterChain);

        // 验证认证耗时被记录
        verify(securityMetrics).recordAuthenticationDuration(anyString(), anyLong());
    }

    @Test
    void testAuthenticationRequestWithPathPrefix_RecordsTime() throws ServletException, IOException {
        // 设置带前缀的认证请求
        request.setRequestURI("/context" + AUTH_LOGIN_PATH);
        request.setContextPath("/context");

        // 执行过滤器
        filter.doFilterInternal(request, response, filterChain);

        // 验证认证耗时被记录
        verify(securityMetrics).recordAuthenticationDuration(anyString(), anyLong());
    }


    @Test
    void testFilterPluginProperties() {
        // 验证过滤器插件属性
        assertEquals("AuthenticationMetricsFilter", filter.getName());
        assertNotNull(filter.getFilter());
        assertEquals(filter, filter.getFilter());
        assertEquals(AuthenticationMetricsFilter.Position.BEFORE, filter.getPosition());
        assertTrue(filter.isEnabled(), "Filter should be enabled by default");
    }

    @Test
    void testFilterDisabledWhenConfigured() {
        // 禁用过滤器
        when(properties.isAuthenticationDurationEnabled()).thenReturn(false);

        AuthenticationMetricsFilter disabledFilter = new AuthenticationMetricsFilter(securityMetrics, properties);

        // 验证过滤器被禁用
        assertFalse(disabledFilter.isEnabled(), "Filter should be disabled when property is false");
    }
}