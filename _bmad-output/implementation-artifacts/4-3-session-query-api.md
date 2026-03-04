# Story 4.3: 实现会话查询 API

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 管理员，
I want 查询当前活跃的用户会话，
so that 我可以监控系统使用情况。

## Acceptance Criteria

1. **Given** 管理员用户
   **When** `GET /api/sessions` 查询所有会话
   **Then** 返回所有活跃会话列表
   **And** 包含用户名、登录时间、最后活跃时间
   **And** 支持分页查询

2. **Given** 普通用户
   **When** `GET /api/sessions/me` 查询自己的会话
   **Then** 返回当前用户的所有会话
   **And** 不返回其他用户的会话
   **And** 支持多设备登录场景

3. **Given** 会话信息
   **When** 查看会话详情
   **Then** 包含会话 ID、用户信息、IP 地址、登录时间
   **And** 返回统一格式响应

## Tasks / Subtasks

- [ ] Task 1: 定义会话查询 DTO (AC: 1, 3)
  - [ ] Subtask 1.1: 创建 `SessionInfo` DTO，包含会话 ID、用户名、登录时间、最后活跃时间、IP 地址等字段。

- [ ] Task 2: 实现会话查询 API Controller (AC: 1, 2)
  - [ ] Subtask 2.1: 在 `AuthenticationController` 或新 `SessionController` 中增加 `GET /api/sessions/me` 接口获取当前用户会话。
  - [ ] Subtask 2.2: 增加 `GET /api/sessions` 接口支持分页查询所有用户的会话。

- [ ] Task 3: 获取会话数据逻辑 (AC: 1, 2)
  - [ ] Subtask 3.1: 使用注入的 `SessionRegistry` 获取指定用户或所有用户的活跃 `SessionInformation`。
  - [ ] Subtask 3.2: 若使用 JWT 且配置为无状态，需明确如何支持会话查询，或补充/说明有状态/数据库支持时的查询逻辑。

- [ ] Task 4: 配置 API 权限控制 (AC: 1, 2)
  - [ ] Subtask 4.1: 限制 `GET /api/sessions` 仅允许管理员 (`hasRole('ADMIN')` 或相关权限) 访问。
  - [ ] Subtask 4.2: 限制 `GET /api/sessions/me` 仅需普通认证即可访问。

- [ ] Task 5: 编写测试并验证覆盖率 (AC: All)
  - [ ] Subtask 5.1: 编写单元测试验证分页逻辑与 DTO 映射。
  - [ ] Subtask 5.2: 编写集成测试模拟 `GET /api/sessions` 访问控制（非管理员拒绝）。

## Dev Notes

- **Relevant architecture patterns and constraints:** 
  - 本项目默认是双认证模式，如果采用 `JWT` 无状态，则 `SessionRegistry` 的表现可能为空。若配置为 session 模式，则需要确保 `SessionRegistry` 正确记录了 principals。我们需要根据当前 `SessionAuthenticationPlugin` 和 `SecurityAutoConfiguration` 中的 `sessionRegistry` Bean 配置来实现逻辑。
- **Source tree components to touch:**
  - `security-core/src/main/java/com/original/security/controller/`
  - `security-core/src/main/java/com/original/security/dto/`

### Project Structure Notes

- Alignment with unified project structure: Controllers are in `controller` package, DTOs in `dto`.

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 4.3]

## Dev Agent Record

### Agent Model Used

Gemini

### Debug Log References

### Completion Notes List

### File List