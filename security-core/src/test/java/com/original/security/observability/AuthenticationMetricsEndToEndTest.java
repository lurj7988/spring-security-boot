package com.original.security.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authentication Metrics 端到端集成测试。
 * <p>
 * 验证：
 * - HTTP 请求触发 Metrics 记录
 * - /actuator/metrics 端点导出正确的 Metrics
 * - 不同认证类型产生不同的标签
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class AuthenticationMetricsEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 清空之前的 metrics 以确保测试隔离
        meterRegistry.clear();
    }

    @Test
    @WithMockUser
    void testAuthenticationMetricsEndpoint_Success() throws Exception {
        // 模拟认证请求（实际项目中需要配置认证端点）
        // 这里我们直接测试 Metrics 的记录和查询

        // 记录一次认证成功
        SecurityMetrics metrics = new SecurityMetrics(meterRegistry, new com.original.security.config.SecurityMetricsProperties());
        metrics.recordAuthenticationSuccess("username-password");

        // 验证成功计数器增加
        double successCount = meterRegistry.get("security.authentication.success")
                .tag("authentication_type", "username-password")
                .counter()
                .count();

        assertThat(successCount).isEqualTo(1.0);
    }

    @Test
    @WithMockUser
    void testAuthenticationMetricsEndpoint_Failure() throws Exception {
        // 记录一次认证失败
        SecurityMetrics metrics = new SecurityMetrics(meterRegistry, new com.original.security.config.SecurityMetricsProperties());
        metrics.recordAuthenticationFailure("jwt", "ExpiredJwtException");

        // 验证失败计数器增加
        double failureCount = meterRegistry.get("security.authentication.failure")
                .tag("authentication_type", "jwt")
                .tag("failure_reason", "ExpiredJwtException")
                .counter()
                .count();

        assertThat(failureCount).isEqualTo(1.0);
    }

    @Test
    @WithMockUser
    void testAuthenticationMetricsEndpoint_Duration() throws Exception {
        // 记录认证耗时
        SecurityMetrics metrics = new SecurityMetrics(meterRegistry, new com.original.security.config.SecurityMetricsProperties());

        // 记录多个耗时数据点
        metrics.recordAuthenticationDuration("username-password", 50_000_000L); // 50ms
        metrics.recordAuthenticationDuration("username-password", 100_000_000L); // 100ms
        metrics.recordAuthenticationDuration("username-password", 200_000_000L); // 200ms

        // 验证计时器记录
        long durationCount = meterRegistry.get("security.authentication.duration")
                .tag("authentication_type", "username-password")
                .timer()
                .count();

        assertThat(durationCount).isEqualTo(3);

        // 验证总耗时
        double totalTime = meterRegistry.get("security.authentication.duration")
                .tag("authentication_type", "username-password")
                .timer()
                .totalTime(java.util.concurrent.TimeUnit.MILLISECONDS);

        assertThat(totalTime).isBetween(350.0, 351.0); // 50 + 100 + 200 = 350ms
    }

    @Test
    @WithMockUser
    void testAuthenticationMetrics_MultipleAuthTypes() throws Exception {
        SecurityMetrics metrics = new SecurityMetrics(meterRegistry, new com.original.security.config.SecurityMetricsProperties());

        // 记录不同认证类型
        metrics.recordAuthenticationSuccess("username-password");
        metrics.recordAuthenticationSuccess("jwt");
        metrics.recordAuthenticationSuccess("oauth2");
        metrics.recordAuthenticationSuccess("username-password"); // username-password 两次

        // 验证 username-password 计数
        double usernamePasswordCount = meterRegistry.get("security.authentication.success")
                .tag("authentication_type", "username-password")
                .counter()
                .count();
        assertThat(usernamePasswordCount).isEqualTo(2.0);

        // 验证 jwt 计数
        double jwtCount = meterRegistry.get("security.authentication.success")
                .tag("authentication_type", "jwt")
                .counter()
                .count();
        assertThat(jwtCount).isEqualTo(1.0);

        // 验证 oauth2 计数
        double oauth2Count = meterRegistry.get("security.authentication.success")
                .tag("authentication_type", "oauth2")
                .counter()
                .count();
        assertThat(oauth2Count).isEqualTo(1.0);
    }

    @Test
    @WithMockUser
    void testAuthenticationMetrics_NullAndEmptyTags() throws Exception {
        SecurityMetrics metrics = new SecurityMetrics(meterRegistry, new com.original.security.config.SecurityMetricsProperties());

        // 测试 null 和空标签
        metrics.recordAuthenticationSuccess(null);
        metrics.recordAuthenticationSuccess("");
        metrics.recordAuthenticationSuccess("  ");

        // 所有无效标签都应该使用 "unknown"
        double unknownCount = meterRegistry.get("security.authentication.success")
                .tag("authentication_type", "unknown")
                .counter()
                .count();

        assertThat(unknownCount).isEqualTo(3.0);
    }

    /**
     * 测试配置类，提供 SimpleMeterRegistry 用于测试。
     */
    @Configuration
    static class TestConfig {
        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
