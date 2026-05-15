# zong 平台架构规划

> 基于旧项目复用决策的统一架构设计
> 
> **讨论完成度**：✅ PaiFlow | ✅ PaiCLI | ✅ PaiSmart | ✅ zhishiku

---

## 1. 整体架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              zong 平台                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐              │
│  │   frontend  │     │   gateway   │     │   shared    │              │
│  │   (React)   │────▶│  (鉴权/路由) │────▶│  (通用组件)  │              │
│  └─────────────┘     └─────────────┘     │  + 统一AI层 │              │
│                              │           └─────────────┘              │
│                              │                  │                      │
│         ┌────────────────────┼──────────────────┼────────────────┐     │
│         │                    │                  │                │     │
│         ▼                    ▼                  ▼                ▼     │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐ ┌──────────┐ │
│  │   services  │     │   services  │     │   services  │ │ services │ │
│  │    agent    │     │  knowledge  │     │ community   │ │ shared/  │ │
│  │  (AI Agent) │     │   (RAG)     │     │  (社区)     │ │   ai    │ │
│  └─────────────┘     └─────────────┘     └─────────────┘ └──────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 模块职责

### 2.1 frontend

| 项目 | 说明 |
|------|------|
| **技术栈** | React + TypeScript + Vite + Ant Design |
| **定位** | zong 的 Web 前端 |

### 2.2 gateway

| 项目 | 说明 |
|------|------|
| **技术栈** | Spring Cloud Gateway |
| **定位** | 统一鉴权、路由转发、限流 |
| **复用来源** | PaiSmart JWT 认证逻辑 |

### 2.3 shared

| 项目 | 说明 |
|------|------|
| **技术栈** | 通用组件库 |
| **定位** | 被所有服务共享的基础组件 |
| **复用来源** | zhishiku paicoding-core + PaiCLI LlmClient |

#### 2.3.1 统一 AI 调用层（shared/ai）

```
shared/ai/
├── LlmClient.java              # 接口定义（来自 PaiCLI）
├── SpringAiLlmClient.java      # Spring AI Alibaba 实现
├── LlmFactory.java            # 工厂模式（来自 zhishiku）
└── models/
    ├── ChatGPTModel.java       # OpenAI GPT
    ├── DeepSeekModel.java      # DeepSeek
    └── ZhipuModel.java        # 智谱 GLM
```

#### 2.3.2 通用组件（shared/cache, shared/async, shared/trace）

| 组件 | 说明 |
|------|------|
| Redis 封装 | Pipeline 批处理、缓存穿透防护 |
| 异步执行框架 | 注解式异步、超时控制 |
| 链路追踪 | AOP + MDC + TraceID |
| 敏感词过滤 | 敏感词检测与替换 |

### 2.4 services/agent

| 项目 | 说明 |
|------|------|
| **定位** | AI Agent 核心服务 |
| **复用来源** | PaiCLI 架构 |
| **核心能力** | Agent 编排、工具调用、记忆管理 |

### 2.5 services/knowledge

| 项目 | 说明 |
|------|------|
| **定位** | 知识库 RAG 服务 |
| **复用来源** | PaiSmart RAG |
| **核心能力** | 文档解析、向量搜索、权限过滤 |

### 2.6 services/community

| 项目 | 说明 |
|------|------|
| **定位** | 开发者社区服务 |
| **复用来源** | zhishiku（备用） |
| **核心能力** | 文章、评论、用户互动 |

---

## 3. services/agent 详细设计

### 3.1 架构图

```
                    ┌─────────────────────────────────────┐
                    │           AgentController            │
                    │    /api/agent/chat (SSE 流式输出)     │
                    └──────────────────┬──────────────────┘
                                       │
                    ┌──────────────────▼──────────────────┐
                    │          AgentOrchestrator            │
                    │  (编排器：ReAct / Plan / MultiAgent)  │
                    └──────────────────┬──────────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          │                            │                            │
┌─────────▼─────────┐    ┌───────────▼───────────┐    ┌──────────▼──────────┐
│    LlmClient       │    │     ToolRegistry     │    │     MemoryManager    │
│ (Spring AI适配器)   │    │    (工具统一注册)     │    │   (短/长/压缩)        │
├────────────────────┤    ├───────────────────────┤    ├──────────────────────┤
│ Spring AI Alibaba │    │ 知识库 / 文件 / API    │    │ Session / VectorDB  │
│ ChatClient        │    │ MCP / Function Call   │    │ 上下文窗口管理        │
└────────────────────┘    └───────────────────────┘    └──────────────────────┘
```

### 3.2 模块结构

```
services/agent/
├── core/
│   ├── llm/
│   │   ├── LlmClient.java              # 来自 PaiCLI（接口）
│   │   └── SpringAiLlmClient.java      # Spring AI Alibaba 实现
│   ├── agent/
│   │   ├── Agent.java                  # ReAct 单代理循环
│   │   ├── PlanExecuteAgent.java      # Plan-and-Execute
│   │   ├── AgentOrchestrator.java     # 🔧 需改进：增强 Multi-Agent
│   │   └── AgentBudget.java           # Token 预算管理
│   └── memory/
│       ├── MemoryManager.java          # 🔧 需改进：智能压缩 + 对接 knowledge
│       ├── ConversationMemory.java     # 短期记忆
│       ├── LongTermMemory.java        # 🔧 改进：对接 services/knowledge
│       └── ContextCompressor.java     # 🔧 需改进：智能摘要
├── tool/
│   ├── ToolRegistry.java              # 工具注册 + 并行执行
│   └── plugins/
│       ├── KnowledgeTool.java          # 对接 services/knowledge
│       ├── FileTool.java              # 文件读写工具
│       └── McpTool.java              # MCP 协议支持
└── api/
    ├── AgentController.java            # REST API
    └── AgentSseController.java         # SSE 流式输出
```

### 3.3 双入口架构

```
                    ┌─────────────────────────────────┐
                    │        核心逻辑层（复用）          │
                    │   Agent / Memory / ToolRegistry │
                    └─────────────────────────────────┘
                              │              │
              ┌───────────────┼──────────────┘
              ▼                               ▼
    ┌─────────────────┐           ┌─────────────────┐
    │   CLI 入口       │           │   Web 入口       │
    │  (JLine/Lanterna)│           │  (REST/SSE)     │
    ├─────────────────┤           ├─────────────────┤
    │ 开发者工具/调试   │           │ 正式产品         │
    │ 前端未完成时备用  │           │ 可视化编排       │
    └─────────────────┘           └─────────────────┘
```

### 3.4 复用决策汇总

| 模块 | 决策 | 说明 |
|------|------|------|
| LlmClient 接口 | ⚠️ 调整后复用 | 保留接口，换 Spring AI Alibaba 实现 |
| ReAct 执行路径 | ✅ 直接复用 | Agent.java 单代理循环 |
| Plan 执行路径 | ✅ 直接复用 | PlanExecuteAgent.java |
| AgentOrchestrator | 🔧 需改进 | 多 Agent 共享 Memory、并行 Reviewer |
| MemoryManager | 🔧 需改进 | 智能摘要、对接 services/knowledge |
| ToolRegistry | ✅ 直接复用 | 工具注册 + 并行执行 |
| AgentBudget | ✅ 直接复用 | Token 预算管理 |
| 流式渲染 | ⚠️ 调整后复用 | CLI 流式 → Web SSE |
| MCP 客户端 | ✅ 复用 | stdio/HTTP 传输层通用 |
| CLI/TUI 渲染 | ✅ 保留备用 | CLI 双入口架构 |
| HITL 审批 | ⚠️ 按需复用 | 需调整到 Web 审批流程 |
| RAG 检索 | 🔧 需改进 | 代码解析保留，专业 RAG 用 PaiSmart |

---

## 4. services/knowledge 详细设计

### 4.1 架构图

```
文档上传 → MinIO 存储 → RocketMQ 异步 → Tika 解析 → HanLP 分词 → ES 向量索引
                                                          │
                                                          ▼
用户查询 ←── SSE ←── Agent ←── HybridSearch ←─── 权限过滤（OrgTag）
                              (向量 + 关键词)
```

### 4.2 模块结构

```
services/knowledge/
├── rag/
│   ├── VectorizationService           # 向量化服务（Tika + HanLP）
│   ├── HybridSearchService            # 混合搜索（向量 + 关键词）
│   ├── RagPermissionFilter            # 权限过滤（OrgTag）
│   └── CodeRetriever.java             # 代码检索（来自 PaiCLI）
├── index/
│   └── ElasticsearchIndexService     # ES 索引管理
└── storage/
    └── MinIOStorageService            # 对象存储（PaiSmart 复用）
```

### 4.3 复用决策汇总

| 模块 | 决策 | 说明 |
|------|------|------|
| RAG 权限过滤 | ✅ 直接复用 | OrgTag 权限系统 |
| 混合搜索 | ✅ 直接复用 | ES 向量 + 关键词 |
| 文档解析 | ✅ 直接复用 | Tika + HanLP 流水线 |
| 登录系统 | ✅ 直接复用 | JWT 认证 |
| 异步处理 | ⚠️ 用 RocketMQ 替代 Kafka | |
| 向量引擎 | ✅ Elasticsearch | |
| MinIO 封装 | ✅ 直接用 PaiSmart 的 | |
| 聊天通信 | ⚠️ SSE 替代 WebSocket | |
| Vue 前端 | ❌ 不复用 | zong 用 React |
| Spring Data JPA | ❌ 换 MyBatis-Plus | |

---

## 5. services/community 详细设计

### 5.1 架构图

```
用户 ──▶ 前端 ──▶ CommunityController
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌──────────┐   ┌──────────┐   ┌──────────┐
    │ Article  │   │ Comment  │   │ Markdown │
    │ Service  │   │ Service  │   │ Service  │
    └──────────┘   └──────────┘   └──────────┘
          │               │               │
          ▼               ▼               ▼
    ┌──────────┐   ┌──────────┐   ┌──────────┐
    │  MySQL   │   │  MySQL   │   │  Parser  │
    └──────────┘   └──────────┘   └──────────┘
```

### 5.2 模块结构

```
services/community/
├── article/
│   ├── ArticleController.java       # REST API
│   ├── ArticleService.java          # 服务接口
│   └── ArticleServiceImpl.java      # 服务实现
├── comment/
│   ├── CommentController.java       # REST API
│   ├── CommentService.java          # 服务接口
│   └── CommentServiceImpl.java      # 服务实现
├── markdown/
│   ├── MarkdownParser.java           # Markdown 解析
│   ├── CodeHighlightExtension.java  # 代码高亮
│   └── TocGenerator.java           # 目录生成
├── interaction/
│   ├── FavoriteService.java         # 收藏
│   ├── FollowService.java           # 关注
│   └── NotifyService.java           # 通知
└── user/
    └── UserService.java             # 用户相关
```

### 5.3 复用决策汇总

| 模块 | 决策 | 说明 |
|------|------|------|
| 文章管理 | ✅ 直接迁移 | CRUD、分类、标签、搜索 |
| 评论互动 | ✅ 直接迁移 | 嵌套回复、点赞 |
| Markdown 处理 | ✅ 直接迁移 | 代码高亮、目录生成 |
| 收藏/关注 | ✅ 直接迁移 | 用户互动逻辑 |
| 通知系统 | ✅ 直接迁移 | 消息通知 |

---

## 6. 技术栈选型

### 6.1 框架版本

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.2.x |
| Java | 17（gateway/agent 用 21 待定） |
| Spring AI Alibaba | 最新稳定版 |
| MySQL | 8.0 |
| Redis | 7.x |
| Elasticsearch | 8.x |
| RocketMQ | 5.x |
| MinIO | 最新版 |

### 6.2 待确认项

| 项目 | 选项 |
|------|------|
| Java 21 | agent 服务是否升级到 Java 21（PaiCLI 用的是 21） |

---

## 7. 实施优先级

### 阶段一：基础骨架（1-2周）

```
1. services/agent 基础骨架
   ├── LlmClient 接口 + SpringAiLlmClient 实现
   ├── Agent.java（ReAct 循环）
   └── 基础 Chat API + SSE

2. services/knowledge 基础骨架
   ├── MinIO 存储集成
   ├── ES 向量索引
   └── 基础 RAG API
```

### 阶段二：核心功能（3-4周）

```
1. services/agent 核心
   ├── ToolRegistry 实现
   ├── MemoryManager 实现（对接 knowledge）
   ├── AgentOrchestrator 增强
   └── AgentBudget 集成

2. services/knowledge 核心
   ├── Tika + HanLP 文档解析
   ├── HybridSearch 混合搜索
   ├── OrgTag 权限过滤
   └── RocketMQ 异步流水线
```

### 阶段三：进阶功能（5-8周）

```
1. Plan-and-Execute 编排
2. Multi-Agent 协作
3. MCP 协议支持
4. CLI 入口完善
5. 可视化编排 UI
```

---

## 8. 文档清单

| 文档 | 位置 |
|------|------|
| PaiFlow 复用决策 | `docs/复用决策/PaiFlow-复用决策.md` |
| PaiCLI 复用决策 | `docs/复用决策/PaiCLI-复用决策.md` |
| PaiSmart 复用决策 | `docs/复用决策/PaiSmart-复用决策.md` |
| zhishiku 复用决策 | `docs/复用决策/zhishiku-复用决策.md` |
| 本架构规划 | `docs/架构规划/zong-架构规划.md` |

---

*本文档基于 2026-05-15 的讨论生成，如有调整请更新相关复用决策文档。*
