# Spring Security Boot 快速开始

本文档提供一条基于当前仓库真实实现的最短集成路径，目标是在 30 分钟内完成接入准备并跑通第一个最小示例。

## 1. 适用对象与目标

如果你想：

- 在标准 Spring Boot 2.7.x 应用中启用基础安全能力
- 使用真实存在的 `@EnableSecurityBoot` 自动装配入口
- 快速验证安全过滤链和启动配置校验已经生效

那么可以先走这条最小路径。

**本文档不覆盖：**

- 完整配置参考
- API 参考
- 故障排查大全
- 独立示例工程

这些内容将在后续文档故事中补齐。

## 2. 前置条件

开始之前，请确认你具备以下环境：

- JDK 8
- Maven 3.x
- MySQL 5.7+ 或兼容版本
- 一个标准的 Spring Boot 2.7.x Web 应用

> 注意：当前框架实现基于 Spring Boot 2.7.18 / Spring Security 5.7.11，本文示例不适用于 Spring Boot 3.x / Jakarta 命名空间项目。

## 3. 添加依赖

在你的 Spring Boot 应用中添加框架依赖和 MySQL 驱动：

```xml
<dependencies>
    <dependency>
        <groupId>com.original.frame</groupId>
        <artifactId>security-core</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

说明：

- 上面的片段包含本框架所需的依赖。假设你的项目已经是 Spring Boot Web 应用，已经具备了 `spring-boot-starter-web` 等基础依赖。
- 如果你通过父 POM 或内部 BOM 管理版本，可以统一管理版本号；上面的写法用于展示最小可读示例。

## 4. 添加配置

当前实现中，`SecurityConfigurationValidator` 会在启动时检查关键配置。

至少需要提供：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `security.network.cors.allowed-origins`

示例 `application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security_demo?useSSL=false&serverTimezone=UTC
# 注意：3306 是 MySQL 默认端口，生产环境请使用强密码
spring.datasource.username=root
spring.datasource.password=your_secure_password_here

security.network.cors.allowed-origins=http://localhost:8080
security.network.csrf.enabled=false
```

说明：

- `security.network.cors.allowed-origins` 必填，因为当前实现中 CORS 默认启用，未配置会触发启动失败。
- 这里将 `security.network.csrf.enabled=false` 仅用于快速启动和本地调试示例；生产环境应根据你的请求模式重新评估 CSRF 策略。

## 5. 启用 `@EnableSecurityBoot`

在你的 Spring Boot 启动类上添加 `@EnableSecurityBoot`：

```java
package com.example.demo;

import com.original.security.annotation.EnableSecurityBoot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSecurityBoot
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

该注解会导入当前仓库中已经实现的基础自动配置，包括：

- `PasswordEncoder`（默认 BCrypt）
- `AuthenticationManager`
- 最小可用的 `SecurityFilterChain`
- 启动时配置校验

## 6. 创建第一个示例接口

为了验证过滤链已经生效，可以添加一个最简单的接口：

```java
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
```

## 7. 启动并验证

启动应用：

```bash
mvn spring-boot:run
```

如果配置正确，你应当看到应用正常启动，而不是出现配置校验异常。

然后访问：

```bash
curl -i http://localhost:8080/hello
```

预期结果：

- 应用成功启动
- `/hello` 不是匿名开放接口
- 在未额外配置认证流程时，返回未认证响应（通常是 `401 Unauthorized`）

这说明：

- 自动配置已经生效
- 安全过滤链已经挂载
- 你的应用已经进入“可继续扩展”的状态

## 8. 常见启动失败原因

### 1. 缺少数据源配置

如果你看到类似”数据库连接未配置”的错误，补齐以下配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security_demo
# 注意：3306 是 MySQL 默认端口
spring.datasource.username=root
spring.datasource.password=your_secure_password_here
```

### 2. 缺少 CORS 允许来源

如果你看到类似 “CORS 已启用，但未配置 allowed-origins” 的错误，补齐：

```properties
security.network.cors.allowed-origins=http://localhost:8080
```

### 3. 误用 Spring Boot 3 / Jakarta 代码示例

当前版本基线仍是 Spring Boot 2.7.x，请继续使用 `javax` 时代兼容的依赖和 API 语境，不要直接迁移到 Boot 3 风格配置。

## 9. 下一步阅读

跑通最小路径后，建议继续看：

- [配置参考文档](configuration.md)：了解所有配置项的详细说明和选项
- 后续 API 参考文档：了解默认端点与返回格式
- [测试支持工具文档](testing-support.md)：为你的安全逻辑添加测试

如果你当前目标只是”先确认框架可以启动并挂载安全能力”，到这里已经足够。
