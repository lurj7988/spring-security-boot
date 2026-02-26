# 核心接口规范文档

**版本:** 0.1.0
**日期:** 2026-02-25
**模块:** security-core

---

## 概述

本文档定义了 Spring Security Boot 框架的两个核心接口：
- `AuthenticationPlugin` - 认证插件接口，用于扩展框架的认证能力
- `ConfigProvider` - 配置提供者接口，用于解耦配置源与配置消费者

---

## 1. AuthenticationPlugin 接口

### 1.1 接口定义

```java
package com.original.security.plugin;

import org.springframework.security.authentication.AuthenticationProvider;

public interface AuthenticationPlugin {
    String getName();
    AuthenticationProvider getAuthenticationProvider();
    boolean supports(Class<?> authenticationType);
}
```

### 1.2 方法说明

#### `String getName()`
- **描述:** 获取认证插件的唯一名称
- **返回值:** 插件名称（不能为 null 或空字符串）
- **命名约定:** 小写字母、数字和连字符的组合，如 "jwt"、"username-password"、"sms-code"

#### `AuthenticationProvider getAuthenticationProvider()`
- **描述:** 获取此插件提供的认证提供者实例
- **返回值:** Spring Security 的 AuthenticationProvider 实例（不能为 null）
- **用途:** 返回的实例将被注册到 AuthenticationManager 中

#### `boolean supports(Class<?> authenticationType)`
- **描述:** 判断此插件是否支持指定的认证类型
- **参数:** authenticationType - 要检查的认证类型（Class 对象）
- **返回值:** 如果支持此认证类型返回 true，否则返回 false

### 1.3 使用示例

```java
@Component
@Order(1)
public class JwtAuthenticationPlugin implements AuthenticationPlugin {

    private final JwtAuthenticationProvider authenticationProvider;

    public JwtAuthenticationPlugin(JwtAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public String getName() {
        return "jwt";
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return JwtAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
```

### 1.4 生命周期管理

| 注解 | 用途 |
|------|------|
| `@Component` | 将插件注册为 Spring Bean，自动发现和管理 |
| `@Order` | 控制插件优先级，数值越小优先级越高 |

### 1.5 扩展开发指南

1. **实现接口:** 创建类实现 `AuthenticationPlugin` 接口
2. **添加依赖:** 使用构造器依赖注入获取所需的 `AuthenticationProvider`
3. **定义名称:** 选择唯一的插件名称
4. **实现支持检查:** 在 `supports()` 方法中判断支持的认证类型
5. **注册组件:** 添加 `@Component` 注解让 Spring 自动发现
6. **设置优先级:** 使用 `@Order` 注解控制执行顺序

---

## 2. ConfigProvider 接口

### 2.1 接口定义

```java
package com.original.security.config;

import java.util.Map;
import java.util.Properties;

public interface ConfigProvider {
    String getConfig(String key);
    Properties getProperties(String prefix);
    default Map<String, String> getAllConfig();
    default String getConfig(String key, String defaultValue);
}
```

### 2.2 方法说明

#### `String getConfig(String key)`
- **描述:** 获取指定键的配置值
- **参数:** key - 配置键（不能为 null）
- **返回值:** 配置值，如果不存在返回 null

#### `Properties getProperties(String prefix)`
- **描述:** 获取指定前缀下的所有配置
- **参数:** prefix - 配置键前缀
- **返回值:** 包含所有匹配配置的 Properties 对象
- **注意:** 返回的配置键已被去除前缀部分

#### `default Map<String, String> getAllConfig()`
- **描述:** 获取所有配置的只读映射视图
- **返回值:** 不可修改的 Map

#### `default String getConfig(String key, String defaultValue)`
- **描述:** 获取指定键的配置值，如果不存在返回默认值
- **参数:** key - 配置键，defaultValue - 默认值
- **返回值:** 配置值或默认值

### 2.3 使用示例

#### 2.3.1 基于数据库的配置提供者

```java
@Component
public class DatabaseConfigProvider implements ConfigProvider {

    private final ConfigRepository configRepository;

    public DatabaseConfigProvider(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public String getConfig(String key) {
        return configRepository.findByKey(key)
            .map(ConfigEntity::getValue)
            .orElse(null);
    }

    @Override
    public Properties getProperties(String prefix) {
        Properties props = new Properties();
        List<ConfigEntity> configs = configRepository.findByKeyPrefix(prefix);
        configs.forEach(config ->
            props.setProperty(
                config.getKey().substring(prefix.length()),
                config.getValue()
            ));
        return props;
    }
}
```

#### 2.3.2 基于配置文件的提供者

```java
@Component
@Order(1)
public class FileConfigProvider implements ConfigProvider {

    private final Environment environment;

    public FileConfigProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getConfig(String key) {
        return environment.getProperty(key);
    }

    @Override
    public Properties getProperties(String prefix) {
        Properties props = new Properties();
        // 从 Environment 中提取匹配前缀的配置
        // 实现略...
        return props;
    }
}
```

### 2.4 配置键命名约定

- **格式:** 使用点号分隔的层次结构，如 `security.jwt.secret`
- **前缀:** 用于分组，如 `security.jwt.`、`security.auth.`
- **示例:**
  - `security.jwt.secret` - JWT 密钥
  - `security.jwt.expiration` - JWT 过期时间
  - `security.auth.session.timeout` - 会话超时时间

### 2.5 配置优先级

当多个 `ConfigProvider` 存在时，使用 `@Order` 注解控制优先级：
- 数值越小优先级越高
- 高优先级的配置会覆盖低优先级的配置
- 建议配置文件优先级高于数据库配置

---

## 3. 集成使用示例

### 3.1 自动配置类

```java
@Configuration
public class SecurityPluginAutoConfiguration {

    private final List<AuthenticationPlugin> plugins;
    private final List<ConfigProvider> configProviders;

    public SecurityPluginAutoConfiguration(
            List<AuthenticationPlugin> plugins,
            List<ConfigProvider> configProviders) {
        this.plugins = plugins;
        this.configProviders = configProviders;
    }

    @PostConstruct
    public void initializePlugins() {
        // 按优先级排序并注册认证提供者
        plugins.stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(plugin -> {
                log.info("Registering authentication plugin: {}", plugin.getName());
                // 注册逻辑...
            });
    }
}
```

### 3.2 使用配置提供者

```java
@Service
public class JwtService {

    private final ConfigProvider configProvider;

    public JwtService(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public String generateToken(User user) {
        // 获取 JWT 配置
        Properties jwtConfig = configProvider.getProperties("security.jwt.");
        String secret = jwtConfig.getProperty("secret");
        long expiration = Long.parseLong(jwtConfig.getProperty("expiration"));

        // 生成 Token...
    }
}
```

---

## 4. 技术约束

| 项目 | 版本/要求 |
|------|----------|
| Java | 1.8+ |
| Spring Boot | 2.7.18 |
| Spring Security | 5.7.11 |
| 命名空间 | javax.* |
| 依赖注入 | 100% 构造器依赖注入 |

---

## 5. 最佳实践

1. **使用构造器依赖注入** - 禁止字段注入
2. **添加完整的 JavaDoc** - 公共 API 必须有文档
3. **处理 null 输入** - 在接口实现中妥善处理边界情况
4. **使用有意义的命名** - 插件名称和配置键应具有描述性
5. **设置合理的优先级** - 使用 `@Order` 控制执行顺序
6. **实现 equals/hashCode** - 如需在集合中使用，实现正确的相等性判断

---

## 6. 相关文档

- [Spring Security Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/)
- [Spring Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [项目架构文档](_bmad-output/planning-artifacts/architecture.md)
- [需求文档](_bmad-output/planning-artifacts/prd.md)

---

**变更历史:**

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| 0.1.0 | 2026-02-25 | 初始版本，定义 AuthenticationPlugin 和 ConfigProvider 接口 |
