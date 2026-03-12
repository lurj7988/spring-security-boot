package com.original.security.test.context;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import com.original.security.test.annotation.SecurityTest;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 安全测试上下文定制器。
 *
 * <p>为 {@link SecurityTest} 注解定制测试上下文，只加载必要的安全配置。</p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>自动注册安全配置类</li>
 *   <li>可选的 MockMvc 自动配置</li>
 *   <li>禁用不必要的自动配置以加快启动</li>
 * </ul>
 *
 * @author Claude
 * @since 1.0.0
 * @see SecurityTest
 */
public class SecurityTestContextCustomizer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * 测试类存储，用于在无法从上下文获取时回退查找。
     */
    static final ThreadLocal<Class<?>> TEST_CLASS_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前测试类（由测试框架调用）。
     *
     * @param testClass 测试类
     */
    public static void setTestClass(Class<?> testClass) {
        TEST_CLASS_HOLDER.set(testClass);
    }

    /**
     * 清除当前测试类（测试完成后调用）。
     */
    static void clearTestClass() {
        TEST_CLASS_HOLDER.remove();
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // 获取 @SecurityTest 注解配置
        SecurityTest securityTest = findSecurityTestAnnotation(context);

        if (securityTest == null) {
            return;
        }

        // 注册必要的配置类
        registerConfigurations(context, securityTest);

        // 应用属性
        applyProperties(context, securityTest);
    }

    private void applyProperties(ConfigurableApplicationContext context, SecurityTest securityTest) {
        org.springframework.boot.test.util.TestPropertyValues.of(securityTest.properties())
                .applyTo(context.getEnvironment());

        // 默认属性
        org.springframework.boot.test.util.TestPropertyValues.of(
            "security.config.validation=false",
            "security.network.csrf.enabled=false",
            "security.network.cors.enabled=false",
            "security.jwt.secret=ZGVmYXVsdE1vY2tTZWNyZXRGb3JKd3RUZXN0aW5nUHVycG9zZXMxMjM0NTY3ODkw"
        ).applyTo(context.getEnvironment());
    }

    /**
     * 查找 @SecurityTest 注解。
     *
     * <p>使用多种策略查找测试类：</p>
     * <ol>
     *   <li>从 ThreadLocal 存储获取（最可靠）</li>
     *   <li>从 Spring TestContextManager 获取</li>
     *   <li>从堆栈跟踪推断（后备方案）</li>
     * </ol>
     *
     * @param context 应用上下文
     * @return SecurityTest 注解实例，如果未找到则返回 null
     */
    private SecurityTest findSecurityTestAnnotation(ConfigurableApplicationContext context) {
        Class<?> testClass = findTestClass(context);

        if (testClass == null) {
            return null;
        }

        return AnnotationUtils.findAnnotation(testClass, SecurityTest.class);
    }

    /**
     * 尝试查找测试类。
     *
     * @param context 应用上下文
     * @return 测试类，如果无法确定则返回 null
     */
    private Class<?> findTestClass(ConfigurableApplicationContext context) {
        // 策略1：从 ThreadLocal 获取（最可靠）
        Class<?> testClass = TEST_CLASS_HOLDER.get();
        if (testClass != null) {
            return testClass;
        }

        // 策略2：从 Spring TestContextManager 获取
        testClass = findTestClassFromTestContextManager(context);
        if (testClass != null) {
            return testClass;
        }

        // 策略3：从堆栈跟踪推断（后备方案）
        return findTestClassFromStackTrace(context);
    }

    /**
     * 从 Spring TestContextManager 获取测试类。
     */
    private Class<?> findTestClassFromTestContextManager(ConfigurableApplicationContext context) {
        try {
            // 尝试从上下文中获取 TestContextManager
            String[] beanNames = context.getBeanNamesForType(TestContextManager.class);
            if (beanNames.length > 0) {
                TestContextManager manager = context.getBean(beanNames[0], TestContextManager.class);
                // 通过反射获取测试类
                Method getTestClass = manager.getClass().getDeclaredMethod("getTestClass");
                getTestClass.setAccessible(true);
                return (Class<?>) getTestClass.invoke(manager);
            }
        } catch (Exception ignored) {
            // 忽略异常，使用后备方案
        }
        return null;
    }

    /**
     * 从堆栈跟踪推断测试类（后备方案）。
     */
    private Class<?> findTestClassFromStackTrace(ConfigurableApplicationContext context) {
        ClassLoader classLoader = context.getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            // 匹配常见测试类命名模式
            if (isTestClass(className)) {
                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    // 验证类上有 @SecurityTest 注解
                    if (AnnotationUtils.findAnnotation(clazz, SecurityTest.class) != null) {
                        return clazz;
                    }
                } catch (ClassNotFoundException ignored) {
                    // 继续查找
                } catch (NoClassDefFoundError ignored) {
                    // 继续查找（可能是内部类）
                }
            }
        }
        return null;
    }

    /**
     * 判断类名是否可能是测试类。
     */
    private boolean isTestClass(String className) {
        return className.endsWith("Test")
                || className.endsWith("Tests")
                || className.contains("Test$");
    }

    /**
     * 注册安全相关的配置类。
     *
     * @param context      应用上下文
     * @param securityTest 注解配置
     */
    private void registerConfigurations(ConfigurableApplicationContext context,
                                        SecurityTest securityTest) {
        if (context instanceof AnnotationConfigRegistry) {
            AnnotationConfigRegistry registry = (AnnotationConfigRegistry) context;

            // 注册核心安全配置
            registry.register(SecurityAutoConfiguration.class);

            // 注册用户指定的额外配置
            Class<?>[] extraClasses = securityTest.classes();
            if (extraClasses.length > 0) {
                registry.register(extraClasses);
            }

            // 注册控制器（如果指定）
            Class<?>[] controllers = securityTest.controllers();
            if (controllers.length > 0) {
                registry.register(controllers);
            }
        }
    }
}
