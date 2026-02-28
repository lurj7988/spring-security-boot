---
project_name: 'spring-security-boot'
user_name: 'Naulu'
date: '2026-02-24'
sections_completed:
  ['technology_stack', 'language_specific_rules', 'framework_rules',
   'testing_rules', 'code_quality_rules', 'dev_workflow_rules', 'anti_patterns']
status: 'complete'
rule_count: 75
optimized_for_llm: true
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## 技术栈与版本

**核心框架：**

- Java 1.8
- Spring Boot 2.7.18
- Spring Security 5.7.11
- Spring Cloud Alibaba 2021.0.5.0

**数据库：**

- MySQL (用于 OAuth2 服务) 默认地址：192.168.220.236:3306 用户名：root 密码：Gepoint

**关键库版本：**

- Jackson (Spring Boot 默认 JSON 库)
- commons-io 2.2
- bcprov-jdk15on 1.69 (加密)
- Spring Social 1.1.6.RELEASE
- Spring Session 1.3.3.RELEASE

**构建工具：**

- Maven 3.x
- maven-compiler-plugin 3.8.0

---

## 关键实现规则

### 语言特定规则

**依赖注入：**

- 必须使用**构造器依赖注入**，禁止使用字段注入（@Autowired on fields）
- 所有依赖通过构造器参数声明并赋值给 final 字段

**组合注解：**

- 使用 @Import 将多个配置类组合到自定义注解中
- 框架提供三个核心组合注解：@EnableFrameAuthorizationServer、@EnableFrameResourceServer、@EnableAlibabaCloud

**JSON 序列化：**

- 统一使用 Jackson（Spring Boot 默认）
- Response.toString() 使用 ObjectMapper.writeValueAsString()
- Security 处理器使用 ObjectMapper.readValue() 进行 JSON 解析

**接口实现位置：**

- -api 模块：定义 Feign 客户端接口（@FeignClient）
- -impl 模块：实现接口并添加 @RestController 和 @RequestMapping
- 实现类直接实现接口，无需单独的 controller 类

---

### 框架特定规则

**Spring Security 自定义处理器：**

- 认证成功/失败处理器位于 security-core/src/main/java/com/original/frame/security/handler/
- 处理器必须实现对应的 Spring Security 接口（如 AuthenticationSuccessHandler）
- 处理器通过构造器接收 ObjectMapper 依赖进行 JSON 响应写入
- 响应格式统一使用 Response.successBuilder(data).build() 模式

**多认证提供者模式：**

- 框架支持多个 AuthenticationProvider 实现
- 认证类型：JWT、用户名密码、短信验证码、移动设备
- 自定义认证提供者需要继承 AuthenticationProvider 并实现 authenticate() 方法

**Feign OAuth2 集成：**

- 使用 @EnableAlibabaCloud 注解启用服务发现和 Feign 客户端
- FeignClientRequestInterceptor 自动添加 OAuth2 Bearer token 到服务间请求

**模块架构模式：**

- 父 POM 管理依赖版本
- 子模块遵循 security-xxx 命名规范
- 组件模块采用 API-Impl-Controller 三层架构

**响应对象模式：**

- 所有 API 响应使用 Response<T> 构建器模式
- 成功响应：Response.successBuilder(data).build()
- 错误响应：Response.errorBuilder(data).build()

**密码编码：**

- 使用 BCryptPasswordEncoder 进行密码加密

---

### 测试规则

**⚠️ 当前状态：零测试覆盖** - 这是一个安全框架项目，零测试覆盖是高风险状态

**测试组织：**

- 测试类应放在对应模块的 src/test/java 目录下
- 测试类命名：{ClassName}Test.java

**测试框架：**

- 单元测试：JUnit 5 (Jupiter) - 本项目实际使用
- Mock 框架：Mockito
- Spring 测试：@SpringBootTest, @WebMvcTest, @MockBean

**测试优先级 (P0-P2)：**

**P0 - 立即：** BCrypt 密码编码测试、JWT 测试、认证失败测试、Response 测试
**P1 - 重要：** Security 处理器测试、认证提供者测试、集成测试
**P2 - 常规：** Feign 测试、端到端测试、性能测试

**覆盖率目标：** 核心安全组件 80%+，业务逻辑 70%+，整体 60%+

---

### 代码质量与风格规则

**命名规范：** 类名 PascalCase，方法名 camelCase，常量 UPPER_SNAKE_CASE

**强制规则：**

- 使用构造器依赖注入
- 不在生产代码中使用 printStackTrace()
- 不使用魔法值，定义为常量
- 公共 API 必须有 JavaDoc

**Import 语句规范：**

- **禁止在类声明处使用全包名实现接口**，应使用 import 导入
- 当需要使用两个同名类/接口时：
  1. 在 import 中导入主要/常用的那个
  2. 仅对次要的同名类在必要时使用全包名（如类声明的 implements 子句）
  3. 在 JavaDoc 中说明为何需要同时使用两个同名接口
- **示例（正确）：**
  ```java
  import org.springframework.security.authentication.AuthenticationProvider;

  public class DaoAuthenticationProvider
          implements AuthenticationProvider,
                     com.original.security.core.authentication.AuthenticationProvider {
  ```
- **示例（错误）：**
  ```java
  public class DaoAuthenticationProvider implements
          org.springframework.security.authentication.AuthenticationProvider,
          com.original.security.core.authentication.AuthenticationProvider {
  ```

**代码审查检查清单：**

- [ ] 使用构造器注入
- [ ] 方法长度 < 50 行
- [ ] 异常被正确处理（使用日志框架）
- [ ] 没有魔法值
- [ ] 公共 API 有 JavaDoc

---

### 开发工作流规则

**Maven 构建规范：**

```bash
mvn clean install                                # 构建整个项目
mvn clean install -pl {module}                   # 构建特定模块
mvn test                                        # 运行测试
```

**服务启动顺序：**

1. AuthorizationApplication (端口 3001)
2. ConfigApplication (端口 3002)
3. UserApplication (端口 3003)

**Git 分支策略：** main (生产), feature/*(功能), hotfix/* (紧急修复)

**提交信息规范（Conventional Commits）：**

```
<type>(<scope>): <subject>
类型：feat/fix/refactor/test/docs/chore
示例：feat(auth): add JWT refresh token support
```

**"完成的定义"：**

- 代码实现完成
- 代码审查通过
- 所有测试通过
- 公共 API 有 JavaDoc

---

### 关键不要错过规则

**安全相关：** 密码必须使用 BCryptPasswordEncoder 加密、敏感信息不能记录到日志

**常见错误：**

- ❌ 使用字段注入而非构造器注入
- ❌ 在生产代码中使用 printStackTrace()
- ❌ 硬编码配置值
- ❌ 公共 API 没有 JavaDoc
- ❌ 测试覆盖率不足

---

## 使用指南

**对于 AI 代理：**

- 在实现任何代码之前阅读此文件
- 严格按照文档记录的规则遵循
- 当有疑问时，选择更严格的选项
- 如果出现新模式，更新此文件

**对于人类：**

- 保持此文件精简，专注于代理需求
- 技术栈变更时更新
- 每季度审查以优化和删除过时规则
- 删除随时间推移变得显而易见的规则

---

**最后更新：** 2026-02-28
