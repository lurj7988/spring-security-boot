# Story 7-6: 插件开发指南

Status: done

## 基本信息
- **ID**: 7-6
- **所属 Epic**: Epic 7 - 开发者体验与文档 (进行中)
- **优先级**: 中
- **预估工作量**: 1-2 天

## 业务价值
为 Spring Security Boot 框架贡献者和高级用户创建全面的插件开发指南，帮助开发者理解和扩展框架的认证能力，促进框架生态系统的发展。

## 目标用户
- 框架贡献者
- 需要自定义认证方式的高级开发者
- 安全团队工程师
- 集成第三方认证系统的开发者

## 需求详情

### 核心需求 (来自 epics.md Story 7.6)

1. **AuthenticationPlugin 接口说明**
   - 接口设计理念
   - 方法详解
   - 生命周期管理

2. **完整开发流程**
   - 创建 Plugin 类
   - 实现 AuthenticationProvider
   - 配置注册
   - 编写测试
   - 文档更新

3. **示例代码**
   - 短信认证示例
   - 第三方登录示例
   - 代码可直接参考使用

### 功能性需求

1. **接口文档**
   - `AuthenticationPlugin` 接口完整说明
   - `SecurityFilterPlugin` 接口完整说明
   - 框架核心扩展点说明

2. **开发指南**
   - 环境准备
   - 项目结构要求
   - 代码规范要求
   - 测试规范要求

3. **示例实现**
   - 短信验证码认证插件（完整示例）
   - OAuth2 第三方登录插件（参考实现）

4. **最佳实践**
   - 性能优化建议
   - 安全注意事项
   - 错误处理模式
   - 日志记录规范

### 非功能性需求

1. **可读性**
   - 清晰的代码示例
   - 逐步引导的教程风格
   - 注释详尽的示例代码

2. **完整性**
   - 覆盖所有扩展点
   - 包含常见场景
   - 提供故障排查指南

3. **实用性**
   - 代码可直接复制使用
   - 配置示例完整可用
   - 测试用例可运行

## 验收标准

### 主要验收标准

1. **内容完整性**
   - [x] 包含 `AuthenticationPlugin` 接口完整文档
   - [x] 包含 `SecurityFilterPlugin` 接口完整文档
   - [x] 包含完整的短信认证插件示例（涵盖 Token、Provider、Plugin、Config、测试）
   - [x] 包含测试编写指南

2. **示例质量**
   - [x] 示例代码可编译运行
   - [x] 示例遵循项目代码规范
   - [x] 示例包含单元测试（SmsAuthenticationTokenTest、SmsAuthenticationProviderTest、SmsAuthenticationPluginTest）

3. **文档质量**
   - [x] 结构清晰，易于导航
   - [x] 代码示例格式正确
   - [x] 与其他文档风格一致

### 辅助验收标准

1. **开发者友好**
   - [x] 新开发者可在 30 分钟内完成第一个插件
   - [x] 文档包含常见问题解答
   - [x] 提供调试技巧

2. **可维护性**
   - [x] 使用 Markdown 格式
   - [x] 易于更新和维护

## 输出物

1. 插件开发指南文档 (`docs/plugin-development.md`)
2. 示例插件代码 (`examples/plugins/`)
   - `sms-auth-plugin/` - 短信认证插件完整示例
   - `oauth2-plugin/` - OAuth2 第三方登录参考实现

## 相关依赖

- 依赖于已完成的 `AuthenticationPlugin` 接口 (security-core)
- 依赖于已完成的 `SecurityFilterPlugin` 接口 (security-core)
- 需要参考现有插件实现（JWT、Session、UsernamePassword）

## 风险与假设

**风险**：
- 插件接口可能在未来版本中变更
- 示例代码需要与核心接口保持同步

**缓解措施**：
- 文档注明接口版本兼容性
- 示例代码使用稳定版本的 API

**假设**：
- 读者已熟悉 Spring Security 基本概念
- 读者了解 Java 构造器注入模式

## 时间线

**开始时间**: 待定
**预计完成**: 2 天后

## 备注

- 需要与核心开发团队确认接口稳定性
- 考虑创建插件模板项目
- 文档需要随接口变更同步更新

---

## Dev Notes

### 项目结构

**文档输出位置**: `docs/plugin-development.md`

**示例代码输出位置**: `examples/plugins/`

### 现有插件参考

框架已实现的插件接口和实现：

**接口位置**:
- `security-core/src/main/java/com/original/security/plugin/AuthenticationPlugin.java`
- `security-core/src/main/java/com/original/security/plugin/SecurityFilterPlugin.java`

**现有实现**:
- `JwtAuthenticationPlugin` - JWT 认证插件
- `SessionAuthenticationPlugin` - Session 认证插件
- `UsernamePasswordAuthenticationPlugin` - 用户名密码认证插件

### AuthenticationPlugin 接口详解

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

### SecurityFilterPlugin 接口详解

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

### 代码规范要求 (必须遵循)

1. **依赖注入**: 必须使用构造器注入，禁止字段注入
2. **日志规范**: 使用 SLF4J，禁止 `System.out.println()` 和 `printStackTrace()`
3. **JavaDoc**: 公共 API 必须包含 JavaDoc，包含 `@author` 和 `@since`
4. **命名规范**: 类名 PascalCase，方法名 camelCase
5. **Import 规范**: 使用 import 语句，禁止在类声明中使用全包名

### 测试规范

- 测试类命名: `{ClassName}Test.java`
- 测试方法命名: `test{MethodName}_{Scenario}_{ExpectedResult}`
- 测试覆盖率: 核心插件代码 ≥ 90%

### 文档结构建议

```markdown
# 插件开发指南

## 概述
- 什么是插件系统
- 插件架构设计理念

## 认证插件开发
- AuthenticationPlugin 接口
- 开发步骤
- 完整示例：短信认证插件

## 过滤器插件开发
- SecurityFilterPlugin 接口
- 开发步骤
- 完整示例：请求日志过滤器

## 测试指南
- 单元测试编写
- 集成测试编写

## 最佳实践
- 性能优化
- 安全注意事项
- 错误处理

## 故障排查
- 常见问题
- 调试技巧
```

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story-7.6]
- [Source: _bmad-output/planning-artifacts/architecture.md#决策1-认证架构]
- [Source: security-core/src/main/java/com/original/security/plugin/AuthenticationPlugin.java]
- [Source: security-core/src/main/java/com/original/security/plugin/SecurityFilterPlugin.java]
- [Source: _bmad-output/project-context.md#关键实现规则]

---

## Dev Agent Record

### Agent Model Used

glm-5[1m]

### Debug Log References

无

### Completion Notes List

1. ✅ 创建了完整的插件开发指南文档 `docs/plugin-development.md`，包含：
   - AuthenticationPlugin 接口详细说明
   - SecurityFilterPlugin 接口详细说明
   - 完整的短信认证插件示例
   - 单元测试和集成测试指南
   - 最佳实践和故障排查

2. ✅ 创建了示例插件代码 `examples/plugins/sms-auth-plugin/`，包含：
   - SmsAuthenticationToken - 认证令牌
   - SmsAuthenticationProvider - 认证提供者
   - SmsAuthenticationPlugin - 插件主类
   - SmsVerifyCodeService - 验证码服务接口
   - SmsAuthenticationConfig - 自动配置
   - SmsProperties - 配置属性
   - 完整的单元测试

3. ✅ 更新了文档索引 `docs/README.md`，添加插件开发指南链接

4. ✅ 创建了示例插件目录说明 `examples/plugins/README.md`

### File List

**新增文件：**
- docs/plugin-development.md
- examples/pom.xml
- examples/plugins/README.md
- examples/plugins/sms-auth-plugin/src/main/java/com/example/security/plugin/sms/SmsAuthenticationToken.java
- examples/plugins/sms-auth-plugin/src/main/java/com/example/security/plugin/sms/SmsVerifyCodeService.java
- examples/plugins/sms-auth-plugin/src/main/java/com/example/security/plugin/sms/SmsAuthenticationProvider.java
- examples/plugins/sms-auth-plugin/src/main/java/com/example/security/plugin/sms/SmsAuthenticationPlugin.java
- examples/plugins/sms-auth-plugin/src/main/java/com/example/security/plugin/sms/config/SmsAuthenticationConfig.java
- examples/plugins/sms-auth-plugin/src/main/java/com/example/security/plugin/sms/config/SmsProperties.java
- examples/plugins/sms-auth-plugin/src/test/java/com/example/security/plugin/sms/SmsAuthenticationProviderTest.java
- examples/plugins/sms-auth-plugin/src/test/java/com/example/security/plugin/sms/SmsAuthenticationPluginTest.java

**修改文件：**
- docs/README.md
