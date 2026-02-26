---
stepsCompleted: ['step-01-init', 'step-02-discovery', 'step-02b-vision', 'step-02c-executive-summary', 'step-03-success', 'step-04-journeys', 'step-05-domain', 'step-06-innovation', 'step-07-project-type', 'step-08-scoping', 'step-09-functional', 'step-10-nonfunctional', 'step-11-polish', 'step-12-complete']
inputDocuments:
  - name: project-context.md
    path: _bmad-output/project-context.md
    type: project_context
    loaded: true
workflowType: 'prd'
documentCounts:
  briefs: 0
  research: 0
  brainstorming: 0
  projectDocs: 12
classification:
  projectType: Developer Tool / B2B Framework / 开源项目
  domain: Developer Tools / Security / Identity Management
  complexity: Medium
  projectContext: brownfield
  businessModel: 社区驱动 + 增值服务
firstPrinciples:
  本质定位: 让认证不再是开发障碍的开发工具
  核心价值: 统一的安全边界守护 + 插件化的登录方式
  架构原则: 清晰的模块边界（非完整 DDD）
  开发者体验: 认知负荷最小化 > 功能丰富度
  开源策略: 社区驱动的生态建设
keyFeatures:
  - 配置清晰启动（智能默认值，最小化配置）
  - 统一的安全 API（谁可以访问什么）
  - 插件化的认证提供者（JWT、Session、短信等）
  - 渐进式配置（按需定制）
  - 支持微服务场景
  - 默认用户认证管理体系
  - MySQL 存储，支持其他数据库扩展
  - 清晰的模块边界设计
  - 网络安全一体化（CORS、CSRF、XSS、安全响应头等）
coreModules:
  - 认证引擎核心
  - 用户管理模块
  - 权限控制模块
  - 扩展插件系统
  - 认证提供者插件（JWT、Session、短信）
  - 第三方登录适配器
  - SSO 统一认证支持
  - 数据库抽象层（仓储模式）
  - 微服务场景支持
  - 网络安全防护模块（新增）
keyInsights:
  - "配置清晰优于零配置"
  - "插件化优于全功能集成"
  - "模块化设计优于完整 DDD"
  - "开发者体验是核心竞争力"
  - "社区驱动优于开源+付费模式"
productVision:
  核心定位: 让认证不再是开发障碍的开发工具
  愿景: 一个让开发者轻松实现企业级安全认证的 Spring Security 框架
  差异化: 10分钟集成、开箱即用的网络安全解决方案
  核心洞察: 让 Spring Security 变得简单，而不是替换它
  价值主张: 唯一一个 10 分钟集成、开箱即用的 Spring Security 网络安全解决方案
---

# 产品需求文档 - Spring Security Boot

**作者:** Naulu
**日期:** 2026-02-24

## 第一性原理：项目本质

### 核心定位
**让认证不再是开发障碍的开发工具**

### 本质真理
- ✅ **开发者真正需要的**：能够快速解决认证问题，不阻碍业务开发
  - 快速集成（时间成本 < 1 天）
  - 功能正确性（能够工作）
  - 可维护性（长期演进）
  - 文档完善（遇到问题能解决）

### 核心价值
**统一的安全边界守护 + 插件化的登录方式 + 网络安全一体化**
- 认证框架 = 安全边界守护者（谁可以访问什么）
- 登录方式 = 实现细节（通过插件提供）
- 网络安全 = 自动配置的防护（CORS、CSRF、XSS 等）

---

## 产品愿景

构建**开发者友好的认证与网络安全框架**，通过配置清晰和插件化扩展，让企业级安全防护变得简单。

**这不是一个要替换 Spring Security 的框架，而是让 Spring Security 变得简单易用的增强层。**

---

## 产品定位

### 目标用户
- 企业级应用开发者
- 需要快速集成认证功能的团队
- 微服务架构项目
- 希望专注业务而非安全开发的开发者

### 核心价值
- **配置清晰** - 智能默认值，只需配置数据库
- **快速上手** - 15-30 分钟完成集成
- **配置清晰** - 不需要成为 Spring Security 专家
- **安全可靠** - 经过验证的安全实践
- **网络安全一体化** - 认证 + 网络安全防护
- **插件化扩展** - 按需定制，渐进式配置

---

## 差异化优势

### 对比现有方案

**与 Keycloak 的差异：**
- ✅ 更轻量 - 专注于 Spring 生态
- ✅ 更简单 - 不需要复杂的 Keycloak 部署
- ✅ 更直观 - 配置清晰，文档完善

**与 Sa-Token 的差异：**
- ✅ Spring Security 原生集成 - 充分利用 Spring 生态
- ✅ 更强大 - 企业级网络安全防护
- ✅ 更灵活 - 插件化架构，易于扩展

**与原生 Spring Security 的差异：**
- ✅ 开箱即用 - 自动化的最佳配置
- ✅ 网络安全一体 - CORS/CSRF/XSS 自动配置
- ✅ 简化使用 - 不需要深入理解复杂概念

### 核心差异化

**"功能对标 Sa-Token，基于 Spring Security 原生集成，架构设计更优雅的企业级认证框架。"**

---

## 核心特性

### 1. 零配置启动
- 自动化的安全配置
- 智能默认值
- 最小化配置需求

### 2. 插件化认证提供者
- JWT 认证插件
- Session 认证插件
- 短信验证码插件
- 第三方登录插件（微信、GitHub 等）

### 3. 统一的安全 API
- 标准化的权限控制接口
- 一致的用户认证 API
- 统一的会话管理

### 4. 网络安全防护
- CORS 自动配置
- CSRF 防护
- XSS 防护
- SQL 注入防护
- 安全响应头自动配置
- 请求频率限制
- IP 白名单/黑名单

### 5. 开发者体验优先
- 10 分钟快速上手
- 配置智能提示
- 清晰的错误反馈
- 完善的文档和示例

---

## 架构设计

### 模块边界（清晰分离）

```
┌─────────────────────────────────────┐
│          认证引擎核心                │
│  (安全边界守护者)                     │
│  - AuthenticationManager              │
│  - AccessDecisionManager             │
│  - SecurityFilterChain               │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│        扩展插件系统                   │
│  - 认证提供者接口                    │
│  - 用户详情服务接口                  │
│  - 权限评估接口                      │
└─────────────────────────────────────┘
              ↓
┌──────────────────┬──────────────────┐
│   用户管理模块    │   权限控制模块    │
│  (User Aggregate)  │  (Role/Permission) │
└──────────────────┴──────────────────┘
```

### 架构原则
- **模块化设计** - 清晰的边界，非完整 DDD
- **接口隔离** - 定义清晰的扩展点
- **依赖倒置** - 核心不依赖具体实现
- **单一职责** - 每个模块职责明确

---

## 开源策略

### Apache 2.0 许可
- 完全开源核心
- 社区友好的许可
- 鼓励商业使用

### 增值服务
- 官方支持的集成适配器（企业级第三方登录）
- 企业级培训服务
- 技术咨询服务

### 社区建设
- 清晰的贡献指南
- 友好的 PR 模板
- 活跃的 Issue 讨论

---

## 成功标准

### 用户成功

**功能对标 Sa-Token：**
- MVP 覆盖率 70%（核心认证场景）
- Growth 阶段 90%（主流功能）
- Vision 版本 100%（完整对标）

**集成体验：**
- 30分钟内完成基础集成
- 必填配置项 ≤ 4 个（数据库 3 个 + JWT 密钥 1 个）
- 配置项自解释率 >80%（不需要查文档就能理解）
- 配置错误修复率 >90%（根据错误提示能自己修）
- 首次启动成功率 >85%

**Spring Security 原生体验：**
- 标准 Spring Security API（无学习新 API 成本）
- 开发者已有的知识可以直接复用
- 与 Spring 生态无缝集成

**网络安全一体化（Sa-Token 没有的独特价值）：**
- CORS 自动配置
- CSRF 防护
- XSS 防护
- 安全响应头自动配置
- 网络安全自动配置覆盖率 100%

**情感成功时刻：**
- "我添加了依赖，配置好数据库，启动就成功了"
- "安全审计通过了，因为 CORS/CSRF/XSS 都自动配置好了"
- "这就是 Spring Security，只是更简单了"
- "配置项很清楚，不需要猜这是什么"
- "报错信息直接告诉我哪里错了，而不是堆栈跟踪"

### 业务成功

**社区增长：**
- 3个月: GitHub Stars 200
- 6个月: GitHub Stars 500
- 12个月: GitHub Stars 2,800（对标 Sa-Token）

**开发者采用：**
- 首次集成成功率 >85%
- 文档完整度评分 >4.5/5.0
- "比 Sa-Token 架构更好" 提及率 >40%
- "比原生 Spring Security 简单" 提及率 >50%
- "网络安全一体化" 独特价值提及率 >40%

**企业级采用：**
- 企业级项目占比 >60%
- 生产环境部署案例 >10 个

**社区健康度：**
- Issue 48小时内首次响应率 >90%
- PR 48小时审核率 >80%
- 安全漏洞 24小时内响应
- 活跃贡献者 >20 人

### 技术成功

**架构质量：**
- 模块化架构（AuthenticationPlugin 接口）
- 清晰的插件接口设计
- 新功能开发不修改核心代码
- 构造器依赖注入规范（100% 覆盖）

**代码质量：**
- 测试覆盖率 >80%
- SonarQube 评分 >A
- 单元测试 + 集成测试 + 性能测试
- 新功能必须有测试才能合并

**性能指标：**
- 认证响应时间 <100ms (P95)
- 框架启动时间 <3秒
- 依赖冲突率 0（通过 BOM 管理）

**兼容性：**
- Spring Boot 2.x / 3.x 支持
- Java 8 / 11 / 17 兼容性
- Spring Security 5.x / 6.x 覆盖
- 向后兼容性承诺：至少支持一个大版本
- 框架升级成本 <1天工作量

**安全承诺：**
- 0 高危漏洞发布
- 24小时内响应并修复高危漏洞
- 安全扫描 100% 通过
- 配置验证覆盖率 100%（启动时验证所有必填配置）

### 可测量成果

**3个月里程碑：**
- GitHub Stars 200
- 活跃贡献者 5 人
- 企业级案例 2 个
- 核心功能 MVP 发布

**6个月里程碑：**
- GitHub Stars 500
- 活跃贡献者 10 人
- 企业级案例 5 个
- Growth 功能发布

**12个月里程碑：**
- GitHub Stars 2,800
- 活跃贡献者 20 人
- 企业级案例 10 个
- 功能对标 Sa-Token 90%

---

## 产品范围

### MVP - 最小可行产品（2.5个月，15个核心功能）

**核心价值：功能对标 Sa-Token 70% + Spring Security 原生 + 网络安全一体化**

**认证场景（5个）：**
1. 用户名密码认证
2. JWT Token 认证（前后端分离标准）
3. Session 认证（传统 Web）
4. Remember Me（七天内免登录）
5. 单端登录

**权限认证（4个）：**
1. 注解式鉴权（@PreAuthorize）
2. 路由拦截式鉴权
3. 角色认证（@PreAuthorize("hasRole()")）
4. 权限认证（@PreAuthorize("hasAuthority()")）

**会话管理（4个）：**
1. 根据账号 id 踢人下线
2. 会话查询接口
3. Http Basic 认证
4. 会话事件监听

**网络安全（5个，Sa-Token 没有的独特价值）：**
1. CORS 自动配置
2. CSRF 防护
3. XSS 防护
4. 安全响应头（X-Frame-Options, X-Content-Type-Options 等）
5. 全局过滤器（跨域、安全响应头）

**持久层（2个）：**
1. MySQL 默认支持
2. 用户/角色/权限管理

**开发者体验（5个）：**
1. 构造器依赖注入规范
2. Jackson JSON 序列化
3. 配置验证和错误提示
4. 一键运行示例项目
5. 故障排查快速参考

**基础设施（3个）：**
1. 配置项自解释
2. 清晰的错误提示和修复建议
3. 完整文档（快速开始、配置参考、API 文档、故障排查）

### Growth - 增长特性（MVP 后）

**会话增强：**
1. 多端登录（手机电脑同时在线）
2. 同端互斥登录（两个手机上互斥）
3. 全端共享 Session（Spring Session）
4. 单端独享 Session
5. 账号封禁
6. 二级认证
7. 会话二级认证

**分布式支持：**
1. Redis 集成（会话持久化）
2. 独立 Redis（权限缓存与业务缓存分离）
3. 分布式会话
4. 微服务网关鉴权（Gateway）
5. RPC 调用鉴权
6. Feign Token 传播

**认证扩展：**
1. 短信验证码认证插件
2. 第三方登录适配器（微信、GitHub、Google）
3. OAuth2.0 服务端
4. 单点登录（SSO）- 同域、跨域
5. 单点注销
6. Quick 快速登录认证（零代码注入登录页）
7. Token 风格定制
8. 临时 Token 认证
9. 自动续签

**多账号体系：**
1. 多 SecurityFilterChain 支持（User 表和 Admin 表分开鉴权）
2. 自定义 Session

**高级特性：**
1. 临时身份切换
2. 模拟他人账号
3. 参数签名（跨系统 API 调用签名校验）
4. 请求频率限制
5. IP 白名单/黑名单

**开发者体验：**
1. 全局侦听器（登录/注销/被踢下线事件）
2. Thymeleaf 标签方言
3. 配置验证工具
4. 性能基准测试

### Vision - 愿景版本

**完整对标 Sa-Token：**
- 所有 Sa-Token 功能 100% 覆盖
- 开箱即用（SpringMVC、WebFlux、Solon）
- 最新技术栈（Spring Boot 3.x、JDK 17）

**企业级增强：**
1. RPC 调用状态传递（Dubbo、gRPC 集成）
2. AI 辅助安全配置建议
3. 自动化的安全审计报告
4. 企业级培训服务
5. 技术咨询服务
6. 云原生部署最佳实践（Kubernetes）

**生态系统：**
1. 社区贡献插件 >5 个
2. 企业级集成适配器
3. 官方认证的第三方服务集成

---

## 用户旅程

### 角色 1：应用开发者 - "忙碌的后端开发小李"

**背景设置：**
- **姓名**：小李
- **角色**：3年经验的 Java 后端开发者
- **现状**：正在开发一个电商后台管理系统，需要在 2 周内完成用户认证模块
- **痛点**：
  - 之前尝试直接用 Spring Security，配置太复杂，花费 3 天还没跑通
  - 老板催促进度，压力很大
  - 网上教程质量参差不齐，不知道哪个靠谱

**目标**：快速完成认证功能，不阻碍业务开发

**障碍**：Spring Security 学习曲线陡峭，安全配置容易出错

**解决方案**：Spring Security Boot

---

**旅程故事：**

**Day 1 - 上午 9:00：尝试启动**
小李添加依赖，配置数据库，启动应用...
❌ **错误！** `Connection refused: localhost:3306`
💭 *心理状态*：焦虑（"完了，第一步就失败了"）
🔧 **恢复**：查看错误提示，文档说"确保 MySQL 已启动"
✅ 修复：启动 MySQL，重新启动 → 成功！控制台打印："Spring Security Boot initialized with default configuration. CORS, CSRF, XSS protection enabled."

**Day 1 - 上午 10:30：测试登录**
小李测试登录接口...
❌ **错误！** `401 Unauthorized`
💭 *心理状态*：沮丧（"为什么别人可以成功？"）
🔧 **恢复**：查看文档"快速故障排查"，发现需要先创建用户
✅ 修复：调用用户创建 API → 登录成功！返回 JWT Token

**Day 1 - 下午 3:00：前端集成**
前端同事调用接口...
❌ **错误！** `CORS policy blocked`
💭 *心理状态*：崩溃（"又是 CORS 问题！"）
🔧 **恢复**：查看配置，发现框架已经自动配置了 CORS
✅ 修复：只需要在配置里添加前端域名 → 成功！

**Day 1 - 下午 5:00：完成集成**
小李完成集成，如释重负。
💭 *心理状态*：成就感（"虽然遇到了问题，但错误提示很清晰，都能自己修"）

**Day 3 - 代码评审：**
周三，代码评审。架构师王工看到代码后说："这个配置很清晰，而且用的是标准 Spring Security API，团队容易维护。" 小李第一次在技术评审中受到表扬。

**Day 14 - 系统上线：**
2 周后系统顺利上线。安全审计报告显示："CORS、CSRF、XSS 防护配置正确，无高危漏洞。" 老板拍着小李的肩膀说："干得漂亮，下个项目你也用这个框架。"

💭 *结局*：小李松了一口气，终于可以专注于业务逻辑了，而不是整天研究安全配置。

---

### 角色 2：架构师 - "追求完美的王工"

**背景设置：**
- **姓名**：王工
- **角色**：某互联网公司架构师，15 年经验
- **现状**：公司要开发微服务架构项目，需要统一的认证方案
- **痛点**：
  - Sa-Token 功能丰富但不是 Spring 原生，担心长期维护
  - 原生 Spring Security 太复杂，团队学习成本高
  - Keycloak 太重，不想引入复杂的独立服务

**目标**：选择一个 Spring 生态内、架构优秀、功能完整的认证框架

**障碍**：市场上没有完美的选择

**解决方案**：Spring Security Boot

---

**旅程故事：**

**开场场景**：周五下午的技术委员会会议，CTO 问王工："微服务的认证方案选好了吗？Sa-Token、Shiro、还是直接用 Spring Security？" 王工犹豫了，每个选择都有问题...

**上升动作 - 周末研究：**
周末，王工在家研究框架对比。他发现了 Spring Security Boot：

- ✅ 功能对标 Sa-Token（30+ 认证场景）
- ✅ 基于 Spring Security 原生（团队现有知识可复用）
- ✅ 模块化架构（AuthenticationPlugin 接口设计清晰）
- ✅ 网络安全一体化（CORS/CSRF/XSS 自动配置）

王工打开源码，看到构造器依赖注入、清晰的接口设计、完善的测试。他心里想："这个代码质量不错，架构师用心了。"

**周一 - 技术提案：**
王工写了一篇技术对比文档，推荐使用 Spring Security Boot：
```
# 微服务认证方案对比

| 方案 | 优势 | 劣势 | 推荐度 |
|------|------|------|--------|
| Sa-Token | 功能丰富 | 非 Spring 原生 | ⭐⭐ |
| Shiro | 成熟 | 过时、社区不活跃 | ⭐⭐ |
| Spring Security | 标准 | 配置复杂 | ⭐⭐⭐ |
| Spring Security Boot | Spring 原生 + 功能完整 + 架构优秀 | 新项目 | ⭐⭐⭐⭐⭐ |
```

团队讨论后一致同意。

**高潮时刻 - 3个月后：**
微服务系统稳定运行。新来的小张问："为什么选这个框架？" 王工自信地说："它是 Spring 生态内最完整的认证方案，架构优秀，团队容易上手。"

**结局：**
公司在技术博客上分享了架构选型经验，Spring Security Boot 的社区影响力扩大。王工因为这次正确的技术选型，获得了年度最佳架构奖。

---

### 角色 3：框架贡献者 - "热爱开源的小张"

**背景设置：**
- **姓名**：小张
- **角色**：5 年经验的开发者，热爱开源
- **现状**：在使用 Spring Security Boot 开发项目时，发现了一个功能需求
- **痛点**：
  - 公司需要短信验证码登录
  - 框架还没有这个功能
  - 不想自己维护私有版本

**目标**：为框架贡献短信验证码插件，帮助更多开发者

**障碍**：担心贡献流程复杂，代码不被接受

**解决方案**：Spring Security Boot 的清晰插件接口

---

**旅程故事：**

**开场场景**：小张在使用 Spring Security Boot 时，发现需要短信验证码功能。他查看文档，发现有清晰的 `AuthenticationPlugin` 接口...

**上升动作 - 创建插件：**
小张按照文档创建插件：

```java
public class SmsAuthenticationPlugin implements AuthenticationPlugin {
    @Override
    public String getName() {
        return "sms-authentication";
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return new SmsAuthenticationProvider();
    }
}
```

小张按照贡献指南提交了 Pull Request。24 小时内，项目维护者回复了：
```
@xiaozhang 感谢贡献！代码结构很好，只需要调整一下日志格式：
- 使用 slf4j 而不是 System.out
- 添加错误处理的单元测试
```

小张快速修改后，PR 被合并了！

**高潮时刻 - 版本发布：**
下一个版本发布时，Release Notes 写着：
```
## v1.2.0 (2026-03-15)

### 新增功能
- ✨ 短信验证码认证插件 (感谢 @xiaozhang)

### 贡献者
- @xiaozhang
```

小张在朋友圈分享了这个消息，朋友们纷纷点赞："哇，你的代码被官方框架采用了！"

**结局：**
小张成为了框架的活跃贡献者，结识了很多优秀的开发者。他的 GitHub 主页上，"Contributed to Spring Security Boot" 的徽章闪闪发光。

---

### 角色 4：运维工程师 - "压力山大的运维老陈"

**背景设置：**
- **姓名**：老陈
- **角色**：10年运维经验的资深工程师
- **现状**：公司上线新系统，需要确保稳定运行
- **痛点**：
  - 之前用过的框架日志混乱，出问题很难排查
  - 监控指标不明确，不知道要看什么
  - 升级时经常出现兼容性问题

**目标**：系统稳定运行，出问题能快速定位

**障碍**：框架缺乏运维友好性

**解决方案**：Spring Security Boot

---

**旅程故事：**

**开场场景**：凌晨2点，监控系统报警："认证服务响应超时"。老陈从床上爬起来，打开电脑...

**上升动作 - 故障排查：**
老陈查看日志，看到清晰的错误信息：
```
2026-02-24 02:00:15.123 ERROR [SecurityFilterChain] Authentication failed: Database connection timeout
  at SecurityAuthenticationFilter.java:145
  Root cause: com.mysql.cj.jdbc.CommunicationsException: Communications link failure
  Recommendation: Check database connectivity and connection pool settings
```

日志清晰地指出问题：数据库连接超时。老陈检查数据库，发现 MySQL 连接数满了。

老陈查看框架提供的健康检查端点：
```bash
curl http://localhost:3001/actuator/health/security
{
  "status": "DOWN",
  "details": {
    "database": "DOWN - Connection timeout",
    "jwtValidator": "UP",
    "cache": "UP"
  }
}
```

**高潮时刻 - 问题解决：**
老陈调整了 MySQL 连接池配置，重启应用。5分钟后，系统恢复。

第二天，老陈在团队会议上说："这个框架的日志和监控做得很好，出问题能快速定位。建议推广到其他系统。"

**结局：**
老陈在公司的运维手册中，把 Spring Security Boot 标记为"推荐框架 - 运维友好"。

---

### 角色 5：安全审计员 - "严谨的审计员小刘"

**背景设置：**
- **姓名**：小刘
- **角色**：第三方安全公司审计员
- **现状**：对某公司进行等保 2.0 安全审计
- **痛点**：
  - 很多系统的安全配置不正确，导致审计失败
  - 开发者不重视安全，认为是"阻碍业务"
  - 需要反复沟通才能修复问题

**目标**：通过等保 2.0 认证

**障碍**：安全配置缺失或不正确

**解决方案**：Spring Security Boot

---

**旅程故事：**

**开场场景**：小刘打开审计工具，扫描目标系统...

**上升动作 - 安全扫描：**
小刘看着扫描报告，眼睛亮了：
```
✅ CORS 配置：正确（允许的域名已配置）
✅ CSRF 防护：已启用
✅ XSS 防护：已启用
✅ 安全响应头：
   ✅ X-Frame-Options: DENY
   ✅ X-Content-Type-Options: nosniff
   ✅ Strict-Transport-Security: max-age=31536000
✅ 密码加密：BCrypt (强度 10)
✅ Token 过期时间：合理 (60分钟)
✅ 会话管理：安全的随机数生成
```

小刘检查代码，发现框架自动配置了这些安全功能：
```java
// 框架自动配置的安全过滤器
@Configuration
public class SecurityAutoConfiguration {
    @Bean
    public SecurityFilterChain filterChain() {
        // CORS、CSRF、XSS 防护已自动启用
        // 开发者不需要手动配置
    }
}
```

**高潮时刻 - 审计通过：**
审计团队的组长看着报告说："这个系统的安全配置做得很好，可以直接通过等保 2.0 认证。很少有公司能把 Spring Security 配置得这么正确。"

技术负责人笑着说："我们用了 Spring Security Boot 框架，它会自动配置这些安全项。"

小刘心想："这个框架应该推广给更多公司，可以减少很多安全问题。"

**结局：**
小刘在审计报告中特别提到："推荐使用 Spring Security Boot 框架，可满足等保 2.0 认证要求。"

---

### 用户旅程需求总结

**从小李的旅程（应用开发者）：**
- ✅ 清晰的错误提示和修复建议
- ✅ 快速故障排查文档
- ✅ 配置验证工具
- ✅ 一键运行示例项目

**从王工的旅程（架构师）：**
- ✅ 模块化架构设计
- ✅ 代码质量展示（构造器注入、测试覆盖）
- ✅ 技术对比文档
- ✅ 架构决策记录（ADR）

**从小张的旅程（框架贡献者）：**
- ✅ 清晰的插件接口（AuthenticationPlugin）
- ✅ 贡献者指南
- ✅ 友好的 PR 审核流程
- ✅ 24小时内响应承诺

**从老陈的旅程（运维工程师）：**
- ✅ 结构化日志（包含错误上下文和修复建议）
- ✅ 健康检查端点（actuator/health/security）
- ✅ 监控指标文档
- ✅ 升级兼容性承诺

**从小刘的旅程（安全审计员）：**
- ✅ 网络安全自动配置（CORS/CSRF/XSS）
- ✅ 安全响应头自动配置
- ✅ 密码加密（BCrypt）
- ✅ 等保 2.0 合规文档
- ✅ 安全配置清单

---

## 领域特定需求

### 核心原则：框架提供能力，应用负责使用

**关键区分：**
- Spring Security Boot 是一个**安全框架**，不是安全产品
- 框架提供安全能力（加密、认证、授权）
- 应用负责正确使用这些能力来保护用户数据

### 合规与监管要求

**等保 2.0（网络安全等级保护）支持：**

| 等保 2.0 要求 | 框架提供的能力 | 应用需要做的 |
|-------------|--------------|-------------|
| 身份鉴别 | 多种认证方式（用户名密码、JWT、短信等） | 实施具体认证逻辑 |
| 访问控制 | @PreAuthorize、@Secured 等注解 | 定义具体权限规则 |
| 安全审计 | 审计事件发布机制 | 记录和保存审计日志（≥ 6 个月） |
| 数据完整性 | JWT 签名验证、HTTPS 强制 | 保护数据传输 |
| 数据保密性 | BCrypt 加密、敏感数据脱敏 API | 使用加密工具保护数据 |

**框架提供的等保 2.0 支持：**
- ✅ 等保 2.0 合规检查清单
- ✅ 等保 2.0 合规示例项目
- ✅ 等保 2.0 合规文档
- ❌ 不承诺"用这个框架就自动通过等保"（应用也需要正确使用）

**国际标准支持：**

| 标准/法规 | 适用地区 | 核心要求 | 框架支持 |
|---------|---------|---------|---------|
| GDPR | 欧盟 | 数据保护、用户同意 | ✅ 数据脱敏、隐私 API |
| SOC 2 | 美国 | 安全控制、审计 | ✅ 审计事件 API |
| ISO 27001 | 全球 | 信息安全管理体系 | ✅ 安全控制文档 |
| PCI DSS | 支付行业 | 支付数据保护 | ✅ 加密、审计支持 |

### 技术约束

**框架提供的安全能力：**
- ✅ 加密：BCrypt（强度 10）、Argon2、PBKDF2
- ✅ Token 安全：JWT 签名验证、过期时间、刷新机制
- ✅ 会话管理：安全的会话 ID 生成、会话过期、会话固定防护
- ✅ 审计：审计事件发布机制（应用负责记录和保存）
- ✅ 脱敏：敏感数据脱敏 API（日志中不记录密码、Token）

**框架不负责：**
- ❌ 存储用户密码（应用存储）
- ❌ 保存审计日志（应用保存）
- ❌ 直接处理用户数据

### 集成要求

**标准协议支持（MVP）：**
- ✅ JWT 认证
- ✅ OAuth 2.0 客户端
- ✅ HTTP Basic 认证

**标准协议支持（Growth）：**
- 🔲 OAuth 2.0 授权服务器
- 🔲 OpenID Connect (OIDC)
- 🔲 SAML 2.0
- 🔲 LDAP / Active Directory

**企业系统集成（Growth）：**
- 🔲 单点登录（SSO）
- 🔲 多因素认证（MFA）
- 🔲 企业目录服务集成

### 风险缓解

**已知风险和缓解措施：**

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 配置错误 | 高 | 中 | ✅ 配置验证、默认安全配置、错误提示 |
| 密码弱加密 | 高 | 低 | ✅ BCrypt 默认强度 10、禁止 MD5/SHA1 |
| Token 泄露 | 中 | 中 | ✅ Token 过期、刷新机制、HTTPS 强制 |
| 会话劫持 | 中 | 低 | ✅ 安全会话管理、IP 绑定选项 |
| 依赖漏洞 | 高 | 中 | ✅ 依赖扫描、快速响应、定期更新 |
| 暴力破解 | 中 | 中 | ✅ 速率限制、账号锁定、验证码支持 |

### 安全验证

**测试覆盖要求：**
- ✅ 单元测试：加密算法、Token 生成/验证、权限验证
- ✅ 集成测试：完整认证流程、权限控制流程
- ✅ 渗透测试：OWASP Top 10 漏洞扫描
- ✅ 性能测试：认证响应时间 <100ms (P95)、并发能力

**依赖安全管理：**
- ✅ 使用 OWASP Dependency-Check 扫描依赖漏洞
- ✅ 定期更新依赖版本（每月一次）
- ✅ 提供依赖安全报告（每个 Release）

**漏洞管理承诺：**

| 漏洞等级 | 响应时间 | 修复时间 | 补丁发布 |
|---------|---------|---------|---------|
| 🔴 高危 | 24 小时内 | 48 小时内 | 72 小时内 |
| 🟡 中危 | 72 小时内 | 1 周内 | 2 周内 |
| 🟢 低危 | 1 周内 | 2 周内 | 1 月内 |

**漏洞披露流程：**
1. 安全研究者通过私密渠道提交漏洞
2. 团队确认漏洞等级和影响范围
3. 开发修复补丁并进行内部测试
4. 邀请安全研究者验证修复
5. 协同发布安全公告和补丁

---

## API Backend 项目类型特定需求

### 项目类型概述

Spring Security Boot 是一个基于 Spring Boot 的后端 API 框架，专注于认证和授权。它不是一个独立的 API 服务，而是一个开发者工具库，通过 Maven 依赖集成到应用中。

**核心特点：**
- 提供可选的 RESTful API 端点用于认证
- 支持 JWT 和 Session 两种认证模式
- 基于 Spring Security 标准的扩展点
- 零配置启动（@EnableSecurityBoot）
- 网络安全一体化（CORS/CSRF/XSS 自动配置）

---

### 核心架构原则

**框架定位：**
- ✅ 开发工具/框架（不是独立 API 服务）
- ✅ 提供认证和授权能力（不是业务功能）
- ✅ 通过 Maven 依赖集成（不是 HTTP 调用）

**技术栈：**
- Java 1.8+ (支持 Java 8 / 11 / 17)
- Spring Boot 2.2.1+ (支持 2.x 和 3.x)
- Spring Security 5.2.1+ (支持 5.x 和 6.x)
- MySQL 5.7+ (可扩展到其他数据库)

**架构模式：**
- 分层微服务架构
- 模块化设计（core, components, services）
- API-Impl-Controller 分层
- 构造器依赖注入

---

### API 端点规格

**可选的默认认证端点（可启用/禁用/覆盖）：**

| 端点 | 方法 | 描述 | 认证 | 可配置 |
|-----|------|------|------|--------|
| /api/auth/login | POST | 用户登录 | ❌ | ✅ 可禁用 |
| /api/auth/logout | POST | 用户登出 | ✅ | ✅ 可禁用 |
| /api/auth/refresh | POST | 刷新 Token | ❌ | ✅ 可禁用 |

**配置方式：**
```properties
# 启用默认端点（默认：true）
security.endpoints.enabled=true

# 自定义端点路径
security.endpoints.login=/api/auth/login
security.endpoints.logout=/api/auth/logout
security.endpoints.refresh=/api/auth/refresh
```

**应用自己实现的端点（业务功能）：**
- 用户管理（/api/users）
- 角色管理（/api/roles）
- 会话管理（/api/sessions）

---

### 认证模型

**支持认证方式：**

**MVP 阶段：**
- ✅ 用户名密码认证
- ✅ JWT Token 认证
- ✅ Session 认证
- ✅ Remember Me
- ✅ HTTP Basic

**Growth 阶段：**
- 🔲 短信验证码认证
- 🔲 第三方登录（OAuth 2.0 / OIDC）
- 🔲 LDAP / Active Directory

**认证流程：**
```
用户登录 → AuthenticationProvider 验证 → 生成 Token/Session → 返回给客户端
客户端请求 → 携带 Token → Filter 验证 → 授权检查 → 访问资源
```

**权限模型：**
- RBAC (基于角色的访问控制)
- 用户 → 角色 → 权限
- 支持 @PreAuthorize、@Secured 注解

---

### 数据模式

**用户实体：**
```json
{
  "id": 1,
  "username": "admin",
  "password": "$2a$10$...",
  "email": "admin@example.com",
  "enabled": true,
  "createdAt": "2026-02-24T00:00:00Z"
}
```

**登录请求：**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**登录响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "roles": ["ADMIN"]
    }
  }
}
```

---

### 错误代码

**统一错误响应格式：**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "data": null,
  "timestamp": "2026-02-24T10:00:00Z",
  "path": "/api/users"
}
```

**标准 HTTP 状态码：**

| 状态码 | 含义 | 使用场景 |
|-------|------|---------|
| 200 | OK | 请求成功 |
| 201 | Created | 资源创建成功 |
| 400 | Bad Request | 请求参数错误 |
| 401 | Unauthorized | 未认证 |
| 403 | Forbidden | 无权限 |
| 404 | Not Found | 资源不存在 |
| 500 | Internal Server Error | 服务器错误 |

**业务错误代码：**

| 代码 | 含义 | 说明 |
|-----|------|------|
| 1001 | INVALID_CREDENTIALS | 用户名或密码错误 |
| 1002 | USER_DISABLED | 用户已被禁用 |
| 1003 | TOKEN_EXPIRED | Token 已过期 |
| 1004 | TOKEN_INVALID | Token 无效 |
| 1005 | RATE_LIMIT_EXCEEDED | 速率限制超出 |
| 1006 | USER_ALREADY_EXISTS | 用户已存在 |
| 1007 | ROLE_NOT_FOUND | 角色不存在 |

---

### 速率限制

**框架提供：**
- ✅ 登录尝试限制（防止暴力破解）
- ✅ 速率限制接口（可选实现）

**默认限制规则：**

| 端点类型 | 限制 | 时间窗口 |
|---------|------|---------|
| 登录端点 | 5 次 | 1 分钟 |
| 注册端点 | 3 次 | 1 小时 |

**速率限制响应头：**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1677240000
```

**应用负责：**
- ✅ 通用 API 速率限制
- ✅ 业务相关的速率限制

---

### 测试支持

**单元测试支持：**
```java
// Mock 用户注解
@WithMockUser(username = "admin", roles = {"ADMIN"})
void testProtectedEndpoint() {
    // 测试受保护端点
}

// Mock 工具类
Authentication auth = MockAuthentication.builder()
    .username("admin")
    .roles("ADMIN")
    .build();
```

**集成测试支持：**
```java
@SpringBootTest
@SecurityTest
class AuthenticationIntegrationTest {
    // 完整的认证流程测试
}
```

**性能基准：**
- Token 生成 < 10ms
- Token 验证 < 5ms
- 认证响应 < 100ms (P95)

---

### 可观测性

**Metrics 端点：**
- /actuator/metrics/security.authentication.success
- /actuator/metrics/security.authentication.failure
- /actuator/metrics/security.authentication.duration

**审计事件：**
- AuthenticationSuccessEvent
- AuthenticationFailureEvent
- AuthorizationFailureEvent

**分布式追踪：**
- 支持 Micrometer Tracing
- 自动生成 Span

---

### 标准扩展点

**使用 Spring Security 标准接口：**

**1. AuthenticationProvider（添加新的认证方式）：**
```java
@Component
public class SmsAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) {
        // 实现短信验证码认证
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

**2. UserDetailsService（自定义用户加载）：**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 从数据库加载用户
    }
}
```

**3. PasswordEncoder（自定义密码加密）：**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

---

### 配置策略

**配置分级：**

**1. 必填配置（3 个）：**
```properties
# 数据库连接
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=***
```

**2. 可选配置（有合理默认）：**
```properties
# JWT 过期时间（默认：60 分钟）
security.jwt.expiration=3600

# Token 前缀（默认：Bearer ）
security.jwt.token-prefix=Bearer

# 登录端点路径（默认：/api/auth/login）
security.endpoints.login=/api/auth/login
```

**3. 高级配置（很少需要）：**
```properties
# BCrypt 强度（默认：10）
security.password.strength=10

# 启用默认端点（默认：true）
security.endpoints.enabled=true

# 启用 CSRF（默认：true）
security.csrf.enabled=true
```

**配置验证：**
- ✅ 启动时验证必填配置
- ✅ 清晰的错误提示和修复建议
- ✅ 配置摘要日志

**零配置启动：**
```java
@Configuration
@EnableSecurityBoot // 一行注解，自动配置
public class SecurityConfig {
    // 只需要配置数据库
}
```

---

### 文档结构

**MVP 必须有：**
1. ✅ 快速开始指南（5 分钟）
2. ✅ 核心概念说明
3. ✅ 配置参考（必填/可选/高级）
4. ✅ 核心 API 文档
5. ✅ 示例项目（1 个）

**Growth 阶段：**
1. 🔲 完整测试指南
2. 🔲 性能优化指南
3. 🔲 迁移指南（Spring Security、Sa-Token）
4. 🔲 更多示例项目

---

### 实现考虑

**性能要求：**
- 认证响应时间 <100ms (P95)
- 框架启动时间 <3秒
- 支持 1000+ 并发请求

**安全要求：**
- HTTPS 强制（生产环境）
- 密码加密 (BCrypt 强度 10)
- JWT 签名验证
- 敏感数据脱敏

**兼容性要求：**
- Spring Boot 2.x / 3.x
- Java 8 / 11 / 17
- MySQL / PostgreSQL / Oracle

**可扩展性要求：**
- 基于 Spring Security 标准扩展点
- 多数据库支持
- 微服务架构支持

---

## 项目范围与渐进式开发

### MVP 策略与理念

**MVP 方法：** Experience MVP（体验 MVP）

**核心原则：**
- 展示核心价值：让 Spring Security 变得简单
- 提供完整体验：从集成到部署的完整流程
- 聚焦核心场景：最常用的认证方式

**MVP 目标：**
- 让开发者在 30 分钟内完成集成
- 让架构师认可代码质量和架构设计
- 让安全审计员认可等保 2.0 合规能力

**资源需求：**
- 2 个开发人员 × 8 周 = 2 个月
- 1 个技术文档专家（兼职）

---

### MVP 功能集（Phase 1 - 8 周）

**核心用户旅程：**
- ✅ 应用开发者（小李）：30 分钟集成体验
- ✅ 架构师（王工）：认可架构设计

**必须具备的能力：**

| 功能 | 描述 | 优先级 |
|-----|------|--------|
| 1. 用户名密码认证 | 最基础的认证方式 | P0 |
| 2. JWT Token 认证 | 前后端分离标准 | P0 |
| 3. 权限注解 | @PreAuthorize 授权控制 | P0 |
| 4. CORS 自动配置 | 前端集成必需 | P0 |
| 5. CSRF 自动配置 | 安全标准 | P0 |
| 6. 配置验证 | 减少集成问题 | P0 |
| 7. 快速开始文档 | 5 分钟上手指南 | P0 |

**Sprint 计划：**

**Sprint 0 (1 周)：项目启动**
- 搭建开发环境
- 确定技术架构
- 设置 CI/CD
- 创建第一个示例

**Sprint 1 (2 周)：核心认证**
- 用户名密码认证
- JWT Token 认证
- 基础配置类

**Sprint 2 (2 周）：权限与网络安全**
- 权限注解（@PreAuthorize）
- CORS 自动配置
- CSRF 自动配置

**Sprint 3 (2 周）：开发者体验**
- 配置验证
- 错误提示和修复建议
- 快速开始文档

**Sprint 4 (1 周）：发布准备**
- 单元测试
- 集成测试
- 安全扫描
- Release 1.0.0

---

### MVP 后功能（Phase 2 - 3 个月）

**目标：** 达到 Sa-Token 90% 功能覆盖

**新增功能：**
- Session 认证
- Remember Me
- XSS 防护
- 安全响应头自动配置
- 完整测试覆盖
- 示例项目
- 迁移指南（Spring Security、Sa-Token）

**用户旅程：**
- 运维工程师（老陈）：生产运维
- 安全审计员（小刘）：等保 2.0 合规

---

### 扩展功能（Phase 3 - 6 个月）

**目标：** 完整对标 Sa-Token + 企业级增强

**新增功能：**
- 短信验证码认证插件
- 第三方登录（微信、GitHub、Google）
- OAuth 2.0 授权服务器
- SSO 单点登录（同域、跨域）
- LDAP / AD 集成
- Redis 集成（会话持久化）
- 多端登录策略
- 踢人下线 API
- 多数据库支持（PostgreSQL、Oracle）
- 微服务网关鉴权
- Feign Token 传播
- 社区贡献插件系统
- 企业级培训和服务

**用户旅程：**
- 框架贡献者（小张）：社区参与

---

### 砍功能预案

**如果 Sprint 1 或 Sprint 2 延期：**
- ❌ 砍掉 CSRF 自动配置
- ✅ 保留 CORS（前端集成必需）
- ✅ 保留配置验证（减少支持成本）

**如果 Sprint 3 延期：**
- ❌ 砍掉示例项目（只保留快速开始文档）
- ✅ 保留配置验证和错误提示

**判断标准：**
- CORS 是前端集成必需，不能砍
- 配置验证能减少支持成本，不砍
- CSRF 可以延后到 Growth
- 示例项目可以延后到 Growth

---

### 风险缓解策略

**技术风险：**

| 风险 | 缓解措施 |
|-----|---------|
| Spring Security 版本兼容 | ✅ 同时支持 5.x 和 6.x |
| 配置验证复杂度 | ✅ 提供默认配置 + 验证工具 |
| 网络安全配置错误 | ✅ 自动配置 + 清晰文档 |
| 功能延期 | ✅ 严格按优先级开发，准备砍功能预案 |

**市场风险：**

| 风险 | 缓解措施 |
|-----|---------|
| Sa-Token 功能丰富 | ✅ 聚焦 Spring 生态 + 网络安全差异化 |
| 开发者认知成本 | ✅ 快速开始文档 + 配置验证 |
| 社区接受度 | ✅ 开源贡献友好 + 活跃 Issue 响应 |

**资源风险：**

| 风险 | 缓解措施 |
|-----|---------|
| 团队规模不足 | ✅ MVP 可由 2 人完成 |
| 开发周期延长 | ✅ 功能优先级清晰，可砍功能不延期 |
| 文档质量 | ✅ 技术文档专家参与 |
| 测试不足 | ✅ 每个功能必须有测试才能合并
---

## 功能需求

### 能力契约声明

**这是产品的能力契约：**

- UX 设计师将**只设计**这里列出的能力
- 架构师将**只支持**这里列出的能力
- Epic 分解将**只实现**这里列出的能力
- 如果一个能力不在这里，它将**不会**存在于最终产品中

**总计：** 13 个能力区域，61 个功能需求

---

### 能力区域 1：认证

- FR1: 开发者可以使用用户名密码进行用户认证
- FR2: 开发者可以使用 JWT Token 进行无状态认证
- FR3: 开发者可以使用 Session 进行有状态认证
- FR4: 系统支持 Remember Me 功能以延长用户会话

---

### 能力区域 2：授权

- FR6: 开发者可以使用注解定义方法级权限要求
- FR7: 开发者可以使用注解定义角色级访问控制
- FR8: 系统支持基于角色的访问控制（RBAC）
- FR9: 系统管理员可以创建和管理角色
- FR10: 系统管理员可以分配权限给角色

---

### 能力区域 3：网络安全

- FR11: 框架自动配置 CORS（跨域资源共享）策略
- FR12: 框架自动启用 CSRF（跨站请求伪造）防护
- FR13: 框架自动配置 XSS（跨站脚本）防护
- FR14: 框架自动配置安全响应头
- FR15: 框架记录认证成功/失败审计事件
- FR16: 框架记录授权失败审计事件

---

### 能力区域 4：配置管理

- FR17: 开发者可以使用注解启用安全配置
- FR18: 框架在启动时验证必填配置
- FR19: 框架提供清晰的配置错误提示和修复建议
- FR20: 框架提供配置默认值以减少必需配置项

---

### 能力区域 5：开发者体验

- FR21: 框架提供配置验证工具
- FR22: 框架提供快速开始文档
- FR23: 框架提供核心概念说明文档
- FR24: 框架提供配置参考文档
- FR25: 框架提供 API 参考文档

---

### 能力区域 6：测试支持

- FR26: 开发者可以在测试中使用 Mock 用户
- FR27: 框架提供认证测试工具类
- FR28: 框架提供安全测试切片
- FR29: 框架提供性能基准测试

---

### 能力区域 7：可观测性

- FR30: 框架暴露认证成功/失败 Metrics
- FR31: 框架暴露认证耗时 Metrics
- FR32: 框架支持分布式追踪（Micrometer Tracing）
- FR33: 框架提供结构化日志
- FR34: 框架提供健康检查端点

---

### 能力区域 8：扩展性

- FR35: 开发者可以实现 AuthenticationProvider 添加新的认证方式
- FR36: 开发者可以实现 UserDetailsService 自定义用户加载
- FR37: 开发者可以实现 PasswordEncoder 自定义密码加密

---

### 能力区域 9：密码安全

- FR38: 框架使用 BCrypt 加密用户密码
- FR39: 框架禁止使用弱加密算法（MD5、SHA1）
- FR40: 框架验证 Token 签名
- FR41: 框架自动生成安全密钥（或警告用户配置）

---

### 能力区域 10：会话管理

- FR42: 用户可以主动登出
- FR43: 系统支持 Token 刷新机制
- FR44: 系统支持查询当前用户会话
- FR45: 系统管理员可以踢出指定用户

---

### 能力区域 11：用户管理

- FR46: 开发者可以使用框架提供的用户创建 API
- FR47: 开发者可以使用框架提供的用户查询 API
- FR48: 开发者可以使用框架提供的密码修改 API
- FR49: 开发者可以使用框架提供的密码重置 API

---

### 能力区域 12：认证端点

- FR50: 框架提供可选的登录端点
- FR51: 框架提供可选的登出端点
- FR52: 框架提供可选的 Token 刷新端点

---

### 能力区域 13：示例与指南

- FR53: 框架提供一键运行示例项目
- FR54: 框架提供故障排查指南
- FR55: 框架提供常见问题解答文档
- FR56: 文档包含完整的代码示例
- FR57: 框架提供插件开发指南
- FR58: 框架提供贡献者指南
- FR59: 开发者可以配置日志级别
- FR60: 开发者可以配置日志格式
- FR61: 框架提供等保 2.0 合规检查清单
- FR62: 框架提供安全配置最佳实践文档
---

## 非功能需求

### 性能

**NFR-PERF-001: 认证响应时间**
- P50 响应时间必须 < 100ms
- P95 响应时间必须 < 200ms
- P99 响应时间必须 < 500ms

**NFR-PERF-002: 框架启动时间**
- 框架启动时间必须 < 5 秒
- 自动配置完成时间必须 < 3 秒

**NFR-PERF-003: 吞吐量**
- 框架必须支持至少 500 QPS
- 在 500 QPS 下，P95 响应时间 < 200ms
- 框架必须支持至少 100 并发连接

**NFR-PERF-004: 性能基准测试**
- 框架必须提供性能基准测试（JMH）
- 基准测试结果包含在文档中
- 性能退化超过 10% 必须在 Release Notes 中说明

---

### 安全

**NFR-SEC-001: 密码存储**
- 所有用户密码必须使用 BCrypt 加密（强度 ≥ 10）
- 禁止使用弱加密算法（MD5、SHA1）
- 密码不得以明文形式记录在日志中

**NFR-SEC-002: Token 安全**
- JWT Token 必须使用 HS256 或更强算法签名
- Token 必须有过期时间（默认 ≤ 60 分钟）
- Token 必须支持刷新机制

**NFR-SEC-003: 数据传输**
- 生产环境必须强制使用 HTTPS
- 敏感数据（密码、Token）不得在 URL 中传输

**NFR-SEC-004: 网络安全**
- 框架必须配置以下安全响应头：
  - X-Frame-Options: DENY
  - X-Content-Type-Options: nosniff
  - Strict-Transport-Security: max-age=31536000
  - X-XSS-Protection: 1; mode=block
- 可以通过 OWASP ZAP 扫描验证

**NFR-SEC-005: 依赖安全**
- 所有依赖必须通过安全扫描（OWASP Dependency-Check）
- 高危漏洞必须在 24 小时内响应，48 小时内修复

**NFR-SEC-006: 合规性**
- 框架必须支持等保 2.0 二级认证要求
- 框架必须提供等保 2.0 合规检查清单

---

### 可扩展性

**NFR-SCALE-001: 用户增长**
- 框架必须支持 10 倍用户增长，性能退化 < 10%
- JWT 认证模式支持无状态水平扩展
- Session 认证模式需要 Sticky Session 或 Session 共享

**NFR-SCALE-002: 数据库扩展**
- 框架必须支持 MySQL（默认）
- 框架架构必须支持其他数据库（PostgreSQL、Oracle）

**NFR-SCALE-003: 多版本支持**
- 框架提供两个版本：
  - 1.x 版本支持 Spring Boot 2.x + Java 8-11
  - 2.x 版本支持 Spring Boot 3.x + Java 17+
- 两个版本的 API 保持一致
- 提供清晰的迁移指南
- 每个大版本有 6 个月的迁移窗口

---

### 集成

**NFR-INT-001: Spring Security 原生**
- 框架必须基于 Spring Security 标准 API
- 框架不得隐藏或替代 Spring Security 核心接口

**NFR-INT-002: Spring 生态兼容**
- 框架必须与 Spring Boot Actuator 兼容
- 框架必须支持 Spring Cloud Alibaba（Nacos、Feign）

**NFR-INT-003: 数据库抽象**
- 框架必须使用 Spring Data JPA 进行数据访问
- 框架必须支持 Repository 模式

---

### 可靠性

**NFR-REL-001: 向后兼容**
- 每个大版本有 6 个月的迁移窗口
- 提供自动迁移工具（如果可能）
- Breaking Changes 提前 6 个月通知
- 不承诺同时支持多个大版本

**NFR-REL-002: 错误处理**
- 框架必须提供清晰的错误提示
- 框架必须提供修复建议
- 错误提示必须包含上下文信息

**NFR-REL-003: 监控**
- 框架必须暴露以下 Metrics：
  - security.authentication.success（计数器）
  - security.authentication.failure（计数器）
  - security.authentication.duration（分布）
- Metrics 集成到 Micrometer
- 可通过 /actuator/metrics 端点访问

---

### 可维护性

**NFR-MAINT-001: 代码质量**
- 框架必须使用构造器依赖注入
- 核心安全代码测试覆盖率 ≥ 90%
- 认证和授权关键路径覆盖率 = 100%
- 整体测试覆盖率 ≥ 70%

**NFR-MAINT-002: 文档质量**
- 框架必须提供快速开始文档
- 框架必须提供 API 参考文档
- 框架必须提供配置参考文档
- 文档必须包含完整的代码示例

**NFR-MAINT-003: 开发者体验**
- 框架在启动时验证必填配置
- 如果必填配置缺失，启动失败并显示错误提示
- 错误提示必须包含修复建议
- 可以通过提供空配置测试验证