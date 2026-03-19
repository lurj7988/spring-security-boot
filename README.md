# Spring Security Boot

Spring Security Boot 是一个基于 Spring Security 的增强框架，目标是用更短的配置路径提供认证、网络安全和开发者体验能力。

## 快速入口

- [30 分钟快速开始](docs/quick-start.md)
- [配置参考文档](docs/configuration.md)
- [API 参考文档](docs/api.md)
- [测试支持工具文档](docs/testing-support.md)

## 示例项目

- [快速开始示例项目](examples/README.md) - 包含完整的前后端示例，展示如何使用框架

## 当前仓库结构

这是一个 Maven 多模块框架仓库，而不是单一可直接运行的 demo 应用仓库。核心模块包括：

- `security-dependencies`: 依赖版本管理
- `security-core`: 核心自动配置与安全能力
- `security-common`: 通用工具
- `security-components`: 默认组件实现

## 适合谁

- 希望快速集成 Spring Security 能力的后端开发者
- 需要统一认证与基础网络安全配置的团队
- 想先跑通最小路径，再逐步扩展 JWT、Session、测试支持等能力的项目

## 下一步

从 [快速开始文档](docs/quick-start.md) 开始，完成最小集成与启动验证。
