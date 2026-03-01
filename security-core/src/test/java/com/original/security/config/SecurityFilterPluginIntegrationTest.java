package com.original.security.config;

import com.original.security.annotation.EnableSecurityBoot;
import com.original.security.plugin.SecurityFilterPlugin;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.original.security.config.FilterTestUtils.containsFilter;
import static com.original.security.config.FilterTestUtils.getFilterIndex;

/**
 * SecurityFilterPlugin 集成测试。
 * <p>
 * 验证自定义过滤器插件能够正确注入到 Spring Security 过滤器链中，
 * 并且按照指定的顺序执行。
 *
 * @author Naulu
 * @since 1.0.0
 */
@SpringBootTest(classes = SecurityFilterPluginIntegrationTest.TestConfig.class, properties = {
        "security.network.cors.enabled=true",
        "security.network.csrf.enabled=true",
        "security.headers.enabled=true",
        "security.headers.frame-options=DENY",
        "security.config.validation=false"
})
public class SecurityFilterPluginIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilterPluginIntegrationTest.class);

    @Autowired
    private SecurityFilterChain filterChain;

    /**
     * 测试自定义插件是否正确注入并按顺序排列。
     * <p>
     * 验证内容：
     * <ul>
     *     <li>CORS 过滤器最先执行</li>
     *     <li>CSRF 和 SecurityHeaders 在认证之前执行</li>
     *     <li>自定义插件按照配置的位置正确插入</li>
     * </ul>
     */
    @Test
    void testSecurityFilterChain_WithCustomPlugins_InjectsAndOrdersCorrectly() {
        List<Filter> filters = filterChain.getFilters();

        // Log loaded filters for debugging
        log.debug("Loaded Filters:");
        filters.forEach(f -> log.debug(" -> {}", f.getClass().getSimpleName()));

        // Assert native Spring Security filters
        assertThat(containsFilter(filters, "CorsFilter")).as("CORS filter should be present").isTrue();
        assertThat(containsFilter(filters, "CsrfFilter")).as("CSRF filter should be present").isTrue();
        assertThat(containsFilter(filters, "HeaderWriterFilter")).as("HeaderWriter filter should be present").isTrue();

        // Verify custom filters are loaded
        assertThat(containsFilter(filters, "TestPluginAFilter")).as("PluginA filter should be loaded").isTrue();
        assertThat(containsFilter(filters, "TestPluginBFilter")).as("PluginB filter should be loaded").isTrue();

        // Get filter indices for order verification
        int corsIndex = getFilterIndex(filters, "CorsFilter");
        int csrfIndex = getFilterIndex(filters, "CsrfFilter");
        int headersIndex = getFilterIndex(filters, "HeaderWriterFilter");
        int pluginAIndex = getFilterIndex(filters, "TestPluginAFilter");
        int pluginBIndex = getFilterIndex(filters, "TestPluginBFilter");

        // AC1: CORS 过滤器最先执行
        assertThat(corsIndex).as("CORS should execute before CSRF").isLessThan(csrfIndex);

        // AC1: CSRF 和 SecurityHeaders 在认证之前执行
        // 直接验证：CSRF 和 Headers 应该在 PluginA 之前
        // 因为 PluginA 配置为 BEFORE UsernamePasswordAuthenticationFilter
        // 所以如果 CSRF/Headers 在 PluginA 之前，就一定在认证过滤器之前
        assertThat(csrfIndex).as("CSRF should execute before authentication (verified via PluginA position)")
                .isLessThan(pluginAIndex);
        assertThat(headersIndex).as("SecurityHeaders should execute before authentication (verified via PluginA position)")
                .isLessThan(pluginAIndex);

        // 额外验证：确保认证相关的过滤器存在且在正确位置
        // 注意：UsernamePasswordAuthenticationFilter 可能被禁用，所以检查 AuthorizationFilter
        int authzIndex = getFilterIndex(filters, "AuthorizationFilter");
        if (authzIndex >= 0) {
            assertThat(csrfIndex).as("CSRF should execute before AuthorizationFilter")
                    .isLessThan(authzIndex);
            assertThat(headersIndex).as("SecurityHeaders should execute before AuthorizationFilter")
                    .isLessThan(authzIndex);
        }

        // Plugin A is BEFORE UsernamePasswordAuthenticationFilter
        // Plugin B is AFTER UsernamePasswordAuthenticationFilter
        assertThat(pluginAIndex).as("PluginA (BEFORE auth) should execute before PluginB (AFTER auth)")
                .isLessThan(pluginBIndex);
    }

    /**
     * 测试配置类，提供测试用的 SecurityFilterPlugin Bean。
     * <p>
     * 配置两个测试插件：
     * <ul>
     *     <li>pluginA: 在 UsernamePasswordAuthenticationFilter 之前执行</li>
     *     <li>pluginB: 在 UsernamePasswordAuthenticationFilter 之后执行</li>
     * </ul>
     */
    @Configuration
    @EnableSecurityBoot
    static class TestConfig {

        /**
         * 创建测试插件 A，配置为在认证过滤器之前执行。
         *
         * @return SecurityFilterPlugin 实例
         */
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public SecurityFilterPlugin pluginA() {
            final Filter filter = new TestPluginAFilter();
            return new SecurityFilterPlugin() {
                @Override
                public String getName() {
                    return "PluginA";
                }

                @Override
                public Filter getFilter() {
                    return filter;
                }

                @Override
                public Position getPosition() {
                    return Position.BEFORE;
                }

                @Override
                public Class<? extends Filter> getTargetFilterClass() {
                    return UsernamePasswordAuthenticationFilter.class;
                }
            };
        }

        /**
         * 创建测试插件 B，配置为在认证过滤器之后执行。
         *
         * @return SecurityFilterPlugin 实例
         */
        @Bean
        @Order(Ordered.LOWEST_PRECEDENCE)
        public SecurityFilterPlugin pluginB() {
            final Filter filter = new TestPluginBFilter();
            return new SecurityFilterPlugin() {
                @Override
                public String getName() {
                    return "PluginB";
                }

                @Override
                public Filter getFilter() {
                    return filter;
                }

                @Override
                public Position getPosition() {
                    return Position.AFTER;
                }

                @Override
                public Class<? extends Filter> getTargetFilterClass() {
                    return UsernamePasswordAuthenticationFilter.class;
                }
            };
        }
    }

    /**
     * 测试用过滤器 A，用于验证插件机制。
     *
     * @author Naulu
     * @since 1.0.0
     */
    static class TestPluginAFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
        }
    }

    /**
     * 测试用过滤器 B，用于验证插件机制。
     *
     * @author Naulu
     * @since 1.0.0
     */
    static class TestPluginBFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
        }
    }

    /**
     * 测试 FilterTestUtils 对同名过滤器的处理。
     * <p>
     * FilterTestUtils.getFilterIndex() 只返回第一个同名过滤器的索引，
     * 此测试验证该限制行为。
     */
    @Test
    void testFilterTestUtils_SameNameFilter_ReturnsFirstMatch() {
        // 创建包含两个同名过滤器的模拟列表
        List<Filter> filtersWithSameName = new ArrayList<>();
        filtersWithSameName.add(new TestPluginAFilter());
        filtersWithSameName.add(new TestPluginAFilter());  // 同名过滤器

        // getFilterIndex 应该返回第一个匹配的索引
        int index = getFilterIndex(filtersWithSameName, "TestPluginAFilter");
        assertThat(index).as("Should return index of first matching filter").isEqualTo(0);

        // containsFilter 应该返回 true
        assertThat(containsFilter(filtersWithSameName, "TestPluginAFilter")).isTrue();
    }
}
