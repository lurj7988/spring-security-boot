# Spring Security Boot 插件开发指南

本指南帮助你理解 Spring Security Boot 框架的插件系统，并指导你开发自定义的认证插件和过滤器插件。

## 目录

- [概述](#概述)
- [认证插件开发](#认证插件开发)
- [过滤器插件开发](#过滤器插件开发)
- [完整示例：短信认证插件](#完整示例短信认证插件)
- [测试指南](#测试指南)
- [最佳实践](#最佳实践)
- [故障排查](#故障排查)

---

## 概述

### 什么是插件系统？

Spring Security Boot 采用插件化架构，允许开发者通过实现标准接口来扩展框架的认证能力。这种设计带来以下优势：

- **可扩展性**：轻松添加新的认证方式
- **解耦**：插件独立于核心框架，便于维护
- **灵活性**：可以组合多个插件满足复杂需求

### 插件类型

框架提供两种核心插件接口：

| 插件类型 | 接口 | 用途 |
|---------|------|------|
| 认证插件 | `AuthenticationPlugin` | 定义认证逻辑和认证提供者 |
| 过滤器插件 | `SecurityFilterPlugin` | 在过滤器链中添加自定义过滤器 |

### 插件生命周期

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring 容器启动                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  1. Spring 扫描 @Component 标注的插件实现类                   │
│  2. 通过构造器注入依赖                                        │
│  3. 按 @Order 注解排序                                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  框架自动配置阶段                                             │
│  - 收集所有 AuthenticationPlugin 实现                        │
│  - 收集所有 SecurityFilterPlugin 实现                        │
│  - 构建 SecurityFilterChain                                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  请求处理阶段                                                 │
│  - 过滤器链执行                                               │
│  - AuthenticationManager 调用匹配的 Provider                  │
│  - 认证成功/失败处理                                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 认证插件开发

### AuthenticationPlugin 接口

```java
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.lang.Nullable;

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
    @Nullable
    AuthenticationProvider getAuthenticationProvider();

    /**
     * 检查该插件是否支持指定的认证类型
     * @param authenticationType 认证类型
     * @return true 表示支持该认证类型
     */
    boolean supports(@Nullable Class<?> authenticationType);
}
```

### 方法详解

#### getName()

返回插件的唯一标识名称，用于日志和调试。

```java
@Override
public String getName() {
    return "sms-authentication";  // 使用 kebab-case 命名
}
```

#### getAuthenticationProvider()

返回底层的 `AuthenticationProvider` 实现。如果认证逻辑由其他机制（如过滤器）处理，可以返回 `null`。

```java
@Override
public AuthenticationProvider getAuthenticationProvider() {
    return smsAuthenticationProvider;  // 返回注入的 Provider
}
```

#### supports()

判断插件是否支持某种认证类型。框架会根据此方法选择合适的插件处理请求。

```java
@Override
public boolean supports(Class<?> authenticationType) {
    return authenticationType != null
        && SmsAuthenticationToken.class.isAssignableFrom(authenticationType);
}
```

### 开发步骤

1. **创建自定义 Authentication Token**

```java
public class SmsAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String phoneNumber;
    private final String verifyCode;

    public SmsAuthenticationToken(String phoneNumber, String verifyCode) {
        super(phoneNumber, verifyCode);
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
        setAuthenticated(false);
    }

    public SmsAuthenticationToken(Object principal, Object credentials,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.phoneNumber = (String) principal;
        this.verifyCode = null;
        setAuthenticated(true);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getVerifyCode() {
        return verifyCode;
    }
}
```

2. **创建 AuthenticationProvider**

建议同时实现 Spring Security 和框架自定义的接口。

> **⚠️ Import 规范注意：**
> 当需要同时实现两个同名接口时，遵循以下规范：
> - 使用 `import` 导入主要/常用的接口（Spring Security 的 AuthenticationProvider）
> - 仅在类声明的 `implements` 子句中对次要同名接口使用全包名
> - 详见 `_bmad-output/project-context.md` 中的 Import 语句规范

```java
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import com.original.security.core.authentication.AuthenticationResult;

@Component
public class SmsAuthenticationProvider implements
    AuthenticationProvider,  // 主要接口：使用 import 导入
    com.original.security.core.authentication.AuthenticationProvider {  // 次要接口：使用 FQN

    private final SmsVerifyCodeService smsVerifyCodeService;
    private final UserDetailsService userDetailsService;

    public SmsAuthenticationProvider(SmsVerifyCodeService smsVerifyCodeService,
                                     UserDetailsService userDetailsService) {
        this.smsVerifyCodeService = smsVerifyCodeService;
        this.userDetailsService = userDetailsService;
    }

    // 实现 Spring Security 接口
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
        // ... 验证逻辑 ...
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // 实现框架自定义接口
    @Override
    public AuthenticationResult authenticate(Object credentials, String authenticationType) {
        // ... 适配逻辑 ...
        return result;
    }

    // ... 其他接口方法 ...
}
```

3. **创建 Plugin 实现类**

```java
@Component
@Order(3)  // 控制插件优先级
public class SmsAuthenticationPlugin implements AuthenticationPlugin {

    public static final String PLUGIN_NAME = "sms-authentication";

    private final SmsAuthenticationProvider authenticationProvider;

    public SmsAuthenticationPlugin(SmsAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType != null
            && SmsAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
```

---

## 过滤器插件开发

### SecurityFilterPlugin 接口

```java
public interface SecurityFilterPlugin extends Ordered {
    /**
     * 过滤器位置类型
     */
    enum Position {
        BEFORE,  // 在目标过滤器之前
        AFTER,   // 在目标过滤器之后
        AT       // 在目标过滤器相同位置
    }

    String getName();
    Filter getFilter();
    default Position getPosition() { return Position.BEFORE; }
    default Class<? extends Filter> getTargetFilterClass() {
        return UsernamePasswordAuthenticationFilter.class;
    }
    default boolean isEnabled() { return true; }
    default int getOrder() { return 0; }
}
```

### 方法详解

#### getName()

返回插件的唯一标识名称，用于日志记录和调试。

```java
@Override
public String getName() {
    return "request-logging";  // 使用 kebab-case 命名
}
```

#### getFilter()

返回要注册到过滤器链的 Filter 实例。

> **⚠️ 重要：** 必须返回相同的 Filter 实例（单例），不要每次调用都创建新实例。

```java
// ✅ 正确：使用单例
private final Filter filter = new RequestLoggingFilter();

@Override
public Filter getFilter() {
    return filter;
}

// ❌ 错误：每次创建新实例
@Override
public Filter getFilter() {
    return new RequestLoggingFilter();  // 会导致过滤器重复注册
}
```

#### getPosition()

指定过滤器相对于目标过滤器的位置。默认为 `BEFORE`。

| 位置 | 说明 | 使用场景 |
|------|------|---------|
| `BEFORE` | 在目标过滤器之前执行 | 请求预处理、日志记录、限流 |
| `AFTER` | 在目标过滤器之后执行 | 响应后处理、清理工作、审计 |
| `AT` | 与目标过滤器同一位置 | 替换或增强默认过滤器 |

#### getTargetFilterClass()

指定目标过滤器类型。默认为 `UsernamePasswordAuthenticationFilter.class`。

常用的目标过滤器：

| 目标过滤器 | 用途 |
|-----------|------|
| `ChannelProcessingFilter.class` | 安全过滤器链最前面 |
| `UsernamePasswordAuthenticationFilter.class` | 表单登录位置（默认） |
| `BasicAuthenticationFilter.class` | HTTP Basic 认证位置 |
| `FilterSecurityInterceptor.class` | 授权检查之前 |

#### isEnabled()

控制插件是否启用。可通过配置动态控制：

```java
@Override
public boolean isEnabled() {
    return properties.isLoggingEnabled();
}
```

#### getOrder()

当有多个插件注册到同一位置时，控制它们的执行顺序。数值越小优先级越高。

### 开发步骤

1. **创建过滤器实现**

```java
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        // 记录请求信息
        log.info("[{}] {} {} from {}",
            requestId, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}] Completed in {}ms with status {}",
                requestId, duration, response.getStatus());
        }
    }
}
```

2. **创建 Plugin 实现类**

```java
@Component
@Order(1)  // 高优先级，确保最先执行
public class RequestLoggingFilterPlugin implements SecurityFilterPlugin {

    private static final String PLUGIN_NAME = "request-logging";

    // 使用单例过滤器实例
    private final Filter filter = new RequestLoggingFilter();

    @Override
    public String getName() {
        return PLUGIN_NAME;
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
        return ChannelProcessingFilter.class;  // 在安全过滤器链最前面
    }

    @Override
    public boolean isEnabled() {
        return true;  // 可通过配置控制
    }
}
```

---

## 完整示例：短信认证插件

以下是一个完整的短信认证插件示例，包含所有必要的组件。

### 1. 目录结构

```
examples/plugins/sms-auth-plugin/
├── src/main/java/com/example/security/plugin/sms/
│   ├── SmsAuthenticationPlugin.java      # 插件主类
│   ├── SmsAuthenticationProvider.java    # 认证提供者
│   ├── SmsAuthenticationToken.java       # 认证令牌
│   ├── SmsVerifyCodeService.java         # 验证码服务接口
│   └── config/
│       └── SmsAuthenticationConfig.java  # 自动配置类
├── src/test/java/com/example/security/plugin/sms/
│   ├── SmsAuthenticationPluginTest.java
│   └── SmsAuthenticationProviderTest.java
└── pom.xml
```

### 2. SmsAuthenticationToken.java

```java
package com.example.security.plugin.sms;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 短信验证码认证令牌。
 * <p>
 * 封装手机号和验证码信息，用于短信认证流程。
 * </p>
 *
 * @author Example Team
 * @since 1.0.0
 */
public class SmsAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final String phoneNumber;
    private final String verifyCode;

    /**
     * 创建未认证的令牌。
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     */
    public SmsAuthenticationToken(String phoneNumber, String verifyCode) {
        super(phoneNumber, verifyCode);
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
        setAuthenticated(false);
    }

    /**
     * 创建已认证的令牌。
     *
     * @param principal   用户主体
     * @param credentials 凭证
     * @param authorities 权限列表
     */
    public SmsAuthenticationToken(Object principal, Object credentials,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.phoneNumber = (String) principal;
        this.verifyCode = null;
        setAuthenticated(true);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    @Override
    public Object getPrincipal() {
        return phoneNumber;
    }

    @Override
    public Object getCredentials() {
        return verifyCode;
    }
}
```

### 3. SmsVerifyCodeService.java

```java
package com.example.security.plugin.sms;

/**
 * 短信验证码服务接口。
 * <p>
 * 定义验证码的生成、发送和验证逻辑。
 * 实际项目中需要实现此接口对接短信服务商。
 * </p>
 *
 * @author Example Team
 * @since 1.0.0
 */
public interface SmsVerifyCodeService {

    /**
     * 发送验证码到指定手机号。
     *
     * @param phoneNumber 手机号
     * @return 是否发送成功
     */
    boolean sendVerifyCode(String phoneNumber);

    /**
     * 验证验证码是否正确。
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     * @return 验证是否通过
     */
    boolean verifyCode(String phoneNumber, String verifyCode);

    /**
     * 获取验证码有效期（秒）。
     *
     * @return 有效期
     */
    default int getExpireSeconds() {
        return 300;  // 默认 5 分钟
    }
}
```

### 4. SmsAuthenticationProvider.java

```java
package com.example.security.plugin.sms;

import com.original.security.core.authentication.AuthenticationResult;
import com.original.security.core.authentication.user.SecurityUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短信验证码认证提供者。
 */
public class SmsAuthenticationProvider implements 
    AuthenticationProvider, 
    com.original.security.core.authentication.AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(SmsAuthenticationProvider.class);
    private final SmsVerifyCodeService smsVerifyCodeService;
    private final UserDetailsService userDetailsService;

    public SmsAuthenticationProvider(SmsVerifyCodeService smsVerifyCodeService,
                                     UserDetailsService userDetailsService) {
        this.smsVerifyCodeService = smsVerifyCodeService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
        String phone = token.getPhoneNumber();
        String code = token.getVerifyCode();

        if (!smsVerifyCodeService.verifyCode(phone, code)) {
            throw new BadCredentialsException("验证码错误");
        }

        UserDetails user = userDetailsService.loadUserByUsername(phone);
        SmsAuthenticationToken result = new SmsAuthenticationToken(
            user, null, user.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // 实现框架自定义接口以支持非 Spring Security 调用
    @Override
    public AuthenticationResult authenticate(Object credentials, String type) {
        // 适配逻辑...
        return AuthenticationResult.success(null, new HashMap<>());
    }
    
    // ... 其他 com.original.security.core.authentication.AuthenticationProvider 方法 ...
}
```

### 5. SmsAuthenticationPlugin.java

```java
package com.example.security.plugin.sms;

import com.original.security.plugin.AuthenticationPlugin;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Component;

/**
 * 短信验证码认证插件。
 * <p>
 * 实现 {@link AuthenticationPlugin} 接口，提供短信验证码认证能力。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 1. 实现 SmsVerifyCodeService 接口
 * // 2. 确保 UserDetailsService 支持手机号查询
 * // 3. 插件会自动注册到认证系统
 * }</pre>
 *
 * @author Example Team
 * @since 1.0.0
 * @see AuthenticationPlugin
 * @see SmsAuthenticationProvider
 */
@Component
@Order(3)
public class SmsAuthenticationPlugin implements AuthenticationPlugin {

    public static final String PLUGIN_NAME = "sms-authentication";

    private final SmsAuthenticationProvider authenticationProvider;

    /**
     * 构造短信认证插件。
     *
     * @param authenticationProvider 短信认证提供者
     */
    public SmsAuthenticationPlugin(SmsAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType != null
            && SmsAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
```

---

## 测试指南

### 单元测试

测试插件的基本功能：

```java
package com.example.security.plugin.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsAuthenticationProviderTest {

    @Mock
    private SmsVerifyCodeService smsVerifyCodeService;

    @Mock
    private UserDetailsService userDetailsService;

    private SmsAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SmsAuthenticationProvider(smsVerifyCodeService, userDetailsService);
    }

    @Test
    @DisplayName("测试支持 SmsAuthenticationToken 类型")
    void testSupports_ValidToken_ReturnsTrue() {
        assertTrue(provider.supports(SmsAuthenticationToken.class));
    }

    @Test
    @DisplayName("测试不支持其他 Token 类型")
    void testSupports_InvalidToken_ReturnsFalse() {
        assertFalse(provider.supports(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("测试认证成功")
    void testAuthenticate_ValidCode_ReturnsAuthenticatedToken() {
        // Given
        String phone = "13800138000";
        String code = "123456";
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);

        UserDetails user = new User(phone, "", Collections.emptyList());

        when(smsVerifyCodeService.verifyCode(phone, code)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(phone)).thenReturn(user);

        // When
        Authentication result = provider.authenticate(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(phone, result.getPrincipal());
    }

    @Test
    @DisplayName("测试验证码错误抛出异常")
    void testAuthenticate_InvalidCode_ThrowsException() {
        // Given
        String phone = "13800138000";
        String code = "wrong";
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);

        when(smsVerifyCodeService.verifyCode(phone, code)).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(token));
    }
}
```

### 集成测试

测试插件与 Spring Security 的集成：

```java
package com.example.security.plugin.sms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SmsAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSmsLogin_Success() throws Exception {
        // 模拟短信登录请求
        mockMvc.perform(post("/api/auth/sms")
                .param("phone", "13800138000")
                .param("code", "123456"))
            .andExpect(status().isOk());
    }
}
```

---

## 最佳实践

### 性能优化

1. **使用单例过滤器实例**

```java
// ✅ 正确：使用单例
private final Filter filter = new MyFilter();

@Override
public Filter getFilter() {
    return filter;
}

// ❌ 错误：每次调用创建新实例
@Override
public Filter getFilter() {
    return new MyFilter();  // 会导致重复过滤器
}
```

2. **避免重复的数据库查询**

```java
// 使用缓存优化权限加载
@Override
public Authentication authenticate(Authentication authentication) {
    String username = authentication.getName();

    // 先检查缓存
    UserDetails cached = userCache.get(username);
    if (cached != null) {
        return createAuthenticatedToken(cached);
    }

    // 缓存未命中，查询数据库
    UserDetails user = userDetailsService.loadUserByUsername(username);
    userCache.put(username, user);

    return createAuthenticatedToken(user);
}
```

### 安全注意事项

1. **敏感数据脱敏**

```java
// 日志中不记录敏感信息
log.info("User login: {}", maskPhone(phoneNumber));
log.debug("Token generated: {}", token.substring(0, 10) + "...");
```

2. **防止暴力破解**

```java
// 添加登录失败计数
if (loginAttemptService.isBlocked(phoneNumber)) {
    throw new LockedException("账户已锁定，请稍后重试");
}
```

3. **验证码安全**

```java
// 限制验证码尝试次数
if (smsVerifyCodeService.getAttemptCount(phoneNumber) > 5) {
    throw new BadCredentialsException("验证码尝试次数过多");
}
```

### 错误处理

1. **使用统一的错误码**

```java
public enum AuthErrorCode {
    INVALID_VERIFY_CODE("AUTH_001", "验证码错误"),
    EXPIRED_VERIFY_CODE("AUTH_002", "验证码已过期"),
    USER_NOT_FOUND("AUTH_003", "用户不存在"),
    ACCOUNT_LOCKED("AUTH_004", "账户已锁定");
}
```

2. **提供清晰的错误信息**

```java
// ✅ 正确：用户友好的错误信息
throw new BadCredentialsException("验证码错误或已过期，请重新获取");

// ❌ 错误：暴露技术细节
throw new BadCredentialsException("SQL query failed: SELECT * FROM users...");
```

---

## 故障排查

### 常见问题

#### 1. 插件没有被注册

**症状**：自定义插件没有被框架识别

**排查步骤**：
1. 确认类上有 `@Component` 注解
2. 确认包在 Spring 扫描路径内
3. 检查是否有多个 `@ComponentScan` 配置

```java
// 确保插件类被 Spring 管理
@Component
@Order(3)
public class MyAuthenticationPlugin implements AuthenticationPlugin {
    // ...
}
```

#### 2. supports() 方法返回 false

**症状**：插件存在但认证请求不被处理

**排查步骤**：
1. 检查 `supports()` 方法的实现逻辑
2. 确认 Token 类型匹配
3. 添加调试日志

```java
@Override
public boolean supports(Class<?> authenticationType) {
    boolean result = authenticationType != null
        && MyToken.class.isAssignableFrom(authenticationType);
    log.debug("Supports check for {}: {}", authenticationType, result);
    return result;
}
```

#### 3. 过滤器顺序错误

**症状**：过滤器执行顺序不符合预期

**排查步骤**：
1. 检查 `@Order` 注解值
2. 检查 `getOrder()` 方法返回值
3. 检查 `getPosition()` 和 `getTargetFilterClass()` 配置

### 调试技巧

1. **启用调试日志**

```properties
logging.level.com.original.security=DEBUG
logging.level.org.springframework.security=DEBUG
```

2. **使用断点调试**

在以下位置设置断点：
- `AuthenticationPlugin.supports()`
- `AuthenticationProvider.authenticate()`
- `SecurityFilterPlugin.getFilter()`

3. **检查已注册的插件**

```java
@Autowired
private List<AuthenticationPlugin> authenticationPlugins;

@PostConstruct
public void listPlugins() {
    log.info("Registered authentication plugins:");
    authenticationPlugins.forEach(p -> log.info("  - {} (order: {})",
        p.getName(), p.getOrder()));
}
```

---

## 相关文档

- [快速开始](quick-start.md) - 框架快速集成指南
- [配置参考](configuration.md) - 完整配置项说明
- [API 参考](api.md) - 端点和响应格式
- [故障排查](troubleshooting.md) - 常见问题解决

---

**最后更新**: 2026-03-20
