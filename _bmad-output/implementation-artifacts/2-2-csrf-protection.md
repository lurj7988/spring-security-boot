# Story 2.2: 2-2-csrf-protection

Status: done
<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 开发者构建安全的 Web 应用，
I want 框架自动启用 CSRF 保护，
So that 应用免受跨站请求伪造攻击。

## Acceptance Criteria

1. **Given** security.network.csrf.enabled=true（默认）
   **When** 用户执行状态变更操作（POST/PUT/DELETE）
   **Then** 请求必须包含有效的 CSRF Token
   **And** Token 无效则返回 403 Forbidden
   **And** 审计事件被记录（FR16）

2. **Given** 使用表单提交
   **When** 渲染表单
   **Then** 表单包含隐藏的 CSRF Token 字段
   **And** Token 通过 CsrfTokenRepository 生成

3. **Given** 构建 RESTful API（无状态）
   **When** 设置 security.network.csrf.enabled=false
   **Then** CSRF 保护被禁用
   **And** 启动时打印警告日志

4. **Given** CSRF Token 配置
   **When** 查看 security.network.csrf.token-header
   **Then** 可以自定义 Token 请求头名称（默认 X-CSRF-TOKEN）
   **And** 配置有合理的默认值

## Tasks / Subtasks

- [x] Task 1: 定义网络安全 CSRF 的配置属性 `CsrfProperties`。
  - [x] Subtask 1.1: 包含属性 `enabled` (默认 true) 和 `token-header` (默认 "X-CSRF-TOKEN")。
- [x] Task 2: 编写基于这些配置的配置日志与提醒逻辑。
  - [x] Subtask 2.1: 当 `enabled` 设置为 false 时，在启动时输出警告级别的日志，提示开发者当前 CSRF 防护已被禁用。
- [x] Task 3: 在框架内组装 Spring Security CSRF 保护。
  - [x] Subtask 3.1: 更新 `NetworkSecurityAutoConfiguration` 等对应配置，如果开启，应用 `CookieCsrfTokenRepository` 并指定由配置加载的 Header Name；如果禁用，调用 `.csrf().disable()`。
  - [x] Subtask 3.2: 保证 CSRF 校验失败时的异常（403 Forbidden）能够套用整个框架自定义的统一异常返回格式。
- [x] Task 4: 添加单元与集成测试。
  - [x] Subtask 4.1: 测试 `enabled=true` 且缺少 Token 时的 403 异常，并测试配置正确 Token 的请求成功。
  - [x] Subtask 4.2: 测试 `enabled=false` 时启动打印警告日志，并且跨站状态变更请求不被拦截。

## Dev Notes

### Previous Story Intelligence

- 依据前一周期 (2-1)，网络安全相关的装配机制已经放在了 `NetworkSecurityAutoConfiguration` 中，可以以此为基础补充基于 HttpSecurity `.csrf()` 的逻辑。
- 此前已建立了良好的属性分离规范（例如 `CorsProperties`），CSRF 相关也可以采用单独的 `@ConfigurationProperties(prefix = "security.network.csrf")`（类名可为 `CsrfProperties`）方便解耦管理。

### Technical Requirements

- Spring Boot 2.7.18
- Spring Security 5.8.x。
- 强制使用构造器注入。
- 返回的请求结构要按照标准实现，CSRF 缺少或者错误的 403 被 `AccessDeniedHandler` 拦截时，应当遵守返回规范（含有 code, message, data）。如果之前未编写过专门拦截处理 CSRFException 的逻辑，可以补充在此处。
- CSRF 防护通常拦截 POST/PUT/DELETE/PATCH 等变动类操作，不拦截 GET/HEAD/OPTIONS/TRACE（Spring Security 默认行为）。

### Architecture Compliance

- `NetworkSecurityAutoConfiguration` 需要维护整洁。
- 使用 SLF4J 进行日志纪录，不在代码中保留任何 `System.out` 的打印。
- 确保应用等保级别相关的设计要求满足。针对前端应用的分离特性，CSRF 常使用 Header 传递 Token，因此配置属性 `token-header` 应该正确透传至 `CookieCsrfTokenRepository.withHttpOnlyFalse().setHeaderName(...)` 等类似配置上。

### Project Structure Notes

- `CsrfProperties` 可置于包 `com.original.security.config`。
- 如果编写过滤器相关的处理或者自定义 Repository，保持代码放在对应的 `com.original.security.config` 或者子包下。

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 2.2: 实现 CSRF 防护]
- [Source: _bmad-output/planning-artifacts/architecture.md#决策 2：网络安全架构 - 自动配置策略]

## Dev Agent Record

### Agent Model Used

Antigravity

### Debug Log References

- none

### Completion Notes List

- 实现了 CsrfProperties 配置类，包含 enabled 和 token-header 属性
- 在 NetworkSecurityAutoConfiguration 中添加了 CsrfTokenRepository Bean
- 更新了 SecurityAutoConfiguration 以支持 CSRF 保护的启用/禁用
- 创建了 FrameAccessDeniedHandler 来统一处理 CSRF 验证失败时的 403 响应
- 添加了单元测试和集成测试来验证 CSRF 功能
- 在 SecurityConfigurationValidator 中添加了 CSRF 禁用时的警告日志

### File List

- security-core/src/main/java/com/original/security/config/CsrfProperties.java (新增)
- security-core/src/main/java/com/original/security/handler/FrameAccessDeniedHandler.java (新增)
- security-core/src/main/java/com/original/security/config/NetworkSecurityAutoConfiguration.java (修改)
- security-core/src/main/java/com/original/security/config/SecurityAutoConfiguration.java (修改)
- security-core/src/main/java/com/original/security/config/SecurityConfigurationValidator.java (修改)
- security-core/src/test/java/com/original/security/CsrfIntegrationTest.java (新增)
- security-core/src/test/java/com/original/security/CsrfDisabledIntegrationTest.java (新增)
- security-core/src/test/java/com/original/security/config/CsrfPropertiesTest.java (新增)
- security-core/src/test/java/com/original/security/config/SecurityConfigurationValidatorTest.java (修改)
- _bmad-output/implementation-artifacts/sprint-status.yaml (修改)
