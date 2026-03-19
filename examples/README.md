# Spring Security Boot 示例项目

本目录包含 Spring Security Boot 框架的完整示例项目，帮助开发者快速理解和上手框架的使用。

## 示例列表

### 1. 快速开始示例（后端）

**位置**: `quick-start/`

这是一个完整的后端示例，展示如何快速集成 Spring Security Boot 框架。

**功能特性**:
- ✅ 一键启动 (`mvn spring-boot:run`)
- ✅ 包含登录 API (`/api/auth/login`)
- ✅ 包含受保护的 API 端点 (`/api/users/me`)
- ✅ 包含权限控制示例 (`/api/admin/users`)
- ✅ 使用 `@EnableSecurityBoot` 注解
- ✅ 使用 `@PreAuthorize` 进行角色控制

**启动方式**:
```bash
cd quick-start
mvn clean install
mvn spring-boot:run
```

访问地址: http://localhost:8080

**测试账户**:
- admin / password123 (管理员)
- user / password123 (普通用户)
- test / password123 (普通用户)

### 2. JWT 前端示例

**位置**: `jwt-frontend/`

这是一个 Vue3 + Vite 构建的前端示例，展示完整的 JWT 认证流程。

**功能特性**:
- ✅ 登录/登出功能
- ✅ JWT Token 存储和管理
- ✅ 路由守卫
- ✅ 权限控制页面
- ✅ Token 自动刷新
- ✅ 跨域请求配置

**启动方式**:
```bash
cd jwt-frontend
npm install
npm run dev
```

访问地址: http://localhost:3000

**说明**: 前端默认连接到 `localhost:8080` 的后端服务。

## 快速上手

### 步骤 1: 运行后端示例

```bash
# 进入后端示例目录
cd quick-start

# 安装依赖
mvn clean install

# 启动服务
mvn spring-boot:run
```

访问 http://localhost:8080/api/hello 测试服务是否正常。

### 步骤 2: 运行前端示例

```bash
# 进入前端示例目录
cd jwt-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:3000 查看前端示例。

### 步骤 3: 测试完整流程

1. 打开 http://localhost:3000
2. 使用测试账户登录（如 admin/password123）
3. 查看用户信息页面
4. 如果是管理员，访问管理员页面
5. 登出并重新登录

## 更多文档

- [快速开始文档](../docs/quick-start.md)
- [配置参考](../docs/configuration.md)
- [API 参考](../docs/api.md)
- [故障排查指南](../docs/troubleshooting.md)
- [示例项目说明](./quick-start/README.md)

## 常见问题

### Q: 后端启动失败？
A: 请检查端口 `8080` 是否被占用。默认使用 H2 内存数据库，无需额外安装数据库服务。

### Q: 前端无法访问后端？
A: 确保 Vite 的代理配置正确，后端服务正在运行。

### Q: Token 过期怎么办？
A: 示例包含自动刷新机制，会尝试在 401 时刷新 Token。

## 依赖版本

- 后端: Java 1.8, Spring Boot 2.7.18
- 前端: Node.js 16+, Vue 3.4+, Vite 5.0+

## 项目结构

```
examples/
├── README.md                  # 本文件，示例导航
├── quick-start/               # 后端示例
│   ├── pom.xml               # Maven 配置
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/         # Java 源码
│   │   │   └── resources/    # 配置文件
│   │   └── resources/        # 包含 application.properties 和 data.sql
│   └── README.md             # 后端示例说明
└── jwt-frontend/             # 前端示例
    ├── package.json          # npm 配置
    ├── vite.config.js        # Vite 配置
    ├── index.html            # 入口 HTML
    ├── src/
    │   ├── main.js           # 应用入口
    │   ├── App.vue           # 根组件
    │   ├── views/            # 页面组件
    │   ├── router/           # 路由配置
    │   ├── services/         # API 服务
    │   └── stores/           # 状态管理
    └── README.md             # 前端示例说明
```