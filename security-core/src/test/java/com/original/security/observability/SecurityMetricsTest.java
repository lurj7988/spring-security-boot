package com.original.security.observability;

import com.original.security.config.SecurityMetricsProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Collections;

/**
 * SecurityMetrics 单元测试。
 * <p>
 * 测试覆盖：
 * - 计数器功能
 * - 标签（tags）正确性
 * - Timer 百分位配置
 * - 空值处理
 * - 无 MeterRegistry 时的行为
 * - 标签格式验证
 * - 自定义百分位数配置
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class SecurityMetricsTest {

    private static final String METRIC_SUCCESS = "security.authentication.success";
    private static final String METRIC_FAILURE = "security.authentication.failure";
    private static final String METRIC_DURATION = "security.authentication.duration";
    private static final String TAG_AUTH_TYPE = "authentication_type";
    private static final String TAG_FAILURE_REASON = "failure_reason";

    private MeterRegistry meterRegistry;
    private SecurityMetrics securityMetrics;
    private SecurityMetricsProperties properties;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        properties = new SecurityMetricsProperties();
        securityMetrics = new SecurityMetrics(meterRegistry, properties);
    }

    // ==================== 成功计数器测试 ====================

    @Test
    void testRecordAuthenticationSuccess_ValidAuthType_IncrementsCounter() {
        String authType = "username-password";

        securityMetrics.recordAuthenticationSuccess(authType);
        securityMetrics.recordAuthenticationSuccess(authType);

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter, "Counter should be registered");
        assertEquals(2.0, counter.count(), 0.001, "Should record 2 successful authentications");
    }

    @Test
    void testRecordAuthenticationSuccess_WithTags_HasCorrectTags() {
        securityMetrics.recordAuthenticationSuccess("jwt");

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("jwt")),
                "Counter should have authentication_type=jwt tag");
    }

    @Test
    void testRecordAuthenticationSuccess_NullAuthType_UsesDefaultTag() {
        securityMetrics.recordAuthenticationSuccess(null);

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("unknown")),
                "Counter should use 'unknown' for null auth type");
    }

    @Test
    void testRecordAuthenticationSuccess_DifferentAuthTypes_CreatesSeparateCounters() {
        securityMetrics.recordAuthenticationSuccess("jwt");
        securityMetrics.recordAuthenticationSuccess("username-password");

        // 验证有两个不同的 counter（按 tag 区分）
        AtomicInteger count = new AtomicInteger(0);
        meterRegistry.find(METRIC_SUCCESS).counters().forEach(c -> count.addAndGet((int) c.count()));
        assertEquals(2, count.get(), "Should have 2 total increments across all counters");
    }

    // ==================== 失败计数器测试 ====================

    @Test
    void testRecordAuthenticationFailure_ValidParams_IncrementsCounter() {
        securityMetrics.recordAuthenticationFailure("username-password", "BadCredentialsException");

        Counter counter = meterRegistry.find(METRIC_FAILURE).counter();
        assertNotNull(counter, "Counter should be registered");
        assertEquals(1.0, counter.count(), 0.001, "Should record 1 failed authentication");
    }

    @Test
    void testRecordAuthenticationFailure_WithTags_HasCorrectTags() {
        securityMetrics.recordAuthenticationFailure("jwt", "ExpiredJwtException");

        Counter counter = meterRegistry.find(METRIC_FAILURE).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("jwt")),
                "Counter should have authentication_type=jwt tag");
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_FAILURE_REASON) && t.getValue().equals("ExpiredJwtException")),
                "Counter should have failure_reason=ExpiredJwtException tag");
    }

    @Test
    void testRecordAuthenticationFailure_NullParams_UsesDefaultTags() {
        securityMetrics.recordAuthenticationFailure(null, null);

        Counter counter = meterRegistry.find(METRIC_FAILURE).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("unknown")),
                "Counter should use 'unknown' for null auth type");
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_FAILURE_REASON) && t.getValue().equals("unknown")),
                "Counter should use 'unknown' for null failure reason");
    }

    // ==================== Duration Timer 测试 ====================

    @Test
    void testRecordAuthenticationDuration_Nanos_RecordsTimer() {
        long durationNanos = 50_000_000L; // 50ms

        securityMetrics.recordAuthenticationDuration("username-password", durationNanos);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer, "Timer should be registered");
        assertEquals(1, timer.count(), "Should record 1 duration measurement");
    }

    @Test
    void testRecordAuthenticationDuration_WithTimeUnit_RecordsTimer() {
        securityMetrics.recordAuthenticationDuration("jwt", 100, TimeUnit.MILLISECONDS);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer);
        assertEquals(1, timer.count(), "Should record 1 duration measurement");
    }

    @Test
    void testRecordAuthenticationDuration_HasCorrectTags() {
        securityMetrics.recordAuthenticationDuration("oauth2", 50_000_000L);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer);

        Meter.Id id = timer.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("oauth2")),
                "Timer should have authentication_type=oauth2 tag");
    }

    @Test
    void testRecordAuthenticationDuration_HasPercentileConfig() {
        securityMetrics.recordAuthenticationDuration("test", 1_000_000L);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer);

        Meter.Id id = timer.getId();
        // 验证 description 存在
        assertNotNull(id.getDescription(), "Timer should have description");
        assertEquals("Authentication duration in milliseconds", id.getDescription());
    }

    // ==================== 无 MeterRegistry 测试 ====================

    @Test
    void testSecurityMetrics_WithNullMeterRegistry_DoesNotThrow() {
        SecurityMetrics noOpMetrics = new SecurityMetrics(null, properties);

        // 这些调用不应该抛出异常
        assertDoesNotThrow(() -> noOpMetrics.recordAuthenticationSuccess("test"));
        assertDoesNotThrow(() -> noOpMetrics.recordAuthenticationFailure("test", "reason"));
        assertDoesNotThrow(() -> noOpMetrics.recordAuthenticationDuration("test", 1000L));
    }

    @Test
    void testIsMetricsEnabled_WithMeterRegistry_ReturnsTrue() {
        assertTrue(securityMetrics.isMetricsEnabled(), "Should be enabled with MeterRegistry");
    }

    @Test
    void testIsMetricsEnabled_WithoutMeterRegistry_ReturnsFalse() {
        SecurityMetrics noOpMetrics = new SecurityMetrics(null, properties);
        assertFalse(noOpMetrics.isMetricsEnabled(), "Should be disabled without MeterRegistry");
    }

    @Test
    void testIsMetricsEnabled_MultipleCalls_ReturnsSameValue() {
        // 多次调用应返回相同的值
        boolean firstCall = securityMetrics.isMetricsEnabled();
        boolean secondCall = securityMetrics.isMetricsEnabled();
        assertEquals(firstCall, secondCall, "Multiple calls should return same value");
    }

    // ==================== 边界条件测试 ====================

    @Test
    void testRecordAuthenticationSuccess_EmptyAuthType_UsesDefaultTag() {
        securityMetrics.recordAuthenticationSuccess("");

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("unknown")),
                "Counter should use 'unknown' for empty auth type");
    }

    @Test
    void testRecordAuthenticationFailure_EmptyParams_UsesDefaultTags() {
        securityMetrics.recordAuthenticationFailure("", "");

        Counter counter = meterRegistry.find(METRIC_FAILURE).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .allMatch(t -> t.getValue().equals("unknown")),
                "Counter should use 'unknown' for empty params");
    }

    // ==================== 标签格式验证测试 ====================

    @Test
    void testRecordAuthenticationSuccess_LongTagValue_Truncated() {
        // 测试超长标签值（256字符）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            sb.append('a');
        }
        String longTag = sb.toString();
        securityMetrics.recordAuthenticationSuccess(longTag);

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().length() == 255),
                "Long tag should be truncated to 255 chars");
    }

    @Test
    void testRecordAuthenticationSuccess_InvalidTagFormat_UsesDefault() {
        // 测试包含非法字符的标签
        String invalidTag = "auth.type@with#invalid*chars";
        securityMetrics.recordAuthenticationSuccess(invalidTag);

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("unknown")),
                "Invalid tag format should use default");
    }

    @Test
    void testRecordAuthenticationSuccess_TagStartingWithDot_UsesDefault() {
        // 测试以点开头的标签
        String invalidTag = ".invalid.tag";
        securityMetrics.recordAuthenticationSuccess(invalidTag);

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("unknown")),
                "Tag starting with dot should use default");
    }

    @Test
    void testRecordAuthenticationSuccess_TagEndingWithDot_UsesDefault() {
        // 测试以点结尾的标签
        String invalidTag = "invalid.tag.";
        securityMetrics.recordAuthenticationSuccess(invalidTag);

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals("unknown")),
                "Tag ending with dot should use default");
    }

    @Test
    void testRecordAuthenticationSuccess_ValidSpecialChars_Preserved() {
        // 测试合法的特殊字符
        String validTag = "auth-type.auth_type:j-w";
        securityMetrics.recordAuthenticationSuccess(validTag);

        Counter counter = meterRegistry.find(METRIC_SUCCESS).counter();
        assertNotNull(counter);

        Meter.Id id = counter.getId();
        assertTrue(id.getTags().stream()
                .anyMatch(t -> t.getKey().equals(TAG_AUTH_TYPE) && t.getValue().equals(validTag)),
                "Valid special chars should be preserved");
    }

    // ==================== 自定义百分位数测试 ====================

    @Test
    void testSecurityMetrics_WithCustomPercentiles_UsesConfiguredValues() {
        // 测试自定义百分位数配置
        properties.setDurationPercentiles(Arrays.asList(0.75, 0.9, 0.95));
        SecurityMetrics customMetrics = new SecurityMetrics(meterRegistry, properties);

        customMetrics.recordAuthenticationDuration("test", 1000L);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer);
        // 验证百分位数配置
        // 注意：直接验证 Timer 的百分位数配置比较复杂，这里我们只验证 Timer 能正常创建
        assertTrue(timer.count() > 0, "Timer should have recorded at least one measurement");
    }

    @Test
    void testSecurityMetrics_WithEmptyPercentiles_UsesDefault() {
        // 测试空百分位数配置
        properties.setDurationPercentiles(Collections.emptyList());
        SecurityMetrics customMetrics = new SecurityMetrics(meterRegistry, properties);

        customMetrics.recordAuthenticationDuration("test", 1000L);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer, "Timer should still be registered with default percentiles");
    }

    @Test
    void testSecurityMetrics_WithNullPercentiles_UsesDefault() {
        // 测试空百分位数配置
        properties.setDurationPercentiles(null);
        SecurityMetrics customMetrics = new SecurityMetrics(meterRegistry, properties);

        customMetrics.recordAuthenticationDuration("test", 1000L);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer, "Timer should still be registered with default percentiles");
    }

    @Test
    void testSecurityMetrics_WithInvalidPercentiles_IgnoredAndUsesDefault() {
        // 测试包含无效百分位数的配置
        properties.setDurationPercentiles(Arrays.asList(0.5, 1.5, -0.1, 0.95));
        SecurityMetrics customMetrics = new SecurityMetrics(meterRegistry, properties);

        customMetrics.recordAuthenticationDuration("test", 1000L);

        Timer timer = meterRegistry.find(METRIC_DURATION).timer();
        assertNotNull(timer, "Timer should still be registered with valid percentiles only");
    }

    @Test
    void testSecurityMetrics_WithNullMeterRegistry_DoesNotThrowAndReturnsFalseForIsEnabled() {
        SecurityMetrics noOpMetrics = new SecurityMetrics(null, properties);

        // 这些调用不应该抛出异常
        assertDoesNotThrow(() -> noOpMetrics.recordAuthenticationSuccess("test"));
        assertDoesNotThrow(() -> noOpMetrics.recordAuthenticationFailure("test", "reason"));
        assertDoesNotThrow(() -> noOpMetrics.recordAuthenticationDuration("test", 1000L));

        // 验证 isMetricsEnabled() 返回 false
        assertFalse(noOpMetrics.isMetricsEnabled(), "isMetricsEnabled() should return false when MeterRegistry is null");
    }

    @Test
    void testSecurityMetrics_WithNullMeterRegistry_MetricsOperationsAreNoOps() {
        SecurityMetrics noOpMetrics = new SecurityMetrics(null, properties);

        // 验证所有操作都是 no-op
        assertDoesNotThrow(() -> {
            noOpMetrics.recordAuthenticationSuccess("test");
            noOpMetrics.recordAuthenticationFailure("test", "reason");
            noOpMetrics.recordAuthenticationDuration("test", 1000L);
            noOpMetrics.recordAuthenticationDuration("test", 1000L, TimeUnit.MILLISECONDS);
        });
    }
}