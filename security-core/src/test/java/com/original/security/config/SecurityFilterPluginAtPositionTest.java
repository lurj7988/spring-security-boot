package com.original.security.config;

import com.original.security.annotation.EnableSecurityBoot;
import com.original.security.plugin.SecurityFilterPlugin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.original.security.config.FilterTestUtils.containsFilter;
import static com.original.security.config.FilterTestUtils.getFilterIndex;

/**
 * SecurityFilterPlugin AT 位置测试。
 * <p>
 * 验证使用 Position.AT 时过滤器能够正确替换目标过滤器。
 *
 * @author Naulu
 * @since 1.0.0
 */
@SpringBootTest(classes = SecurityFilterPluginAtPositionTest.TestConfig.class, properties = {
        "security.network.cors.enabled=false",
        "security.network.csrf.enabled=true",
        "security.headers.enabled=false",
        "security.config.validation=false"
})
public class SecurityFilterPluginAtPositionTest {

    @Autowired
    private SecurityFilterChain filterChain;

    /**
     * 测试 AT 位置能够在目标过滤器相同位置添加。
     * <p>
     * 验证使用 Position.AT 时，自定义过滤器与目标过滤器在同一位置共存。
     * 注意：Spring Security 的 addFilterAt() 不会替换目标过滤器。
     */
    @Test
    void testSecurityFilterChain_WithAtPosition_AddsAtSamePosition() {
        List<Filter> filters = filterChain.getFilters();

        // 验证自定义过滤器已加载
        assertThat(containsFilter(filters, "TestAtPluginFilter"))
                .as("AT plugin filter should be loaded")
                .isTrue();

        // 验证 AT 过滤器位置
        int atPluginIndex = getFilterIndex(filters, "TestAtPluginFilter");
        assertThat(atPluginIndex)
                .as("AT plugin should be found in filter chain")
                .isGreaterThanOrEqualTo(0);

        // 验证过滤器链中同时存在 CsrfFilter 和我们的 AT 过滤器
        // Spring Security 的 addFilterAt 行为是在同一位置添加新过滤器
        // 两者都会存在于过滤器链中
        assertThat(containsFilter(filters, "CsrfFilter"))
                .as("CsrfFilter should still exist in filter chain (AT adds at same position)")
                .isTrue();
    }

    /**
     * 测试 AT 位置过滤器与目标过滤器位置关系。
     * <p>
     * 验证 AT 过滤器被添加到目标过滤器的相同位置区域。
     */
    @Test
    void testSecurityFilterChain_AtPositionFilter_ProximityToTarget() {
        List<Filter> filters = filterChain.getFilters();

        int atPluginIndex = getFilterIndex(filters, "TestAtPluginFilter");
        int csrfIndex = getFilterIndex(filters, "CsrfFilter");

        // 两个过滤器都应该存在
        assertThat(atPluginIndex).isGreaterThanOrEqualTo(0);
        assertThat(csrfIndex).isGreaterThanOrEqualTo(0);

        // AT 过滤器应该在目标过滤器的邻近位置（前后差值不超过1）
        int indexDifference = Math.abs(atPluginIndex - csrfIndex);
        assertThat(indexDifference)
                .as("AT filter should be at or adjacent to target filter position (difference <= 1)")
                .isLessThanOrEqualTo(1);
    }

    /**
     * 测试配置类，提供使用 AT 位置的 SecurityFilterPlugin Bean。
     */
    @Configuration
    @EnableSecurityBoot
    static class TestConfig {

        /**
         * 创建使用 AT 位置的测试插件。
         * <p>
         * 该插件将在 CsrfFilter 的位置执行。
         *
         * @return SecurityFilterPlugin 实例
         */
        @Bean
        public SecurityFilterPlugin atPlugin() {
            final Filter filter = new TestAtPluginFilter();
            return new SecurityFilterPlugin() {
                @Override
                public String getName() {
                    return "AtPlugin";
                }

                @Override
                public Filter getFilter() {
                    return filter;
                }

                @Override
                public Position getPosition() {
                    return Position.AT;
                }

                @Override
                public Class<? extends Filter> getTargetFilterClass() {
                    return CsrfFilter.class;
                }
            };
        }
    }

    /**
     * 测试用 AT 位置过滤器，用于验证 AT 位置机制。
     *
     * @author Naulu
     * @since 1.0.0
     */
    static class TestAtPluginFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
        }
    }
}
