# 核心接口规范文档

## 文档信息
- **项目名称**: Spring Security Boot 框架
- **文档版本**: 1.0.0
- **生成日期**: 2026-02-26
- **作者**: Original Security Team

---

## 1. AuthenticationPlugin 接口规范

### 1.1 接口概述

`AuthenticationPlugin` 是插件化认证系统的核心接口，允许用户自定义认证逻辑并通过 Spring Security 框架集成。

### 1.2 包路径
```java
package com.original.security.plugin;
```

### 1.3 接口定义

```java
public interface AuthenticationPlugin {
    /**
     * 获取认证插件的名称
     * @return 插件名称，用于标识不同的认证实现
     */
    String getName();

    /**
     * 获取认证提供者实例
     * @return AuthenticationProvider 实现，提供实际的认证逻辑
     */
    AuthenticationProvider getAuthenticationProvider();

    /**
     * 检查该插件是否支持指定的认证类型
     * @param authenticationType 认证类型
     * @return true 表示支持该认证类型，false 表示不支持
     */
    boolean supports(Class<?> authenticationType);
}
```

### 1.4 使用示例

#### 基本使用
```java
// 创建认证插件
AuthenticationPlugin plugin = new DefaultAuthenticationPlugin(
    "JWTAuthenticationPlugin",
    new JwtAuthenticationProvider()
);

// 检查是否支持 JWT 认证
boolean supportsJwt = plugin.supports(UsernamePasswordAuthenticationToken.class);

// 获取认证提供者
AuthenticationProvider provider = plugin.getAuthenticationProvider();
```

#### 自定义实现
```java
public class CustomAuthenticationPlugin implements AuthenticationPlugin {
    private final String name;
    private final AuthenticationProvider provider;

    public CustomAuthenticationPlugin(String name, AuthenticationProvider provider) {
        this.name = name;
        this.provider = provider;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return provider;
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        // 自定义认证类型检查逻辑
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
```

### 1.5 设计要点

1. **构造器依赖注入**: 所有实现类必须使用构造器注入，不允许使用字段注入
2. **插件名称**: 每个插件必须有唯一的名称标识
3. **类型支持**: 通过 `supports` 方法明确支持哪些认证类型

---

## 2. ConfigProvider 接口规范

### 2.1 接口概述

`ConfigProvider` 提供统一的配置访问抽象，支持从多种配置源（数据库、配置文件等）加载配置。

### 2.2 包路径
```java
package com.original.security.config;
```

### 2.3 接口定义

```java
public interface ConfigProvider {
    /**
     * 获取指定键的配置值
     * @param <T> 配置值类型
     * @return 包含配置值的 Optional，如果不存在则为空
     */
    <T> Optional<T> getConfig(String key);

    /**
     * 获取指定键的配置值，如果不存在则返回默认值
     * @param <T> 配置值类型
     * @return 配置值或默认值
     */
    <T> T getConfig(String key, T defaultValue);

    /**
     * 获取指定前缀的所有配置属性
     * @return 包含配置属性的 Map，键为配置键，值为配置值
     */
    Map<String, Object> getProperties(String prefix);

    /**
     * 获取所有配置属性
     * @return 包含所有配置属性的 Map
     */
    Map<String, Object> getAllProperties();

    /**
     * 检查是否存在指定的配置键
     * @return true 表示存在该配置键
     */
    boolean hasConfig(String key);

    /**
     * 检查配置值是否满足指定条件
     * @return true 表示配置值存在且满足条件
     */
    <T> boolean checkConfig(String key, Predicate<T> condition);

    /**
     * 获取配置值的原始字符串形式
     * @return 配置值的字符串形式
     */
    String getString(String key);

    /**
     * 获取配置值并转换为指定类型
     * @return 转换后的配置值
     */
    <T> T getConfigAs(String key, Class<T> type);

    /**
     * 刷新配置缓存
     */
    void refresh();

    /**
     * 获取配置源的信息
     * @return 配置源描述信息
     */
    String getSourceInfo();
}
```

### 2.4 使用示例

#### 基本使用
```java
// 创建配置提供者
ConfigProvider config = new DefaultConfigProvider();
config.addConfig("app.name", "Spring Security Boot");
config.addConfig("app.version", "1.0.0");

// 获取配置值
Optional<String> appName = config.getConfig("app.name");
String version = config.getConfig("app.version", "1.0.0");

// 获取带前缀的配置
Map<String, Object> appConfig = config.getProperties("app.");

// 检查配置存在性
boolean hasName = config.hasConfig("app.name");

// 条件检查
boolean isDev = config.checkConfig("env", env -> "dev".equals(env));
```

#### 从属性文件加载
```java
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesConfigProvider implements ConfigProvider {
    private final Properties properties;
    private final String sourceInfo;

    public PropertiesConfigProvider(String filePath) throws IOException {
        this.properties = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
        }
        this.sourceInfo = "Properties file: " + filePath;
    }

    @Override
    public <T> Optional<T> getConfig(String key) {
        String value = properties.getProperty(key);
        return value != null ? Optional.of((T) value) : Optional.empty();
    }

    @Override
    public String getSourceInfo() {
        return sourceInfo;
    }

    // 实现其他方法...
}
```

#### 从数据库加载
```java
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConfigProvider implements ConfigProvider {
    private final DataSource dataSource;
    private final Map<String, Object> cache = new HashMap<>();

    public DatabaseConfigProvider(DataSource dataSource) {
        this.dataSource = dataSource;
        refresh();
    }

    @Override
    public void refresh() {
        Map<String, String> newConfig = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT config_key, config_value FROM app_config");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                newConfig.put(rs.getString("config_key"), rs.getString("config_value"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load config from database", e);
        }
        cache.clear();
        cache.putAll(newConfig);
    }

    @Override
    public <T> Optional<T> getConfig(String key) {
        String value = (String) cache.get(key);
        return value != null ? Optional.of((T) value) : Optional.empty();
    }

    // 实现其他方法...
}
```

### 2.5 设计要点

1. **类型安全**: 支持泛型配置值获取
2. **默认值支持**: 提供 `getConfig` 方法支持默认值
3. **条件检查**: 支持基于条件的配置值验证
4. **多种配置源**: 可以从文件、数据库、内存等多种来源加载配置
5. **缓存机制**: 支持 `refresh` 方法刷新配置缓存

---

## 3. 依赖关系图

```
AuthenticationPlugin
    ↓ (contains)
AuthenticationProvider
    ↓ (uses)
AuthenticationResult
AuthenticationException
SecurityUser
Token

ConfigProvider (separate, independent)
    ↓ (uses)
DefaultConfigProvider
    (can extend to other implementations)
```

---

## 4. 最佳实践

### 4.1 AuthenticationPlugin 最佳实践

1. **插件命名**: 使用有意义的名称，如 `JWTAuthenticationPlugin`、`DatabaseAuthenticationPlugin`
2. **类型检查**: 在 `supports` 方法中实现严格的类型检查
3. **异常处理**: 在认证过程中使用 `AuthenticationException` 抛出具体错误

### 4.2 ConfigProvider 最佳实践

1. **配置前缀**: 使用统一的配置前缀，如 `app.`、`security.`、`database.`
2. **类型转换**: 使用 `getConfigAs` 方法进行类型安全的配置值转换
3. **性能考虑**: 对于频繁访问的配置，考虑添加缓存机制

---

## 5. 注意事项

1. **线程安全**: 所有接口实现都需要考虑线程安全问题
2. **内存管理**: 大量配置项时要注意内存使用
3. **配置更新**: 对于动态配置，需要实现合适的更新机制
4. **错误处理**: 配置加载失败时需要有合适的错误处理机制

---

## 6. 扩展建议

### 6.1 AuthenticationPlugin 扩展

1. **插件生命周期**: 可以添加 `init()`、`destroy()` 方法管理插件生命周期
2. **插件优先级**: 支持插件优先级设置，解决多个插件的冲突
3. **插件配置**: 支持插件自身的配置管理

### 6.2 ConfigProvider 扩展

1. **配置监听**: 支持配置变更监听机制
2. **配置验证**: 添加配置值验证功能
3. **多源合并**: 支持多配置源的合并策略