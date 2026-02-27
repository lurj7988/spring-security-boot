package com.original.security.annotation;

import com.original.security.config.SecurityAutoConfiguration;
import com.original.security.config.SecurityDeferredImportSelector;
import com.original.security.config.SecurityConfigurationValidator;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 Spring Security Boot 核心自动配置。
 * <p>
 * 在 Spring Boot 应用的入口类上添加此注解，将自动：
 * <ul>
 *     <li>注册 {@link org.springframework.security.crypto.password.PasswordEncoder} (基于 BCrypt)</li>
 *     <li>注册 {@link org.springframework.security.authentication.AuthenticationManager}</li>
 *     <li>装配极简基础能力的 {@link org.springframework.security.web.SecurityFilterChain}</li>
 *     <li>加载并启用 {@link SecurityConfigurationValidator} 执行启动时的必需配置项检查</li>
 * </ul>
 * 
 * 使用示例：
 * <pre>
 * &#064;SpringBootApplication
 * &#064;EnableSecurityBoot
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * </pre>
 * 
 * 当应用中显式定义了相同类型的 Bean (例如自定义的 {@code PasswordEncoder}) 时，
 * 自动配置将会谦让，优先使用开发者的配置。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({SecurityConfigurationValidator.class, SecurityDeferredImportSelector.class})
public @interface EnableSecurityBoot {
}
