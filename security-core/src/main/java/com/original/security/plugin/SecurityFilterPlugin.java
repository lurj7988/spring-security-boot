package com.original.security.plugin;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.Filter;

/**
 * 安全过滤器插件接口。
 * <p>
 * 允许开发者自定义过滤器，并指定其在 Spring Security 过滤器链中的位置。
 * <p>
 * <b>插件排序规则：</b>
 * <ul>
 *     <li>本接口继承 {@link Ordered}，Spring 会自动按 {@link #getOrder()} 返回值升序排列</li>
 *     <li>也可以使用 {@link Order @Order} 注解覆盖默认的 getOrder() 值</li>
 *     <li>相同 {@link Position} 和目标过滤器的插件，按 order 顺序依次添加</li>
 * </ul>
 *
 * @author Naulu
 * @since 1.0.0
 */
public interface SecurityFilterPlugin extends Ordered {

    /**
     * 指定过滤器添加的位置类型。
     */
    enum Position {
        /**
         * 在指定的 {@link #getTargetFilterClass()} 之前添加。
         */
        BEFORE,

        /**
         * 在指定的 {@link #getTargetFilterClass()} 之后添加。
         */
        AFTER,

        /**
         * 在指定的 {@link #getTargetFilterClass()} 的相同位置添加过滤器。
         * <p>
         * 注意：目标过滤器仍然存在于过滤器链中，Spring Security 的
         * {@code addFilterAt()} 行为是在同一位置添加新过滤器，而非替换。
         */
        AT
    }

    /**
     * 获取过滤器名称。
     *
     * @return 过滤器名称
     */
    String getName();

    /**
     * 获取具体的过滤器实例。
     * <p>
     * <b>重要：</b>实现者应确保每次调用此方法返回相同的过滤器实例。
     * 返回不同的实例可能导致过滤器链中出现重复过滤器或不可预测的行为。
     * <p>
     * <b>推荐实现：</b>建议使用 Spring Security 的 {@code OncePerRequestFilter} 作为基类，
     * 以确保过滤器在每个请求中只执行一次，避免转发/包含场景下的重复执行。
     * <p>
     * 建议实现方式：
     * <pre>{@code
     * private final Filter filter = new MyCustomFilter(); // 推荐 extends OncePerRequestFilter
     *
     * @Override
     * public Filter getFilter() {
     *     return filter;  // 返回缓存的单例实例
     * }
     * }</pre>
     *
     * @return 过滤器实例（应为相同实例，推荐使用 OncePerRequestFilter）
     */
    Filter getFilter();

    /**
     * 获取添加位置类型。
     *
     * @return 位置类型，默认是在 {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter} 之前
     */
    default Position getPosition() {
        return Position.BEFORE;
    }

    /**
     * 获取相对位置的目标过滤器类。
     *
     * @return 目标过滤器类，默认对应的是 {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}
     */
    default Class<? extends Filter> getTargetFilterClass() {
        return org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class;
    }

    /**
     * 是否启用此过滤器。
     *
     * @return true 如果启用，否则 false。默认返回 true
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 获取插件在相同位置过滤器中的排序优先级。
     * <p>
     * 本接口继承 {@link Ordered}，Spring 会通过
     * {@link org.springframework.beans.factory.ObjectProvider#orderedStream()} 自动按此值排序。
     * <p>
     * 当多个插件配置了相同的 {@link Position} 和 {@link #getTargetFilterClass()} 时，
     * 按 order 值升序排列执行。值越小优先级越高（越先执行）。
     * <p>
     * <b>也可以使用 {@link Order @Order} 注解覆盖此方法的返回值。</b>
     *
     * @return 排序值，默认为 0（中等优先级）。负值表示高优先级，正值表示低优先级
     * @since 1.0.0
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
