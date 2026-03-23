# BMAD 输出目录索引

本目录包含 Spring Security Boot 项目的 BMAD 工作流生成的所有规划和实现工件。

---

## 📋 规划文档 (planning-artifacts/)

规划阶段产生的战略和设计文档。

### 产品与架构

| 文档 | 描述 |
|------|------|
| [**prd.md**](./planning-artifacts/prd.md) | 产品需求文档，定义功能需求和非功能需求 |
| [**architecture.md**](./planning-artifacts/architecture.md) | 架构决策文档，技术选型和系统设计 |
| [**epics.md**](./planning-artifacts/epics.md) | Epic 和 Story 分解，完整的需求层级结构 |
| [**interface-specification.md**](./planning-artifacts/interface-specification.md) | 核心接口规范，AuthenticationPlugin 和 ConfigProvider |
| [**implementation-readiness-report-2026-02-25.md**](./planning-artifacts/implementation-readiness-report-2026-02-25.md) | 实施就绪检查报告 |

---

## 🔧 实施工件 (implementation-artifacts/)

实现阶段产生的 Story 文档和开发记录。

### Stories 按功能分组

#### Epic 0: 项目启动与数据基础
| 文档 | 状态 | 描述 |
|------|------|------|
| [0-1-define-core-interfaces.md](./implementation-artifacts/stories/0-1-define-core-interfaces.md) | ✅ 完成 | 定义核心接口 (AuthenticationPlugin, ConfigProvider) |
| [0-2-create-core-tables.md](./implementation-artifacts/stories/0-2-create-core-tables.md) | ✅ 完成 | 创建核心数据表 |

#### Epic 1: 框架基础与核心认证
| 文档 | 状态 | 描述 |
|------|------|------|
| [1-1-config-validation-startup-check.md](./implementation-artifacts/stories/1-1-config-validation-startup-check.md) | ✅ 完成 | 配置验证与启动检查 |
| [1-3-enable-security-boot-annotation.md](./implementation-artifacts/stories/1-3-enable-security-boot-annotation.md) | ✅ 完成 | @EnableSecurityBoot 注解实现 |
| [1-4-username-password-authentication.md](./implementation-artifacts/stories/1-4-username-password-authentication.md) | ✅ 完成 | 用户名密码认证 |
| [1-5-jwt-authentication-plugin.md](./implementation-artifacts/stories/1-5-jwt-authentication-plugin.md) | ✅ 完成 | JWT 认证插件 |
| [1-6-default-authentication-endpoints.md](./implementation-artifacts/stories/1-6-default-authentication-endpoints.md) | ✅ 完成 | 默认认证端点 |

#### Epic 2: 网络安全一体化
| 文档 | 状态 | 描述 |
|------|------|------|
| [2-1-cors-auto-configuration.md](./implementation-artifacts/2-1-cors-auto-configuration.md) | ✅ 完成 | CORS 自动配置 |
| [2-2-csrf-protection.md](./implementation-artifacts/2-2-csrf-protection.md) | ✅ 完成 | CSRF 防护 |
| [2-3-xss-protection-security-headers.md](./implementation-artifacts/2-3-xss-protection-security-headers.md) | ✅ 完成 | XSS 防护和安全响应头 |
| [2-4-global-security-filter.md](./implementation-artifacts/2-4-global-security-filter.md) | ✅ 完成 | 全局安全过滤器 |

#### Epic 3: 授权与权限控制
| 文档 | 状态 | 描述 |
|------|------|------|
| [3-1-preauthorize-support.md](./implementation-artifacts/3-1-preauthorize-support.md) | ✅ 完成 | @PreAuthorize 权限注解支持 |
| [3-2-role-permission-data-model.md](./implementation-artifacts/3-2-role-permission-data-model.md) | ✅ 完成 | 角色和权限数据模型 |
| [3-3-permission-evaluation-service.md](./implementation-artifacts/3-3-permission-evaluation-service.md) | ✅ 完成 | 权限评估服务 |
| [3-4-role-management-api.md](./implementation-artifacts/3-4-role-management-api.md) | ✅ 完成 | 角色管理 API |
| [3-5-dynamic-permission-loading.md](./implementation-artifacts/3-5-dynamic-permission-loading.md) | ✅ 完成 | 动态权限加载 |

#### Epic 4: 会话管理
| 文档 | 状态 | 描述 |
|------|------|------|
| [4-1-session-authentication-plugin.md](./implementation-artifacts/4-1-session-authentication-plugin.md) | ✅ 完成 | Session 认证插件 |
| [4-2-remember-me-functionality.md](./implementation-artifacts/4-2-remember-me-functionality.md) | ✅ 完成 | Remember Me 功能 |
| [4-3-session-query-api.md](./implementation-artifacts/4-3-session-query-api.md) | ✅ 完成 | 会话查询 API |
| [4-4-kick-user-offline.md](./implementation-artifacts/4-4-kick-user-offline.md) | ✅ 完成 | 踢人下线功能 |

#### Epic 5: 用户管理默认实现
| 文档 | 状态 | 描述 |
|------|------|------|
| [5-1-user-data-model-repository.md](./implementation-artifacts/5-1-user-data-model-repository.md) | ✅ 完成 | 用户数据模型和 Repository |
| [5-2-user-creation-api.md](./implementation-artifacts/5-2-user-creation-api.md) | ✅ 完成 | 用户创建 API |
| [5-3-user-query-api.md](./implementation-artifacts/5-3-user-query-api.md) | ✅ 完成 | 用户查询 API |
| [5-4-password-management-api.md](./implementation-artifacts/5-4-password-management-api.md) | ✅ 完成 | 密码管理 API |

#### Epic 6: 可观测性与测试支持
| 文档 | 状态 | 描述 |
|------|------|------|
| [6-1-audit-event-publishing.md](./implementation-artifacts/6-1-audit-event-publishing.md) | ✅ 完成 | 审计事件发布 |
| [6-2-metrics-indicators.md](./implementation-artifacts/6-2-metrics-indicators.md) | ✅ 完成 | Metrics 指标 |
| [6-3-health-check-endpoint.md](./implementation-artifacts/6-3-health-check-endpoint.md) | ✅ 完成 | 健康检查端点 |
| [6-4-testing-support-tools.md](./implementation-artifacts/6-4-testing-support-tools.md) | ✅ 完成 | 测试支持工具 |
| [6-5-structured-logging.md](./implementation-artifacts/6-5-structured-logging.md) | ✅ 完成 | 结构化日志 |
| [6-6-distributed-tracing.md](./implementation-artifacts/6-6-distributed-tracing.md) | ✅ 完成 | 分布式追踪 |

#### Epic 7: 开发者体验与文档
| 文档 | 状态 | 描述 |
|------|------|------|
| [7-1-quick-start-documentation.md](./implementation-artifacts/7-1-quick-start-documentation.md) | ✅ 完成 | 快速开始文档 |
| [7-2-configuration-reference-documentation.md](./implementation-artifacts/7-2-configuration-reference-documentation.md) | ✅ 完成 | 配置参考文档 |
| [7-3-api-reference-documentation.md](./implementation-artifacts/7-3-api-reference-documentation.md) | ✅ 完成 | API 参考文档 |
| [7-4-quick-start-example-project.md](./implementation-artifacts/7-4-quick-start-example-project.md) | ✅ 完成 | 快速开始示例项目 |
| [7-5-troubleshooting-guide.md](./implementation-artifacts/7-5-troubleshooting-guide.md) | ✅ 完成 | 故障排查指南 |
| [7-6-plugin-development-guide.md](./implementation-artifacts/7-6-plugin-development-guide.md) | ✅ 完成 | 插件开发指南 |
| [7-7-security-compliance-documentation.md](./implementation-artifacts/7-7-security-compliance-documentation.md) | ✅ 完成 | 安全合规文档 |

### 代码审查报告 (code-reports/)

| 文档 | 描述 |
|------|------|
| [0-1-define-core-interfaces-adversarial-review-2.md](./implementation-artifacts/code-reports/0-1-define-core-interfaces-adversarial-review-2.md) | Story 0-1 对抗性审查报告 |

---

## 📖 项目上下文

| 文档 | 描述 |
|------|------|
| [**project-context.md**](./project-context.md) | AI Agent 项目上下文，包含技术栈、代码规范和开发规则 |

---

## 📊 项目进度统计

| Epic | Stories | 状态 |
|------|---------|------|
| Epic 0: 项目启动与数据基础 | 2 | ✅ 完成 |
| Epic 1: 框架基础与核心认证 | 5 | ✅ 完成 |
| Epic 2: 网络安全一体化 | 4 | ✅ 完成 |
| Epic 3: 授权与权限控制 | 5 | ✅ 完成 |
| Epic 4: 会话管理 | 4 | ✅ 完成 |
| Epic 5: 用户管理默认实现 | 4 | ✅ 完成 |
| Epic 6: 可观测性与测试支持 | 6 | ✅ 完成 |
| Epic 7: 开发者体验与文档 | 7 | ✅ 完成 |
| **总计** | **37** | **100% 完成** |

---

*索引生成时间: 2026-03-23*
