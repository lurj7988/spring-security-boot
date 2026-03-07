# Story 5.3: 实现用户查询 API

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 开发者，
I want 使用框架提供的用户查询 API，
so that 我可以获取用户信息。

## Acceptance Criteria

1. **Given** 已有用户
   **When** GET /api/users/{id}
   **Then** 返回用户详情
   **And** 不返回密码字段
   **And** 包含角色信息
   **And** 返回统一格式响应

2. **Given** 用户列表
   **When** GET /api/users?page=0&size=10
   **Then** 返回分页用户列表
   **And** 支持按 username 模糊搜索
   **And** 支持按 enabled 状态筛选

3. **Given** 当前登录用户
   **When** GET /api/users/me
   **Then** 返回当前用户信息
   **And** 基于认证上下文获取
   **And** 不需要传递用户 ID

## Tasks / Subtasks

- [x] Task 1 (AC: 1) - 实现用户详情查询 API
  - [x] Subtask 1.1 - 创建 UserService.getUserById 方法
  - [x] Subtask 1.2 - 创建 UserController.getUserById 接口
  - [x] Subtask 1.3 - 确保不返回密码字段
- [x] Task 2 (AC: 2) - 实现用户列表查询 API
  - [x] Subtask 2.1 - 创建 UserService.getUsers 方法（带分页）
  - [x] Subtask 2.2 - 创建 UserController.getUsers 接口
  - [x] Subtask 2.3 - 实现按 username 模糊搜索功能
  - [x] Subtask 2.4 - 实现按 enabled 状态筛选功能
- [x] Task 3 (AC: 3) - 实现当前用户查询 API
  - [x] Subtask 3.1 - 创建 UserController.getCurrentUser 接口
  - [x] Subtask 3.2 - 从认证上下文中获取当前用户信息

## Dev Notes

### 项目结构说明
- 位置: security-components/security-user-impl
- 包名: com.original.security.user.controller, com.original.security.user.service, com.original.security.user.repository
- 遵循 API-Impl 架构模式

### 技术要求
- 必须使用构造器依赖注入（禁止字段注入）
- 所有 API 响应使用统一格式：Response.successBuilder(data).build()
- 不返回密码字段（安全要求）
- 支持分页查询（Spring Data JPA Pageable）
- 模糊搜索使用 JPA 模糊查询（LIKE '%keyword%'）

### 测试要求
- Controller 层测试：API 端点正确性、响应格式验证
- Service 层测试：业务逻辑、分页功能
- Repository 层测试：查询逻辑、模糊搜索

### 安全要求
- /api/users/me 端点需要认证
- 所有用户查询 API 遵循权限控制策略

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 5.3](FR47)
- [Source: _bmad-output/planning-artifacts/architecture.md](API-Impl 分离模式)

## Dev Agent Record

### Agent Model Used

claude-opus-4-6

### Debug Log References

### Completion Notes List

- Implemented user query API endpoints with pagination, search and filter capabilities
- Added getCurrentUser() functionality to retrieve currently authenticated user
- Enhanced UserRepository with findByUsernameContainingAndEnabled query method
- Updated UserService interface with listUsers method supporting filters
- Added comprehensive unit and integration tests for all new functionality
- Maintained security requirements by excluding password fields from responses

**Code Review Fixes (2026-03-06):**
- Fixed: Changed UserDisabledException to IllegalStateException for unauthenticated access in getCurrentUser()
- Fixed: Removed redundant username equality check in getCurrentUser() (line 169-173)
- Fixed: Added @PreAuthorize("isAuthenticated()") annotation to /api/users/me endpoint
- Fixed: Added performance warning documentation to UserRepository.findByUsernameContainingAndEnabled()
- Fixed: Updated File List to include new exception classes (UserNotFoundException, UserDisabledException)
- Fixed: Created comprehensive Repository layer tests (UserRepositoryTest.java)

### File List

- src/main/java/com/original/security/user/controller/UserController.java
- src/main/java/com/original/security/user/service/UserService.java
- src/main/java/com/original/security/user/service/impl/UserServiceImpl.java
- src/main/java/com/original/security/user/repository/UserRepository.java
- src/main/java/com/original/security/user/api/UserApi.java
- src/main/java/com/original/security/user/exception/UserNotFoundException.java
- src/main/java/com/original/security/user/exception/UserDisabledException.java
- src/test/java/com/original/security/user/controller/UserControllerTest.java
- src/test/java/com/original/security/user/service/impl/UserServiceImplTest.java