# Zong — 项目目录结构设计

## Objective

Zong 是面向企业/开发者团队的技术知识生产与自动化交付平台，融合三个已有项目：

1. **开发者社区** — 技术文章平台
2. **知识库** — 企业级知识管理，含完整 RAG（权限过滤、优化检索）
3. **多 Agent** — Agent 调度 + 工作流编排 + 轻量 RAG（无权限过滤，用于 Agent 执行时快速检索）

统一平台让用户无缝使用所有功能：工程师撰写方案（社区），平台自动存入知识库，产品经理检索知识，团队通过工作流编排多个 Agent 自动化交付。

## Tech Stack

| 层 | 技术 |
|---|---|
| 前端 | React + TypeScript + Vite + Ant Design + Zustand |
| 后端 | Java + Spring Boot（各服务独立版本） |
| 网关 | Spring Cloud Gateway |
| RAG | Spring AI Alibaba（knowledge 服务） |
| 部署 | Docker + docker-compose |

## 项目定位

核心定位：面向企业或开发者团队的技术知识生产与自动化交付平台。

场景故事：工程师在平台上撰写技术方案（社区模块），平台自动将文档解析为知识块存入企业知识库。产品经理需要了解"微服务拆分原则"时，RAG 系统检索相关知识并生成摘要。团队要落地新项目时，可视化工作流编排多个 Agent：一个读取历史架构文档，一个生成代码框架，一个编写部署手册，最终自动输出完整的项目启动包。

## 顶层目录结构

```
zong/
├── docs/                          # 过程资产
│   ├── 需求分析/
│   ├── 变更记录/
│   ├── 任务清单/
│   └── 经验总结/
│       ├── 可复用流程/
│       ├── 架构决策/
│       └── 踩坑记录/
├── frontend/                      # 统一前端 SPA
├── services/                      # 后端微服务集群
│   ├── community/                 #   开发者社区
│   ├── knowledge/                 #   知识库（完整 RAG + 权限）
│   └── agent/                     #   多 Agent（调度 + 编排 + 轻量 RAG）
├── gateway/                       # API 网关 + 场景编排层
├── shared/                        # 跨服务共享代码
├── deploy/                        # 部署配置
├── agent-skills/                  # 参考（.gitignore）
├── .gitignore
└── README.md
```

## docs/ 文档目录结构

```
docs/
├── 需求分析/                      # specs
│   └── YYYY-MM-DD-主题-design.md #   按日期+主题命名
├── 变更记录/                      # changes
│   └── CHANGE-编号-描述.md        #   含动机、影响范围、审批状态
├── 任务清单/                      # tasks
│   ├── sprint-XX.md               #   按迭代组织
│   └── backlog.md                 #   待办池
└── 经验总结/                      # lessons
    ├── 可复用流程/                 #   workflows（AI 可直接执行的成功流程模板）
    ├── 架构决策/                   #   decisions（ADR-编号-主题.md）
    └── 踩坑记录/                   #   pitfalls
```

### 经验总结/可复用流程 流程模板格式

```markdown
# 流程：[名称]

## 触发条件
[何时使用此流程]

## 前置检查
- [ ] 检查项1
- [ ] 检查项2

## 执行步骤
1. 步骤1
2. 步骤2
3. ...

## 验证
- [ ] 验证项1
- [ ] 验证项2

## 踩过的坑
- ...
```

## frontend/ 前端目录结构

```
frontend/
├── public/
├── src/
│   ├── assets/                    # 图片、图标、样式
│   ├── components/                # 通用组件
│   │   └── Layout/                #   全局布局（侧边栏+顶栏）
│   ├── pages/                     # 页面（按业务模块）
│   │   ├── Community/             #   开发者社区
│   │   ├── Knowledge/             #   知识库
│   │   ├── Agent/                 #   Agent 编排与执行
│   │   └── Dashboard/             #   首页仪表盘
│   ├── services/                  # API 调用层（按后端服务分）
│   │   ├── communityApi.ts
│   │   ├── knowledgeApi.ts
│   │   └── agentApi.ts
│   ├── stores/                    # 状态管理（Zustand）
│   ├── hooks/                     # 自定义 Hooks
│   ├── types/                     # TypeScript 类型定义
│   ├── router/                    # 路由配置
│   ├── App.tsx
│   └── main.tsx
├── package.json
├── vite.config.ts
├── tsconfig.json
└── .env
```

## services/ 后端服务目录结构

三个服务统一 Spring Boot 标准结构，包名 `com.zong.<服务名>`：

```
services/
├── community/                     # 开发者社区服务（源码待梳理）
│   ├── src/main/java/com/zong/community/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/entity|dto|vo/
│   │   ├── config/
│   │   └── CommunityApplication.java
│   ├── src/main/resources/application.yml
│   ├── src/test/java/
│   ├── pom.xml
│   └── Dockerfile
│
├── knowledge/                     # 知识库服务（完整 RAG + 权限过滤）
│   ├── src/main/java/com/zong/knowledge/
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── rag/               #   RAG 核心逻辑（权限过滤 + 优化检索）
│   │   ├── repository/
│   │   ├── model/entity|dto|vo/
│   │   ├── config/
│   │   └── KnowledgeApplication.java
│   ├── src/main/resources/application.yml
│   ├── src/test/java/
│   ├── pom.xml
│   └── Dockerfile
│
└── agent/                         # 多 Agent 服务（调度 + 编排 + 轻量 RAG）
    ├── src/main/java/com/zong/agent/
    │   ├── controller/
    │   ├── service/
    │   │   ├── orchestration/      #   工作流编排引擎
    │   │   ├── agent/              #   Agent 定义与调度
    │   │   └── rag/                #   轻量 RAG（无权限过滤）
    │   ├── repository/
    │   ├── model/entity|dto|vo/
    │   ├── config/
    │   └── AgentApplication.java
    ├── src/main/resources/application.yml
    ├── src/test/java/
    ├── pom.xml
    └── Dockerfile
```

## gateway/ + shared/ + deploy/

```
gateway/                           # API 网关 + 场景编排层
├── src/main/java/com/zong/gateway/
│   ├── filter/                    # 鉴权、限流、日志
│   ├── route/                     # 路由规则
│   ├── orchestration/             # 跨服务场景编排
│   └── GatewayApplication.java
├── src/main/resources/application.yml
├── pom.xml
└── Dockerfile

shared/                            # 跨服务共享代码（Maven 依赖引入）
├── src/main/java/com/zong/shared/
│   ├── response/                  # 统一响应格式（Result<T>）
│   ├── exception/                 # 统一异常定义
│   ├── auth/                      # 鉴权工具类
│   └── constants/                 # 常量定义
└── pom.xml

deploy/                            # 部署配置
├── docker-compose.yml             # 本地开发
├── docker-compose.prod.yml        # 生产环境
├── nginx/default.conf             # 前端反向代理
└── scripts/                       # 部署脚本
```

## 服务间调用关系

```
Agent ──调用──→ Knowledge（获取带权限的检索结果）
前端 ──直接──→ Knowledge（直接检索文章）
前端 ──直接──→ Agent（编排+执行任务）
前端 ──直接──→ Community（社区内容）
前端 ──全部──→ Gateway（统一入口）
```

## Boundaries

- **Always do:** 统一包名 `com.zong.*`、统一响应格式 `Result<T>`、每个服务独立 Dockerfile
- **Ask first:** 修改 shared/ 代码、新增跨服务调用、修改 gateway 路由规则
- **Never do:** 服务间直接访问数据库、在 shared/ 放业务逻辑、跳过 gateway 直接暴露服务端口

## Open Questions

- community 源码待梳理，内部结构可能调整
- knowledge 和 agent 的 Spring Boot 版本是否统一
- RAG 场景统一后的接口规范待定义
- 原工作流前端的拖拽编排 UI 如何融入新 frontend（作为独立页面嵌入 / 作为组件复用 / 全部重写）
