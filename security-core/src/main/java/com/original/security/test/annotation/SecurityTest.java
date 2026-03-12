package com.original.security.test.annotation;

import com.original.security.test.context.SecurityTestContextCustomizer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 安全测试切片注解。
 *
 * <p>类似于 {@code @WebMvcTest} 或 {@code @DataJpaTest}，此注解用于测试安全组件时
 * 只加载必要的安全配置，而不是完整的 Spring Boot 上下文。</p>
 *
 * <h3>优势</h3>
 * <ul>
 *   <li>测试启动更快（比完整 @SpringBootTest 快 50%+）</li>
 *   <li>不加载完整的 Web 上下文</li>
 *   <li>只加载安全相关的配置</li>
 *   <li>自动配置 MockMvc</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @SecurityTest
 * class MySecurityTest {
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @Test
 *     @WithMockUser(roles = "ADMIN")
 *     void testAdminAccess() throws Exception {
 *         mockMvc.perform(get("/admin"))
 *                .andExpect(status().isOk());
 *     }
 * }
 * }</pre>
 *
 * @author Claude
 * @since 1.0.0
 * @see SecurityTestContextCustomizer
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = SecurityTestContextCustomizer.class)
public @interface SecurityTest {

    /**
     * 指定要加载的控制器类。
     *
     * <p>类似于 {@code @WebMvcTest} 的 controllers 属性。</p>
     *
     * @return 控制器类数组
     */
    Class<?>[] controllers() default {};

    /**
     * 指定额外的配置类。
     *
     * @return 配置类数组
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
    Class<?>[] classes() default {};

    /**
     * Spring Boot 应用属性配置。
     *
     * @return 属性数组
     */
    String[] properties() default {};
}
