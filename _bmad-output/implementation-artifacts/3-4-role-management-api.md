# Story 3.4: role-management-api

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 系统管理员,
I want 通过 API 管理角色和分配权限,
so that 我可以控制系统的访问权限。

## Acceptance Criteria

1. **Given** 管理员用户
   **When** `POST /api/roles` 创建角色
   **Then** 角色创建成功
   **And** 返回统一格式响应 `{code, message, data}`
   **And** 角色名称唯一性校验

2. **Given** 已有角色
   **When** `POST /api/roles/{roleId}/permissions` 分配权限
   **Then** 权限分配成功
   **And** 可以批量分配多个权限
   **And** 操作记录审计日志（FR15）

3. **Given** 查询角色
   **When** `GET /api/roles/{roleId}`
   **Then** 返回角色详情
   **And** 包含关联的权限列表
   **And** 支持分页查询 (Note: Should also support GET `/api/roles` for listing)

## Tasks / Subtasks

- [x] Task 1: 定义 RoleApi 接口及 DTO (AC: 1, 2, 3)
  - [x] Subtask 1.1: 创建 `RoleApi` 接口，定义 POST, GET 端点
  - [x] Subtask 1.2: 创建相应的请求和响应 DTO，如 `RoleCreateRequest`, `RoleDTO`, `PermissionAssignRequest`
- [x] Task 2: 实现 RoleController 和 Service 扩展 (AC: 1, 2, 3)
  - [x] Subtask 2.1: 在 `RoleController` 实现接口
  - [x] Subtask 2.2: 在 `RoleService` (和 `RoleServiceImpl`) 中添加角色创建和权限分配的方法
  - [x] Subtask 2.3: 在分配权限后，调用 `RoleService.clearAllCache()` (或相关缓存清理方法) 使权限缓存失效
  - [x] Subtask 2.4: 在角色操作时，发布相应的审计事件记录审计日志 (遵循 FR15)
- [x] Task 3: API 异常处理与验证 (AC: 1)
  - [x] Subtask 3.1: 处理角色名称唯一性约束冲突，返回清晰错误信息
  - [x] Subtask 3.2: 统一个 API 响应使用 `Response.successBuilder(data).build()`
- [x] Task 4: 编写测试 (AC: 1, 2, 3)
  - [x] Subtask 4.1: 编写 `RoleControllerTest`
  - [x] Subtask 4.2: 编写新添加的 `RoleService` 逻辑的测试，测试命名遵循 `test{MethodName}_{Scenario}_{ExpectedResult}`

## Dev Notes

### Technical Requirements

- **Frameworks:** Spring Boot 2.7.18, Spring Security 5.7.11, Java 1.8.
- **Dependency Injection:** Constructor Injection strictly required. No `@Autowired` on fields.
- **API Response:** Use standard `Response<T>` pattern.
- **Naming Conventions:** API uses plural forms (`/api/roles`), code uses camelCase/PascalCase. Database uses `snake_case`.
- **Auditing:** FR15 requires recording an audit event when permissions are assigned.

### Architecture Compliance

- **Module:** `security-components/security-user`
- **Interfaces:** Define in `security-user-api` (`com.original.security.user.api.RoleApi`, `com.original.security.user.api.dto`).
- **Implementation:** Define in `security-user-impl` (`com.original.security.user.controller.RoleController`).
- API interface contains Feign client definitions (`@FeignClient`), while the Controller implements it and adds `@RestController` and `@RequestMapping`.

### Previous Story Intelligence

- **Story 3.3:** `PermissionService` and `RoleService` were implemented with caching using DCL (Double-Checked Locking). Caching must be invalidated (`clearAllCache()` or `clearCache(username)`) when roles/permissions are updated to prevent stale data.
- **Story 3.2:** Created core `Role` and `Permission` entities along with their `Repository` interfaces.

### Git Intelligence Summary

- Recent commits (e.g. `feat(rbac): implement permission evaluation service`) highlight strict adherence to testing and architecture patterns.
- Tests should mock standard Spring components correctly, and Mockito `any()` matchers should account for casting.

### Project Structure Notes

- **API module:** `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/`
- **Impl module:** `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/controller/`

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 3.4]
- [Source: _bmad-output/project-context.md#结构规范]
- [Source: _bmad-output/implementation-artifacts/3-3-permission-evaluation-service.md#Previous Story Intelligence]

## Dev Agent Record

### Agent Model Used

Gemini 2.5 Pro

### Debug Log References

- Fixed compilation error regarding `Response` generic type code building by using `Response.<Void>withBuilder(400).msg(...).build()`.
- Handled Type mismatch list/set in `RoleServiceImpl` by creating a `HashSet` containing fetched `Permission` entities to satisfy the entity mapping requirement.
- Added `spring-boot-starter-validation` dependency to `security-user-api` to enable usage of JSR-303 constraints like `@NotBlank` and `@NotEmpty`.
- Added `spring-security-test` to `security-user-impl` for Controller-level MockMvc user context tests.

### Completion Notes List

- Designed standard request and response DTOs (`RoleCreateRequest`, `PermissionAssignRequest`, `RoleDTO`, `PermissionDTO`, `PageDTO`) containing JSR-303 annotations.
- Implemented `RoleApi` using `@FeignClient` exposing standard REST POST and GET mappings.
- Expanded `RoleService` and `RoleServiceImpl` to handle creation, retrieval, and permission assignments with cache clearance calling `clearAllCache()`.
- Hooked `RolePermissionAssignedEvent` publishing in permission assignment to comply with audit event requirement.
- Created `RoleController` to execute requests returning proper unified `Response.successBuilder(...)` schema.
- Added comprehensive unit tests via `RoleControllerTest` covering web layer mapping/exceptions, and expanded `RoleServiceImplTest` covering new features and failure cases ensuring 100% test passing metrics.

### File List

- `security-components/security-user/security-user-api/pom.xml` (Modified)
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/dto/request/RoleCreateRequest.java` (Added)
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/dto/request/PermissionAssignRequest.java` (Added)
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/dto/response/PermissionDTO.java` (Added)
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/dto/response/RoleDTO.java` (Added)
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/dto/response/PageDTO.java` (Added)
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/RoleApi.java` (Added)
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/service/RoleService.java` (Modified)
- `security-components/security-user/security-user-impl/pom.xml` (Modified)
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/controller/RoleController.java` (Added)
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/service/impl/RoleServiceImpl.java` (Modified)
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/event/RolePermissionAssignedEvent.java` (Added)
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/event/RolePermissionAssignedEventListener.java` (Added)
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/event/RoleCacheEvictionListener.java` (Added)
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/config/AsyncConfig.java` (Added)
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/controller/RoleControllerTest.java` (Added)
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/service/impl/RoleServiceImplTest.java` (Modified)
- `_bmad-output/implementation-artifacts/sprint-status.yaml` (Modified)

## Senior Developer Review (AI)

**审查日期：** 2026-03-03  
**审查模型：** Gemini 2.5 Pro (Adversarial Mode)

### 发现问题

| 级别 | 编号 | 问题 | 状态 |
|------|------|------|------|
| 🔴 HIGH | HIGH-1 | FR15 审计事件无监听器，日志从未记录 | ✅ 已修复 |
| 🔴 HIGH | HIGH-2 | `assignPermissions` 全量替换权限而非增量追加 | ✅ 已修复 |
| 🔴 HIGH | HIGH-3 | JSR-303 校验失败抛 `MethodArgumentNotValidException` 未处理，返回 500 | ✅ 已修复 |
| 🟡 MEDIUM | MEDIUM-1 | `sprint-status.yaml` 未列入 File List | ✅ 已修复 |
| 🟡 MEDIUM | MEDIUM-2 | `RoleController` 缺少显式 `@RequestMapping` | ✅ 已修复 |
| 🟡 MEDIUM | MEDIUM-3 | 无效 permissionId 被静默忽略，无错误反馈 | ✅ 已修复 |
| 🟡 MEDIUM | MEDIUM-4 | `createdAt` 在 service 层手动设置，与 `@PrePersist` 冗余 | ✅ 已修复 |
| 🟢 LOW | LOW-1 | `RolePermissionAssignedEvent` 返回可变列表 | ✅ 已修复 |
| 🟢 LOW | LOW-2 | 缺少 JSR-303 校验失败的 Controller 测试 | ✅ 已修复 |
| 🟢 LOW | LOW-3 | 使用 FQN `java.util.HashSet` 代替 import | ✅ 已修复 |

### 第二轮审查（R2）发现问题

| 级别 | 编号 | 问题 | 状态 |
|------|------|------|------|
| 🔴 HIGH | R2-HIGH-1 | `@Async` 无 `@EnableAsync` 支持，静默降级为同步执行 | ✅ 已修复 |
| 🔴 HIGH | R2-HIGH-2 | 事务提交前清空缓存导致其他线程可能读回旧数据 | ✅ 已修复 |
| 🟡 MEDIUM | R2-MEDIUM-1 | 重复 `permissionId` 导致 size 比较误判 "not found" | ✅ 已修复 |
| 🟡 MEDIUM | R2-MEDIUM-2 | 测试方法命名不符合 Story 约定 `test{Method}_{Scenario}_{Result}` | ✅ 已修复 |
| 🟡 MEDIUM | R2-MEDIUM-3 | `@WebMvcTest` 未隔离 `RolePermissionAssignedEventListener` 组件 | ✅ 已修复 |
| 🟢 LOW | R2-LOW-1 | `convertToDTO` 中永为真的 `null` 检查 | 📋 已知，低优先级 |
| 🟢 LOW | R2-LOW-2 | import 块间多余空白行（删除 import 遗留） | ✅ 已修复 |

### 第三轮审查（R3）发现问题

| 级别 | 编号 | 问题 | 状态 |
|------|------|------|------|
| 🟡 MEDIUM | R3-MEDIUM-1 | `onPermissionAssigned` 违反单一职责，public 方法泄露缓存清理 | ✅ 已修复 |
| 🟡 MEDIUM | R3-MEDIUM-2 | `listRoles` 缺少分页参数边界校验（page < 0 / size > 100） | ✅ 已修复 |
| 🟢 LOW | R3-LOW-1 | `onPermissionAssigned` 缓存清理逻辑缺少专属单元测试 | 📋 已知，低优先级 |
| 🟢 LOW | R3-LOW-2 | `RoleControllerTest` 中 `eq` import 仅使用一次 | 📋 已知，无害 |
