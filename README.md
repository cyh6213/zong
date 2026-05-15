# Zong

面向企业/开发者团队的技术知识生产与自动化交付平台。

## 模块

| 模块 | 说明 |
|------|------|
| frontend | 统一前端 SPA（React + TypeScript + Vite） |
| services/community | 开发者社区服务 |
| services/knowledge | 知识库服务（完整 RAG + 权限过滤） |
| services/agent | 多 Agent 服务（调度 + 编排 + 轻量 RAG） |
| gateway | API 网关 + 场景编排层 |
| shared | 跨服务共享代码 |
| deploy | 部署配置 |

## 快速开始

```bash
# 前端
cd frontend && npm install && npm run dev

# 后端（各服务独立启动）
cd services/community && mvn spring-boot:run
cd services/knowledge && mvn spring-boot:run
cd services/agent && mvn spring-boot:run
cd gateway && mvn spring-boot:run

# 或使用 docker-compose 一键启动
cd deploy && docker-compose up
```
