package com.original.security.config;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

/**
 * 过滤器测试工具类。
 * <p>
 * 提供过滤器链测试的通用工具方法。
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *     <li>这些方法基于过滤器类的简单名称（simple name）进行匹配</li>
 *     <li>对于 Spring 代理的过滤器（如 AOP 代理），可能无法正确匹配</li>
 *     <li>如果过滤器链中有多个同名过滤器，只返回第一个匹配项</li>
 * </ul>
 *
 * @author Naulu
 * @since 1.0.0
 */
public final class FilterTestUtils {

    private FilterTestUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 获取指定过滤器在链中的索引位置。
     * <p>
     * <b>限制：</b>
     * <ul>
     *     <li>如果有多个同名过滤器，此方法只返回第一个匹配的索引</li>
     *     <li>对于 Spring 代理类（如 CGLIB 代理），类名可能包含 $$EnhancerBySpringCGLIB$$ 等后缀</li>
     *     <li>在 Spring Security 过滤器链中，通常不会有同名过滤器</li>
     * </ul>
     * <p>
     * <b>替代方案：</b>如果遇到代理类问题，可以考虑使用过滤器实现的接口名称进行匹配，
     * 或在测试配置中禁用代理。
     *
     * @param filters   过滤器列表，不能为 null
     * @param shortName 过滤器简单类名（不含包名），不能为 null
     * @return 索引位置，未找到返回 -1
     * @throws IllegalArgumentException 如果 filters 或 shortName 为 null
     */
    public static int getFilterIndex(List<Filter> filters, String shortName) {
        if (filters == null) {
            throw new IllegalArgumentException("filters cannot be null");
        }
        if (shortName == null) {
            throw new IllegalArgumentException("shortName cannot be null");
        }
        for (int i = 0; i < filters.size(); i++) {
            String actualName = filters.get(i).getClass().getSimpleName();
            // 直接匹配
            if (actualName.equals(shortName)) {
                return i;
            }
            // 处理 CGLIB 代理类名（如 "CorsFilter$$EnhancerBySpringCGLIB$$xxx"）
            if (actualName.startsWith(shortName + "$$")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 检查过滤器链中是否包含指定名称的过滤器。
     * <p>
     * 此方法同样受限于 {@link #getFilterIndex(List, String)} 中描述的限制。
     *
     * @param filters   过滤器列表，不能为 null
     * @param shortName 过滤器简单类名（不含包名），不能为 null
     * @return 如果包含返回 true，否则返回 false
     * @throws IllegalArgumentException 如果 filters 或 shortName 为 null
     */
    public static boolean containsFilter(List<Filter> filters, String shortName) {
        return getFilterIndex(filters, shortName) >= 0;
    }

    /**
     * 获取指定过滤器在链中的所有索引位置。
     * <p>
     * 当过滤器链中可能存在多个同名过滤器时，使用此方法获取所有匹配的索引。
     * <p>
     * <b>限制：</b>
     * <ul>
     *     <li>对于 Spring 代理类（如 CGLIB 代理），类名可能包含 $$EnhancerBySpringCGLIB$$ 等后缀</li>
     * </ul>
     *
     * @param filters   过滤器列表，不能为 null
     * @param shortName 过滤器简单类名（不含包名），不能为 null
     * @return 所有匹配索引的列表（按出现顺序），如果没有匹配则返回空列表
     * @throws IllegalArgumentException 如果 filters 或 shortName 为 null
     * @since 1.0.0
     */
    public static List<Integer> getAllFilterIndices(List<Filter> filters, String shortName) {
        if (filters == null) {
            throw new IllegalArgumentException("filters cannot be null");
        }
        if (shortName == null) {
            throw new IllegalArgumentException("shortName cannot be null");
        }
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < filters.size(); i++) {
            String actualName = filters.get(i).getClass().getSimpleName();
            // 直接匹配
            if (actualName.equals(shortName)) {
                indices.add(i);
            }
            // 处理 CGLIB 代理类名
            else if (actualName.startsWith(shortName + "$$")) {
                indices.add(i);
            }
        }
        return indices;
    }
}
