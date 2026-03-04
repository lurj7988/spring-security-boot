# Story 3.5: dynamic-permission-loading

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a system admin / developer,
I want to dynamically load permissions and role mappings,
so that permission changes take effect immediately without requiring a system restart and evaluation performs under 5ms.

## Acceptance Criteria

1. **Given** an authenticated user
   **When** checking user permissions
   **Then** dynamically load user roles and permissions from the database
   **And** cache the permission data locally
   **And** subsequent checks hit the cache instead of the database

2. **Given** an admin modifies a role or permission
   **When** the permission data changes
   **Then** automatically invalidate or refresh the cache
   **And** the next user request evaluates using the updated permissions
   **Or** provide a manual cache clearing endpoint

3. **Given** permission evaluation load
   **When** running concurrent permission checks
   **Then** the permission check latency is < 5ms (cache hit)
   **And** satisfies NFR-PERF-001 load requirements

## Tasks / Subtasks

- [x] Task 1: Implement Dynamic Permission Loading and Caching (AC: 1, 3)
  - [x] Subtask 1.1: Integrate caching (e.g., Caffeine or Double-Checked Locking structure) into `PermissionService` / `RoleService` to store user-to-permissions mapping.
  - [x] Subtask 1.2: Ensure the permission evaluation logic first checks the cache, falling back to DB loading only on cache miss.
- [x] Task 2: Implement Cache Invalidation/Refresh mechanism (AC: 2)
  - [x] Subtask 2.1: Implement Spring Application Events or AOP to listen for Role/Permission modification events.
  - [x] Subtask 2.2: Ensure `RoleService.clearAllCache()` or `clearCache(username)` correctly flushes the stale data.
- [x] Task 3: Performance and Load Testing (AC: 3)
  - [x] Subtask 3.1: Write unit and integration tests to verify the cache hit/miss logic and invalidation.
  - [x] Subtask 3.2: Write benchmark/performance tests ensuring repeated `hasPermission()` checks return in < 5ms.
- [x] Task 4: Externalize Cache Configuration (AI-Review Fix)
  - [x] Subtask 4.1: Move hardcoded cache size and TTL to `SecurityProperties` with `@ConfigurationProperties`.
  - [x] Subtask 4.2: Update services and tests to use dynamic configuration.

## Dev Notes

### Technical Requirements

- **Frameworks:** Spring Boot 2.7.18, Spring Security 5.7.11, Java 1.8.
- **Dependency Injection:** Constructor Injection strictly required. No `@Autowired` on fields.
- **Performance:** NFR-PERF-001 dictates strict latency constraints. Caching mechanism must be highly concurrent (e.g., `ConcurrentHashMap` with careful locking or `Caffeine`).
- **Events:** Use Spring's `ApplicationEventPublisher` for decoupling cache invalidation from the modification logic.

### Architecture Compliance

- **Module:** `security-components/security-user` and `security-core` (if dealing with `AccessDecisionManager`).
- **Interfaces:** Keep caching logic encapsulated behind `PermissionService` and `RoleService` abstractions.

### Previous Story Intelligence

- **Story 3.4:** `RoleService.clearAllCache()` was introduced. We must hook into it and ensure it's properly propagated when roles/permissions are assigned.
- **Story 3.3:** `PermissionService` and `RoleService` were implemented with rudimentary caching using DCL. This story must refine, robustly test, and ensure the dynamic reload behaves perfectly without race conditions under high load.

### Git Intelligence Summary

- Prior commits established strict adherence to event-driven architectures (e.g., `RolePermissionAssignedEvent`, `RoleCacheEvictionListener`).
- Ensure all new caching tests leverage Mockito to verify the exact number of repository invocations (e.g., verifying DB is called exactly once per user until invalidated).

### Project Structure Notes

- **Service logic:** `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/service/impl/`
- **Events:** `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/event/`

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 3.5]
- [Source: _bmad-output/implementation-artifacts/3-4-role-management-api.md]

## Dev Agent Record

### Agent Model Used

Gemini 2.5 Pro

### Debug Log References

- `RoleControllerTest` failed due to missing `spring-boot-starter-validation` dependency in `security-user-impl` module (which was inherited as `<optional>true</optional>` from `security-user-api`). Fixed by declaring it explicitly in `security-user-impl/pom.xml`.
- Fix compilation error where `RolePermissionAssignedEvent.getTimestamp()` conflicted with `ApplicationEvent.getTimestamp()` (since Spring 5.2/Boot 2.7 `ApplicationEvent.getTimestamp()` is final). Renamed property to `assignedAt`.
- Fixed Caffeine cache behavior for disabled users by returning `null` from the loading function instead of `Optional.empty()` to correctly prevent caching missing/disabled users.
- Warm-up phase added to `PermissionServicePerformanceTest` to allow accurate latency measurement free of JIT compilation and thread initialization spikes.

### Completion Notes List

- Switched rudimentary DCL implementation in `PermissionServiceImpl` and `RoleServiceImpl` to use `Caffeine` cache for high performance.
- Added `Caffeine` dependency to `security-user-impl`.
- Updated `RoleCacheEvictionListener` to evict caches in BOTH `RoleService` and `PermissionService` upon permission assignment.
- Successfully created a multithreaded performance test `PermissionServicePerformanceTest` which verifies concurrent access latency is well under 5ms.
- **AI Review Fixes**: Moved hardcoded `MAX_CACHE_SIZE` and `TTL` parameters to `SecurityProperties` in `security-core` and renamed `maxPoolSize` to `maximumSize` for clarity.
- **AI Review Fixes**: Synchronized all modified files into the File List for better traceability.
- **AI Review Fixes (Antigravity)**: Implemented granular cache invalidation in `RoleCacheEvictionListener` (findByRoles_Name) to prevent "Thunder Herd" database spikes on permission updates.
- **AI Review Fixes (Antigravity)**: Removed redundant null checks in service implementations.
- All unit tests passing (including performance tests), compilation successful.

### File List

- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/api/RoleApi.java`
- `security-components/security-user/security-user-impl/pom.xml`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/controller/RoleController.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/service/impl/PermissionServiceImpl.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/service/impl/RoleServiceImpl.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/event/RoleCacheEvictionListener.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/event/RolePermissionAssignedEvent.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/event/RolePermissionAssignedEventListener.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/controller/RoleControllerTest.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/service/impl/PermissionServiceImplTest.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/service/impl/RoleServiceImplTest.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/service/impl/PermissionServicePerformanceTest.java`
- `security-core/src/main/java/com/original/security/config/SecurityProperties.java`

## Senior Developer Review (AI)

### 🔴 High Severity Fixes

- **Completed AC 2**: Added `/api/roles/cache` endpoint to `RoleController` to allow manual cache clearing.
- **Cache Policy Security**: Implemented 30-minute TTL in Caffeine caches for both roles and permissions to ensure eventual consistency.
- **Negative Caching**: Fixed "Disabled User DoS" risk by caching `Collections.emptySet()` for non-existent or disabled users, preventing database penetration.

### 🟡 Medium Severity Fixes

- **Auditing Integrity**: Fixed incorrect timestamp reference in `RolePermissionAssignedEventListener`.
- **Cache Invalidation Consistency**: Verified `RoleCacheEvictionListener` properly clears both service caches upon any permission assignment.

### 🟢 Gaps / Future Work

- **Done**: Moved `MAX_CACHE_SIZE` and `TTL` parameters to `@ConfigurationProperties` (SecurityProperties).
- Performance tests verify latency but could be further improved with cache hit ratio metrics.

Reviewer: Antigravity (AI) on 2026-03-03
