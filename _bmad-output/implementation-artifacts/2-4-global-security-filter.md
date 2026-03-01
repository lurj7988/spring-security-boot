# Story 2.4: 2-4-global-security-filter

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 框架开发者,
I want 统一的安全过滤器链管理所有安全功能,
so that 安全功能的执行顺序和优先级一致。

## Acceptance Criteria

1. **Given** 多个安全过滤器（CORS、CSRF、SecurityHeaders）
   **When** 请求进入
   **Then** 过滤器按正确顺序执行
   **And** CORS 过滤器最先执行
   **And** CSRF 和 SecurityHeaders 在认证之前执行

2. **Given** 过滤器链配置
   **When** 查看 `SecurityFilterChain` 配置
   **Then** 使用 Spring Security 标准 `FilterChain`
   **And** 核心安全过滤器（CORS, CSRF, Headers）在 `SecurityAutoConfiguration` 中显式按序配置
   **And** 配置清晰易读，符合 Spring Security DSL 规范

3. **Given** 自定义过滤器需要集成
   **When** 开发者添加自定义安全过滤器
   **Then** 可以通过实现标准接口 `SecurityFilterPlugin`（或类似机制）集成
   **And** 可以指定过滤器在 Spring Security 过滤器链中的相对位置/优先级
   **And** 文档提供集成示例

4. **Given** 过滤器性能
   **When** 请求通过过滤器链
   **Then** 每个过滤器执行时间 < 10ms
   **And** 不阻塞正常请求
   **And** 满足 NFR-PERF-001 (P95 < 200ms)

## Tasks / Subtasks

- [x] Task 1: 定义 `SecurityFilterPlugin` 接口规范。 (AC: 3)
  - [x] Subtask 1.1: 接口应支持获取 `Filter` 实例。
  - [x] Subtask 1.2: 接口应支持指定过滤器的位置（例如：`beforeFilter`, `afterFilter`）。
- [x] Task 2: 完善 `SecurityAutoConfiguration` 中的过滤器链装配。 (AC: 1, 2)
  - [x] Subtask 2.1: 确保 CORS, CSRF, SecurityHeaders 的装配顺序符合架构要求。
  - [x] Subtask 2.2: 使用 `ObjectProvider<SecurityFilterPlugin>` 收集所有自定义过滤器插件。
  - [x] Subtask 2.3: 在 `HttpSecurity` 配置中动态循环注册这些插件。
- [x] Task 3: 优化过滤器性能与日志管控。 (AC: 4)
  - [x] Subtask 3.1: 过滤器应保持极简，避免阻塞。
  - [x] Subtask 3.2: 增加调试日志，在启动或 DEBUG 模式下打印加载的过滤器顺序。
- [x] Task 4: 编写集成测试验证过滤器顺序与插件机制。 (AC: 1, 3)
  - [x] Subtask 4.1: 创建模拟 `SecurityFilterPlugin` 并验证其是否成功注入链中。
  - [x] Subtask 4.2: 验证核心过滤器（CORS, CSRF）的执行顺序优于认证逻辑。

## Dev Notes

### Previous Story Intelligence

- 在 Story 2.1, 2.2, 2.3 中，已经分别实现了 CORS、CSRF 和 Security Headers 的基础配置。目前这些配置分散在 `SecurityAutoConfiguration` 中。
- 本故事的目标是"收口"，将这些网络安全防护逻辑整合为一个清晰、可扩展的体系。
- 架构上已经确立了 `AuthenticationPlugin` 模式，建议对于 Filter 的扩展也采用类似的 `SecurityFilterPlugin` 模式，保持设计语言一致性。

### Technical Requirements

- **Spring Boot 2.7.18 / Spring Security 5.8.x**
- **强制约束**: 必须使用构造器依赖注入。
- **过滤器顺序**: 遵循 Spring Security 默认且安全的顺序。通常为：`ChannelProcessingFilter` (Optional) -> `WebAsyncManagerIntegrationFilter` -> `SecurityContextPersistenceFilter` -> `HeaderWriterFilter` -> `CorsFilter` -> `CsrfFilter` -> `LogoutFilter` -> `JwtAuthenticationFilter` (Custom) -> `UsernamePasswordAuthenticationFilter` -> ... -> `FilterSecurityInterceptor`。
- **扩展性**: `SecurityFilterPlugin` 接口应允许开发者定义自己的过滤器并指定位置。

### Architecture Compliance

- 接口定义位于 `com.original.security.plugin` 或 `com.original.security.filter` 包中。
- 配置维持在 `com.original.security.config.SecurityAutoConfiguration`。
- 不能使用 `@Value`，统一度过 `@ConfigurationProperties`。
- 遵循统一响应对象模式。

### Project Structure Notes

- `security-core/src/main/java/com/original/security/plugin/SecurityFilterPlugin.java`
- 修改 `security-core/src/main/java/com/original/security/config/SecurityAutoConfiguration.java` 以支持插件收集。

### SecurityFilterPlugin 集成示例 (AC3)

开发者可以通过实现 `SecurityFilterPlugin` 接口来添加自定义安全过滤器：

**基础示例（使用匿名类 + @Order 注解）：**
```java
@Bean
@Order(1)  // 使用 @Order 注解控制排序（值越小优先级越高）
public SecurityFilterPlugin myCustomFilterPlugin() {
    // 缓存过滤器实例，确保 getFilter() 返回相同实例
    final Filter filter = new MyCustomFilter();

    return new SecurityFilterPlugin() {
        @Override
        public String getName() {
            return "MyCustomFilter";
        }

        @Override
        public Filter getFilter() {
            return filter;  // 返回缓存的单例实例
        }

        @Override
        public Position getPosition() {
            return Position.BEFORE;  // 或 AFTER, AT
        }

        @Override
        public Class<? extends Filter> getTargetFilterClass() {
            return UsernamePasswordAuthenticationFilter.class;
        }

        // isEnabled() 使用默认实现（返回 true）
        // getOrder() 使用默认实现（返回 0），但 @Order(1) 会覆盖它
    };
}
```

**高级示例（支持配置动态控制 + 自定义排序）：**
```java
@Component
@Order(2)  // 多个插件时，通过 @Order 控制相对顺序
@ConfigurationProperties(prefix = "security.custom-filter")
public class MyCustomFilterPlugin implements SecurityFilterPlugin {

    private boolean enabled = true;
    private final Filter filter = new MyCustomFilter();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return "MyCustomFilter";
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
        return UsernamePasswordAuthenticationFilter.class;
    }

    @Override
    public boolean isEnabled() {
        return enabled;  // 根据配置动态控制
    }

    // 可以覆盖 getOrder() 方法，但 @Order 注解优先级更高
    // @Override
    // public int getOrder() {
    //     return 2;
    // }
}

**配置示例：**
```properties
# 启用/禁用自定义过滤器
security.custom-filter.enabled=true
```

**位置说明：**
- `BEFORE`: 在目标过滤器之前执行
- `AFTER`: 在目标过滤器之后执行
- `AT`: 在目标过滤器的相同位置添加（目标过滤器仍然存在）

**常用目标过滤器：**
- `UsernamePasswordAuthenticationFilter.class` - 用户名密码认证
- `CsrfFilter.class` - CSRF 保护
- `CorsFilter.class` - CORS 处理
- `FilterSecurityInterceptor.class` - 权限拦截

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 2.4: 实现全局安全过滤器]
- [Source: _bmad-output/planning-artifacts/architecture.md#决策 2：网络安全架构 - 自动配置策略]

## Dev Agent Record

### Agent Model Used

Antigravity

### Debug Log References

### Completion Notes List

- Code review fixed: 移除测试中的 System.out.println，改用 SLF4J 日志
- Code review fixed: 修复测试方法命名符合项目规范
- Code review fixed: 添加测试类和内部类的 JavaDoc 文档
- Code review fixed: 修复 SecurityAutoConfiguration 中的空指针检查逻辑
- Code review fixed: 增强测试验证 SecurityHeaders 过滤器位置
- Note: JwtAuthenticationFilter 保持硬编码方式，后续可考虑迁移到插件机制
- Code review (2026-03-01): 增强 SecurityFilterPlugin.getFilter() JavaDoc，明确应返回相同实例
- Code review (2026-03-01): 优化 SecurityAutoConfiguration 插件过滤，使用 Stream.filter() 预过滤
- Code review (2026-03-01): 修复测试方法命名 testSecurityFilterChain_WithCustomPlugins_InjectsAndOrdersCorrectly
- Code review (2026-03-01): 修复测试中 getFilter() 返回新实例问题，改为返回缓存的单例
- Code review (2026-03-01): 新增 SecurityFilterPluginAtPositionTest 测试 AT 位置功能
- Code review (2026-03-01): 修复 SecurityAutoConfiguration 使用完全限定名问题，改用 import 语句
- Code review (2026-03-01): 创建 FilterTestUtils 工具类消除重复代码
- Code review (2026-03-01): 增强 SecurityFilterPluginAtPositionTest 测试验证 AT 位置邻近关系
- Code review (2026-03-01): 新增 SecurityFilterPerformanceTest 验证 AC4 性能要求
- Code review (2026-03-01): 从 git 暂存区移除 compile.log 和 test.log 临时文件
- Code review (2026-03-01): 修正 Task 2.2 描述与实际实现一致
- Code review (2026-03-01): 修正 Position.AT JavaDoc 说明，明确目标过滤器仍然存在
- Code review (2026-03-01): 增强 SecurityFilterPerformanceTest.isSecurityContextDependent() 方法文档
- Code review (2026-03-01): 增强 FilterTestUtils.getFilterIndex() 方法文档，说明同名过滤器限制
- Code review (2026-03-01): 为 TestPluginAFilter 和 TestPluginBFilter 添加 @author 和 @since JavaDoc
- Code review (2026-03-01): 增强 Dev Notes 示例代码，添加基础示例和高级示例
- Code review (2026-03-01): HIGH-1 修复 - 添加 SecurityFilterPlugin.getOrder() 方法支持相同位置插件排序
- Code review (2026-03-01): HIGH-2 修复 - 增强测试直接验证 CSRF/Headers 在认证过滤器之前执行
- Code review (2026-03-01): MEDIUM-1 修复 - 更新 File List 记录 sprint-status.yaml 修改
- Code review (2026-03-01): MEDIUM-2 修复 - 添加 DEBUG 模式下 getFilter() 实例一致性验证
- Code review (2026-03-01): MEDIUM-3 修复 - 增强 FilterTestUtils 支持 CGLIB 代理类名匹配
- Code review (2026-03-01): MEDIUM-4 修复 - 性能测试现在包含 CsrfFilter（使用 GET 请求测试）
- Code review (2026-03-01): HIGH-1 修复 - SecurityFilterPlugin 继承 Ordered 接口，确保 getOrder() 排序生效
- Code review (2026-03-01): HIGH-2 修复 - 更新 Dev Notes 示例代码，添加 @Order 注解说明
- Code review (2026-03-01): MEDIUM-1 修复 - 增强性能测试文档说明局限性
- Code review (2026-03-01): MEDIUM-2 修复 - 添加 FilterTestUtils 同名过滤器限制测试
- Code review (2026-03-01): MEDIUM-3 修复 - 更新 SecurityFilterPlugin JavaDoc 与实现一致
- Code review (2026-03-01): HIGH-1 修复 - SecurityAutoConfiguration 第 234 行移除全包名，使用导入的 Filter
- Code review (2026-03-01): MEDIUM-1 修复 - 增强 SecurityFilterPerformanceTest 文档，添加未来改进建议
- Code review (2026-03-01): MEDIUM-2 修复 - 重命名 AT 位置测试方法为 testSecurityFilterChain_WithAtPosition_AddsAtSamePosition
- Code review (2026-03-01): MEDIUM-3 修复 - FilterTestUtils 添加 null 安全检查
- Code review (2026-03-01): MEDIUM-4 修复 - 新增 SecurityFilterPluginOrderingTest 验证相同位置多插件排序
- Code review (2026-03-01): MEDIUM-1 修复 - FilterTestUtils 新增 getAllFilterIndices() 方法支持同名多实例
- Code review (2026-03-01): MEDIUM-2 修复 - SecurityFilterPlugin.getFilter() JavaDoc 推荐使用 OncePerRequestFilter
- Code review (2026-03-01): LOW-1 修复 - SecurityAutoConfiguration 插件注册详情日志级别改为 debug
- Code review (2026-03-01): MEDIUM-3 修复 - SecurityFilterPerformanceTest 新增带安全上下文的过滤器性能测试

### File List

- `security-core/src/main/java/com/original/security/plugin/SecurityFilterPlugin.java` (新增)
  - 定义 SecurityFilterPlugin 接口，继承 Spring Ordered 接口，支持 BEFORE/AFTER/AT 三种位置类型
  - 包含 getName(), getFilter(), getPosition(), getTargetFilterClass(), isEnabled(), getOrder() 方法
  - getFilter() JavaDoc 明确要求返回相同实例，推荐使用 OncePerRequestFilter
  - getOrder() 方法通过继承 Ordered 接口，确保 Spring orderedStream() 正确排序

- `security-core/src/main/java/com/original/security/config/SecurityAutoConfiguration.java` (修改)
  - 添加 SecurityFilterPlugin 的 ObjectProvider 注入
  - 实现插件动态注册逻辑，使用 Stream.filter() 预过滤启用的插件
  - 插件注册详情日志级别改为 debug，总数统计保持 info
  - 添加 DEBUG 模式下的过滤器顺序日志

- `security-core/src/test/java/com/original/security/config/SecurityFilterPluginIntegrationTest.java` (新增)
  - 验证自定义插件正确注入过滤器链
  - 验证 CORS、CSRF、Headers 过滤器顺序
  - 直接验证 CSRF 和 SecurityHeaders 在认证过滤器之前执行
  - 包含测试用插件示例（BEFORE 和 AFTER 位置）
  - 新增 FilterTestUtils 同名过滤器限制测试

- `security-core/src/test/java/com/original/security/config/SecurityFilterPluginAtPositionTest.java` (新增)
  - 验证 Position.AT 位置功能
  - 测试自定义过滤器替换目标过滤器的能力
  - 增强测试验证 AT 过滤器与目标过滤器的位置邻近关系

- `security-core/src/test/java/com/original/security/config/FilterTestUtils.java` (新增)
  - 过滤器测试工具类
  - 提供 getFilterIndex()、containsFilter() 和 getAllFilterIndices() 方法
  - getAllFilterIndices() 支持获取同名过滤器的所有索引
  - 消除测试类中的重复代码
  - 添加 null 安全检查，抛出 IllegalArgumentException

- `security-core/src/test/java/com/original/security/config/SecurityFilterPerformanceTest.java` (新增)
  - 验证 AC4 性能要求：每个过滤器执行时间 < 10ms
  - 验证完整过滤器链不阻塞正常请求
  - 新增带模拟安全上下文的过滤器性能测试，扩展测试覆盖范围
  - 文档说明测试局限性（部分过滤器需要完整安全上下文被跳过）
  - 使用多次迭代取平均值的方式测量性能
  - 增强文档说明未来改进建议

- `security-core/src/test/java/com/original/security/config/SecurityFilterPluginOrderingTest.java` (新增)
  - 验证相同 Position 和 TargetFilterClass 的多插件排序
  - 测试 @Order 注解控制相对顺序
  - 验证 order 值小的插件先执行

- `_bmad-output/implementation-artifacts/sprint-status.yaml` (修改)
  - 更新 2-4-global-security-filter 状态为 done
  - 更新 epic-2 状态为 done