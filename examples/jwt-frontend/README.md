# Vue3 + Vite JWT 前端示例

这是一个使用 Vue 3 + Vite 构建的 JWT 认证前端示例，用于演示与 Spring Security Boot 后端的集成。

## 前置条件

- Node.js 16+
- npm 8+

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

### 3. 访问应用

打开浏览器访问 [http://localhost:3000](http://localhost:3000)

## 功能说明

### 1. 登录功能
- 用户名密码登录
- 存储 JWT Token 到 localStorage
- 登录状态持久化

### 2. 认证页面
- 需要认证才能访问的用户信息页面
- 显示当前登录用户信息

### 3. 管理页面
- 需要管理员权限才能访问
- 展示权限控制效果

### 4. Token 管理
- 自动添加 Authorization header
- Token 过期处理
- Token 刷新机制

## API 端点

| 功能 | 方法 | 端点 | 说明 |
|-----|------|-----|-----|
| 登录 | POST | /api/auth/login | 获取 JWT Token |
| 登出 | POST | /api/auth/logout | 清除 Token |
| Token 刷新 | POST | /api/auth/refresh | 刷新 Token |
| 用户信息 | GET | /api/users/me | 获取当前用户信息 |
| 所有用户 | GET | /api/admin/users | 获取所有用户（需要 ADMIN 权限） |

## 测试账户

| 用户名 | 密码 | 角色 |
|-------|------|-----|
| admin | password123 | ADMIN, USER |
| user | password123 | USER |
| test | password123 | USER |

## 项目结构

```
jwt-frontend/
├── src/
│   ├── main.js           # 应用入口（包含路由配置）
│   ├── App.vue           # 根组件
│   ├── views/            # 页面组件
│   │   ├── Login.vue      # 登录页面
│   │   ├── Home.vue      # 首页（用户信息）
│   │   └── Admin.vue     # 管理页面
│   ├── services/         # API 服务
│   │   └── auth.js       # 认证相关 API
│   └── stores/           # 状态管理
│       └── auth.js       # 认证状态
├── package.json
├── vite.config.js
└── index.html
```

## 开发说明

### 1. 代理配置
Vite 已配置代理，自动将 `/api` 请求转发到后端 `http://localhost:8080`。

### 2. 跨域支持
后端已配置 CORS，前端无需额外配置。

### 3. 状态管理
使用 Pinia 进行状态管理，存储用户信息和登录状态。

## 更多文档

- [快速开始文档](../../docs/quick-start.md)
- [配置参考](../../docs/configuration.md)
- [API 参考](../../docs/api.md)