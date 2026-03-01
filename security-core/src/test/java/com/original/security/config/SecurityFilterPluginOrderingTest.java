package com.original.security.config;

import com.original.security.annotation.EnableSecurityBoot;
import com.original.security.plugin.SecurityFilterPlugin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
 * SecurityFilterPlugin 排序测试。
 * <p>
 * 验证多个插件在相同 Position 和相同 TargetFilterClass 时的排序行为。
 * <p>
 * 测试场景：
 * <ul>
 *     <li>两个插件都配置为 BEFORE UsernamePasswordAuthenticationFilter</li>
 *     <li>通过 @Order 注解控制相对顺序</li>
 *     <li>验证 order 值小的插件先执行</li>
 * </ul>
 *
 * @author Naulu
 * @since 1.0.0
 */
@SpringBootTest(classes = SecurityFilterPluginOrderingTest.TestConfig.class, properties = {
        "security.network.cors.enabled=false",
        "security.network.csrf.enabled=false",
        "security.headers.enabled=false",
        "security.config.validation=false"
})
public class SecurityFilterPluginOrderingTest {

    @Autowired
    private SecurityFilterChain filterChain;

    /**
     * 测试相同位置多个插件的排序。
     * <p>
     * 验证当多个插件配置了相同的 Position 和 TargetFilterClass 时，
     * 按照 @Order 注解（或 getOrder() 方法）的值升序排列执行。
     */
    @Test
    void testSecurityFilterChain_SamePositionPlugins_OrderedCorrectly() {
        List<Filter> filters = filterChain.getFilters();

        // 验证两个插件都已加载（使用 Filter 类的 simple name）
        assertThat(containsFilter(filters, "OrderedPluginFirstFilter"))
                .as("OrderedPluginFirstFilter should be loaded")
                .isTrue();
        assertThat(containsFilter(filters, "OrderedPluginSecondFilter"))
                .as("OrderedPluginSecondFilter should be loaded")
                .isTrue();

        // 获取索引位置
        int firstIndex = getFilterIndex(filters, "OrderedPluginFirstFilter");
        int secondIndex = getFilterIndex(filters, "OrderedPluginSecondFilter");

        // 验证两个插件都存在
        assertThat(firstIndex).as("OrderedPluginFirstFilter should be found").isGreaterThanOrEqualTo(0);
        assertThat(secondIndex).as("OrderedPluginSecondFilter should be found").isGreaterThanOrEqualTo(0);

        // 验证排序：@Order(1) 的插件应该在 @Order(2) 的插件之前
        assertThat(firstIndex)
                .as("Plugin with @Order(1) should execute before plugin with @Order(2) at same position")
                .isLessThan(secondIndex);
    }

    /**
     * 测试配置类，提供两个相同位置的测试插件。
     * <p>
     * 两个插件都配置为 BEFORE UsernamePasswordAuthenticationFilter，
     * 通过 @Order 注解区分执行顺序。
     */
    @Configuration
    @EnableSecurityBoot
    static class TestConfig {

        /**
         * 创建第一个插件，@Order(1)，应该先执行。
         *
         * @return SecurityFilterPlugin 实例
         */
        @Bean
        @Order(1)
        public SecurityFilterPlugin orderedPluginFirst() {
            final Filter filter = new OrderedPluginFirstFilter();
            return new SecurityFilterPlugin() {
                @Override
                public String getName() {
                    return "OrderedPluginFirst";
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
         * 创建第二个插件，@Order(2)，应该后执行。
         *
         * @return SecurityFilterPlugin 实例
         */
        @Bean
        @Order(2)
        public SecurityFilterPlugin orderedPluginSecond() {
            final Filter filter = new OrderedPluginSecondFilter();
            return new SecurityFilterPlugin() {
                @Override
                public String getName() {
                    return "OrderedPluginSecond";
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
    }

    /**
     * 第一个排序测试过滤器。
     *
     * @author Naulu
     * @since 1.0.0
     */
    static class OrderedPluginFirstFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
        }
    }

    /**
     * 第二个排序测试过滤器。
     *
     * @author Naulu
     * @since 1.0.0
     */
    static class OrderedPluginSecondFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
        }
    }
}
