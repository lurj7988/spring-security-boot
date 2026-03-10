# Story 6.1: audit-event-publishing

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 开发者,
I want 框架发布审计事件,
so that 我可以记录和监控安全相关操作.

## Acceptance Criteria

1. **Authentication Success**: 
   - **Given** 用户登录成功
   - **When** 认证完成
   - **Then** 发布 AuthenticationSuccessEvent
   - **And** 事件包含用户名、认证方式、时间戳
   - **And** 事件包含 IP 地址（可选）

2. **Authentication Failure**: 
   - **Given** 用户登录失败
   - **When** 认证失败
   - **Then** 发布 AuthenticationFailureEvent
   - **And** 事件包含失败原因、用户名（如可用）
   - **And** 事件用于异常检测

3. **Authorization Failure**: 
   - **Given** 用户访问无权限资源
   - **When** 授权失败
   - **Then** 发布 AuthorizationFailureEvent
   - **And** 事件包含用户、资源、所需权限
   - **And** 审计日志记录

4. **Event Mechanism**: 
   - **Given** 事件发布机制
   - **When** 查看事件配置
   - **Then** 使用 Spring ApplicationEventPublisher
   - **And** 应用可以监听事件做后续处理
   - **And** 不影响认证性能

## Tasks / Subtasks

- [x] Task 1: Create core event classes (AC: 1, 2, 3)
  - [x] Create base `AuditEvent` class.
  - [x] Create `AuthenticationSuccessEvent`, `AuthenticationFailureEvent`, and `AuthorizationFailureEvent`.
- [x] Task 2: Implement event publisher (AC: 4)
  - [x] Create `AuditEventPublisher` interface and Spring-based implementation (`SpringAuditEventPublisher`).
  - [x] Ensure the publisher wraps `ApplicationEventPublisher` using constructor injection.
- [x] Task 3: Integrate event publishing into authentication/authorization flows (AC: 1, 2, 3, 4)
  - [x] Publish success and failure events in authentication providers/plugins or global authentication manager.
  - [x] Publish authorization failure events in global security filters or method security interceptors.
- [x] Task 4: Ensure structural logging for events (AC: 3)
  - [x] Implement an `AuditEventListener` that logs events using SLF4J at INFO/WARN levels.
  - [x] Verify sensitive info (like passwords or full JWT tokens) is not logged.
- [x] Task 5: Write tests
  - [x] Write unit tests for events and publisher with >= 90% coverage.
  - [x] Write integration tests verifying events are fired upon login/failure.

## Dev Notes

- **Architecture Patterns**: 
  - Follow constructor dependency injection.
  - Follow Spring standard event mechanisms (`ApplicationEvent` and `ApplicationEventPublisher`).
- **Source Tree Components**: 
  - `security-core/src/main/java/com/original/security/event/` (Events and Publisher)
  - `security-core/src/main/java/com/original/security/observability/` (Listener/Logging)
- **Testing Standards**: 
  - Unit tests required for all core security event generation code. Goal: 90% core coverage.
  - SLF4J must be used for logging. No `System.out`.

### Project Structure Notes

- Base package is `com.original.security`. Ignore the `org.original.security` typo in `architecture.md`.
- Security events should align with Spring Boot Actuator's audit concepts but be tailored for the framework.

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story-6.1]
- [Source: _bmad-output/planning-artifacts/architecture.md]

## Dev Agent Record

### Agent Model Used

{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

- 实现了审计事件基础类和三个具体事件类
- 使用 Spring ApplicationEventPublisher 作为事件发布机制
- 在认证/授权处理器中集成事件发布
- 实现了 AuditEventListener 进行结构化日志记录
- 添加了敏感信息自动过滤机制（密码、token 等）
- 删除了旧的 AuthorizationAuditListener（功能已合并到新架构）

### File List

**新增文件:**
- `security-core/src/main/java/com/original/security/event/AuditEvent.java` - 审计事件基类
- `security-core/src/main/java/com/original/security/event/AuditEventPublisher.java` - 事件发布器接口
- `security-core/src/main/java/com/original/security/event/AuthenticationSuccessEvent.java` - 认证成功事件
- `security-core/src/main/java/com/original/security/event/AuthenticationFailureEvent.java` - 认证失败事件
- `security-core/src/main/java/com/original/security/event/SpringAuditEventPublisher.java` - Spring 实现
- `security-core/src/main/java/com/original/security/observability/AuditEventListener.java` - 事件监听器
- `security-core/src/test/java/com/original/security/event/AuditEventTest.java` - 事件测试
- `security-core/src/test/java/com/original/security/event/SpringAuditEventPublisherTest.java` - 发布器测试
- `security-core/src/test/java/com/original/security/observability/AuditEventListenerTest.java` - 监听器测试
- `security-core/src/test/java/com/original/security/handler/FrameAccessDeniedHandlerTest.java` - 访问拒绝处理器测试

**修改文件:**
- `security-core/src/main/java/com/original/security/event/AuthorizationFailureEvent.java` - 添加 JavaDoc 和常量引用
- `security-core/src/main/java/com/original/security/handler/FrameAuthenticationSuccessHandler.java` - 集成事件发布
- `security-core/src/main/java/com/original/security/handler/FrameAuthenticationFailureHandler.java` - 集成事件发布
- `security-core/src/main/java/com/original/security/handler/FrameAccessDeniedHandler.java` - 集成事件发布
- `security-core/src/main/java/com/original/security/config/SecurityAutoConfiguration.java` - 注册 AuditEventListener
- `security-core/src/test/java/com/original/security/handler/FrameAuthenticationSuccessHandlerTest.java` - 更新测试
- `security-core/src/test/java/com/original/security/handler/FrameAuthenticationFailureHandlerTest.java` - 更新测试

**删除文件:**
- `security-core/src/main/java/com/original/security/event/AuthorizationAuditListener.java` - 功能已合并到 AuditEventListener

### Change Log

- 2026-03-10: 初始实现完成
- 2026-03-10: 代码审查修复 - 添加敏感信息过滤、完善 JavaDoc、增强测试覆盖
- 2026-03-10: 第二轮代码审查修复 - 添加事件发布异常保护、增强测试覆盖、添加 @since 注解

## Senior Developer Review (AI)

**审查日期:** 2026-03-10
**审查者:** AI Code Reviewer

### 审查结果：✅ APPROVED

### 已修复的问题

| 严重程度 | 问题 | 修复方案 |
|---------|------|---------|
| CRITICAL | Story File List 为空 | 已填充完整的文件变更列表 |
| HIGH | 敏感信息过滤机制缺失 | 在 AuditEvent 基类中添加 filterSensitiveDetails() 方法 |
| MEDIUM | 公共 API 缺少 @author/@since | 已为所有事件类和接口添加完整 JavaDoc |
| MEDIUM | 测试断言不足 | 增强了 AuditEventTest、SpringAuditEventPublisherTest 等测试 |
| MEDIUM | 缺少 FrameAccessDeniedHandlerTest | 已创建完整的测试类 |
| MEDIUM | 用户名提取逻辑重复 | 统一使用 AuthorizationFailureEvent.extractUsername() |
| LOW | details Map 可被外部修改 | getDetails() 返回不可修改的 Map |
| LOW | "anonymous" 魔法字符串 | 定义为 AuditEvent.ANONYMOUS_USER 常量 |

### 验证通过的测试

- AuditEventTest (12 tests)
- SpringAuditEventPublisherTest (3 tests)
- AuditEventListenerTest (7 tests)
- FrameAccessDeniedHandlerTest (5 tests)
- FrameAuthenticationSuccessHandlerTest (4 tests) - 增强
- FrameAuthenticationFailureHandlerTest (5 tests) - 增强

### 第二轮审查修复的问题

| 严重程度 | 问题 | 修复方案 |
|---------|------|---------|
| HIGH | 事件发布失败会中断认证流程 | 添加 try-catch 保护，确保事件发布异常不影响认证 |
| MEDIUM | 测试覆盖率不足 | 新增测试用例覆盖事件发布成功/失败场景 |
| LOW | 类级别缺少 @since | 为 FrameAuthenticationSuccessHandler 和 FrameAuthenticationFailureHandler 添加 @since 1.0.0 |

### 最终状态

- **Status:** done
- **所有 Acceptance Criteria:** ✅ 已实现
- **所有 Tasks:** ✅ 已完成
- **测试覆盖:** ✅ 已增强