# 测试支持工具文档

本文档介绍 spring-security-boot 框架提供的测试支持工具，帮助开发者快速编写安全测试用例。

## 目录

1. [快速开始](#快速开始)
2. [@WithMockUser 增强注解](#withmockuser-增强注解)
3. [@SecurityTest 测试切片](#securitytest-测试切片)
4. [AuthenticationTestUtils 工具类](#authenticationtestutils-工具类)
5. [AbstractSecurityTest 测试基类](#abstractsecuritytest-测试基类)
6. [最佳实践](#最佳实践)
7. [常见问题](#常见问题)

---

## 快速开始

### 添加依赖

框架已包含测试支持工具。在你的项目中添加以下依赖：

```xml
<!-- 测试时需要添加 spring-security-test 依赖 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 示例：5分钟上手

```java
import com.original.security.test.annotation.WithMockUser;
import com.original.security.test.util.AuthenticationTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/users"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testUserCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/users"))
               .andExpect(status().isForbidden());
    }
}
```

---

## @WithMockUser 增强注解

`@WithMockUser` 是基于 Spring Security Test 的增强版注解，提供更灵活的用户配置能力。

### 基本用法

```java
@Test
@WithMockUser(username = "admin", roles = {"ADMIN"})
void testWithAdminRole() {
    // 测试代码，当前用户为 admin，拥有 ADMIN 角色
}
```

### 增强功能

#### 1. 同时配置 roles 和 authorities

```java
@Test
@WithMockUser(
    username = "user",
    roles = {"USER"},
    authorities = {"user:read", "user:write"}
)
void testWithRolesAndAuthorities() {
    // 用户同时拥有 ROLE_USER 角色和 user:read, user:write 权限
}
```

#### 2. 账户状态配置

```java
@Test
@WithMockUser(
    username = "disabled",
    enabled = false
)
void testDisabledAccount() {
    // 测试禁用账户
}

@Test
@WithMockUser(
    username = "locked",
    accountNonLocked = false
)
void testLockedAccount() {
    // 测试锁定账户
}
```

#### 3. 自定义 UserDetails

```java
@Test
@WithMockUser(
    userDetailsClass = "com.example.MyCustomUserDetails"
)
void testWithCustomUserDetails() {
    // 使用自定义 UserDetails 实现
}
```

### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `username` | String | "user" | 用户名 |
| `password` | String | "password" | 密码 |
| `roles` | String[] | {} | 角色列表（自动添加 ROLE_ 前缀） |
| `authorities` | String[] | {} | 权限列表（不添加前缀） |
| `enabled` | boolean | true | 账户是否启用 |
| `accountNonExpired` | boolean | true | 账户是否未过期 |
| `accountNonLocked` | boolean | true | 账户是否未锁定 |
| `credentialsNonExpired` | boolean | true | 凭证是否未过期 |
| `userDetailsClass` | String | "" | 自定义 UserDetails 类名 |
| `setupBefore` | TestExecutionEvent | TEST_METHOD | 安全上下文设置时机 |

---

## @SecurityTest 测试切片

`@SecurityTest` 是类似 `@WebMvcTest` 的测试切片注解，只加载安全相关配置，提高测试启动速度。

### 使用示例

```java
@SecurityTest
class MySecuritySliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess() throws Exception {
        mockMvc.perform(get("/admin"))
               .andExpect(status().isOk());
    }
}
```

### 指定控制器

```java
@SecurityTest(controllers = {UserController.class, AdminController.class})
class ControllerSecurityTest {
    // 只加载指定的控制器
}
```

### 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `controllers` | Class<?>[] | {} | 要加载的控制器类 |
| `classes` | Class<?>[] | {} | 额外的配置类 |
| `properties` | String[] | 见下方 | Spring Boot 属性配置 |

**注意**: `@SecurityTest` 自动配置 MockMvc（通过 `@AutoConfigureMockMvc` 元注解），无需额外配置。

默认属性：
- `security.config.validation=false`
- `security.network.csrf.enabled=false`
- `security.network.cors.enabled=false`

---

## AuthenticationTestUtils 工具类

`AuthenticationTestUtils` 提供静态方法来创建和管理测试认证对象。

### 快速 Mock

```java
// 创建简单认证（自动设置到 SecurityContext）
Authentication auth = AuthenticationTestUtils.mockAuthentication("admin", "ADMIN", "USER");

// 验证
assertEquals("admin", auth.getName());
assertTrue(AuthenticationTestUtils.hasRole("ADMIN"));
```

### 构建器模式

```java
// 使用构建器创建复杂认证
Authentication auth = AuthenticationTestUtils.withUser("admin")
    .password("secret")
    .roles("ADMIN", "USER")
    .authorities("user:read", "user:write")
    .enabled(true)
    .buildAuthentication();

// 设置到 SecurityContext
AuthenticationTestUtils.withUser("admin")
    .roles("ADMIN")
    .setupInContext();
```

### Mock JWT Token

```java
// 创建 Mock JWT Token（仅用于测试）
String token = AuthenticationTestUtils.mockJwtToken("admin", "ADMIN", "USER");
// 返回格式：header.payload.signature
```

### 工具方法

```java
// 获取当前用户名
String username = AuthenticationTestUtils.getCurrentUsername();

// 检查角色
boolean isAdmin = AuthenticationTestUtils.hasRole("ADMIN");

// 检查权限
boolean canRead = AuthenticationTestUtils.hasAuthority("user:read");

// 清除认证
AuthenticationTestUtils.clearAuthentication();
```

---

## AbstractSecurityTest 测试基类

继承 `AbstractSecurityTest` 可以使用便捷的测试辅助方法。

### 使用示例

```java
import com.original.security.test.AbstractSecurityTest;

class MyTest extends AbstractSecurityTest {

    @Test
    void testAdminFunctionality() {
        // 设置管理员认证
        withAdmin();

        // 断言角色
        assertHasRole("ADMIN");
        assertAuthenticated();
    }

    @Test
    void testUserFunctionality() {
        // 设置普通用户认证
        withUser("testuser", "USER", "GUEST");

        // 断言
        assertHasRole("USER");
        assertDoesNotHaveRole("ADMIN");
    }
}
```

### 提供的方法

| 方法 | 说明 |
|------|------|
| `withAdmin()` | 设置默认管理员（admin/ADMIN） |
| `withAdmin(username, roles...)` | 设置自定义管理员 |
| `withUser()` | 设置默认用户（user/USER） |
| `withUser(username, roles...)` | 设置自定义用户 |
| `withAuthorities(username, authorities...)` | 设置带权限的用户 |
| `assertHasRole(role)` | 断言拥有角色 |
| `assertDoesNotHaveRole(role)` | 断言不拥有角色 |
| `assertHasAuthority(authority)` | 断言拥有权限 |
| `assertAuthenticated()` | 断言已认证 |
| `assertNotAuthenticated()` | 断言未认证 |
| `getCurrentUsername()` | 获取当前用户名 |

---

## 最佳实践

### 1. 测试隔离

每个测试后清理安全上下文：

```java
@AfterEach
void tearDown() {
    AuthenticationTestUtils.clearAuthentication();
}
```

使用 `AbstractSecurityTest` 基类会自动处理清理。

### 2. 使用正确的注解

- **单元测试**：使用 `AuthenticationTestUtils` 直接设置认证
- **集成测试**：使用 `@WithMockUser` 注解
- **切片测试**：使用 `@SecurityTest` 注解

### 3. 测试覆盖

确保测试覆盖以下场景：
- 正常认证用户访问
- 未认证用户访问
- 权限不足的用户访问
- 账户状态异常（禁用、锁定、过期）

### 4. 性能考虑

- 使用 `@SecurityTest` 替代 `@SpringBootTest` 可以提高测试速度
- 避免在每个测试中重复创建 MockMvc

### 5. 命名规范

遵循测试命名规范：

```java
// 格式：test{方法名}_{场景}_{预期结果}
@Test
@WithMockUser(roles = "ADMIN")
void testGetAllUsers_AsAdmin_ReturnsOk() { }

@Test
@WithMockUser(roles = "USER")
void testGetAllUsers_AsUser_ReturnsForbidden() { }

@Test
void testGetAllUsers_Unauthenticated_ReturnsUnauthorized() { }
```

---

## 常见问题

### Q1: @WithMockUser 不生效？

确保：
1. 测试类使用了 `@SpringBootTest` 或 `@WebMvcTest`
2. 添加了 `spring-security-test` 依赖
3. 方法或类上有 `@WithMockUser` 注解

### Q2: 测试启动太慢？

使用 `@SecurityTest` 替代 `@SpringBootTest`：

```java
// 慢
@SpringBootTest
@AutoConfigureMockMvc
class SlowTest { }

// 快
@SecurityTest
class FastTest { }
```

### Q3: 如何测试自定义 UserDetails？

方法1：使用 `userDetailsClass` 属性
```java
@WithMockUser(userDetailsClass = "com.example.MyUserDetails")
```

方法2：使用构建器模式
```java
MyUserDetails userDetails = new MyUserDetails("admin", "ADMIN");
Authentication auth = new UsernamePasswordAuthenticationToken(
    userDetails, null, userDetails.getAuthorities());
SecurityContextHolder.getContext().setAuthentication(auth);
```

### Q4: 如何测试 @PreAuthorize 注解？

确保启用了方法级安全：

```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig { }
```

然后使用：
```java
@Test
@WithMockUser(authorities = "user:read")
void testPreAuthorize() {
    // 测试 @PreAuthorize("hasAuthority('user:read')")
}
```

---

## 版本信息

- **框架版本**: 1.0.0
- **Spring Boot**: 2.7.18
- **Spring Security**: 5.7.11
- **JUnit**: 5 (Jupiter)

---

*最后更新: 2026-03-11*
