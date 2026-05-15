# PaiCLI 复用决策记录

## 基本信息

| 项目 | 定位 | 分析日期 |
|------|------|----------|
| PaiCLI | Java Agent CLI 产品（对标 Claude Code） | 2026-05-15 |

---

## 复用决策总览

| 模块 | 决策 | 说明 |
|------|------|------|
| LlmClient 接口 | ⚠️ 调整后复用 | 保留接口，换 Spring AI Alibaba 实现 |
| ReAct 执行路径 | ✅ 直接复用 | Agent.java 单代理循环 |
| Plan 执行路径 | ✅ 直接复用 | PlanExecuteAgent.java |
| AgentOrchestrator | 🔧 需改进 | Multi-Agent 编排需增强 |
| MemoryManager | 🔧 需改进 | 长期记忆和压缩策略需优化 |
| ToolRegistry | ✅ 直接复用 | 工具注册 + 并行执行 |
| AgentBudget | ✅ 直接复用 | Token 预算管理 |
| 流式渲染 | ⚠️ 调整后复用 | CLI 流式 → Web SSE |
| MCP 客户端 | ✅ 复用 | stdio/HTTP 传输层通用 |
| CLI/TUI 渲染 | ✅ 保留备用 | CLI 双入口架构 |
| HITL 审批 | ⚠️ 按需复用 | 需调整到 Web 审批流程 |
| RAG 检索 | 🔧 需改进 | 代码解析保留，专业 RAG 用 PaiSmart |

---

## 详细决策

### ✅ 直接复用

#### 1. 三条执行路径

```
PaiCLI 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
Agent.java                              Agent.java
(ReAct 单代理循环)                       (直接移植)

PlanExecuteAgent.java                  PlanExecuteAgent.java
(Plan → 确认 → DAG执行)                  (直接移植)

AgentOrchestrator.java                 AgentOrchestrator.java
(Planner + Worker + Reviewer)          (直接移植)
```

**复用理由：**
- 21 期验证，执行路径逻辑成熟
- 与 Web 服务模式完全兼容
- 只需替换 LLM 调用层

---

#### 2. MemoryManager

```java
// 保留分层设计
public class MemoryManager {
    ConversationMemory  // 短期：当前会话
    LongTermMemory     // 长期：向量数据库
    ContextCompressor  // 压缩：防 window 超限
}
```

**复用理由：**
- 三层设计通用，适用于任何 Agent
- 实现可复用，存储层按需替换（SQLite → MySQL/Redis）

---

#### 3. ToolRegistry

```java
public class ToolRegistry {
    void registerTool(ToolDefinition def, ToolExecutor executor);
    List<ToolExecutionResult> executeTools(List<ToolInvocation> invocations);
    // 保留：并行执行、统一超时、取消机制
}
```

**复用理由：**
- 统一工具入口，zong 需要接入知识库/文件/MCP
- PaiCLI 已验证并行执行和超时控制

---

#### 4. AgentBudget

```java
public class AgentBudget {
    // 按模型动态计算 budget = 80% * maxContextWindow
    // 触发压缩：triggerTokens = 75% * budget
    // 硬轮数限制防死循环
}
```

**复用理由：**
- 防止 LLM 死循环的必备机制
- 直接移植，无需修改

---

#### 5. MCP 客户端

```java
public class McpClient {
    // stdio 传输：启动子进程
    // Streamable HTTP：REST 调用
}
```

**复用理由：**
- MCP 是开放协议，传输层代码通用
- zong 可以调用外部 MCP Server

---

### 🔧 需改进

#### 1. AgentOrchestrator（Multi-Agent 编排）

```
PAICLI 原设计                          ZONG 改进方向
─────────────────────────────────────────────────────────────────
Planner + Worker + Reviewer           增强版 Multi-Agent
├── 固定三角色                         ├── 角色可配置
├── 简单结果传递                       ├── 共享 Memory
└── 串行 Reviewer                     └── 并行 Reviewer
```

**改进点：**
- 多 Agent 间共享 Memory，而非独立记忆
- Reviewer 可以并行审查多个 Worker 结果
- 支持子 Agent 池（类似 PaiCLI Worker 池但更灵活）
- 增加 Agent 间的消息传递机制

---

#### 2. MemoryManager

```
PAICLI 原设计                          ZONG 改进方向
─────────────────────────────────────────────────────────────────
ConversationMemory                    改进版 MemoryManager
├── 简单消息列表                       ├── 支持多模态（图片/文件）
├── 固定窗口截断                       ├── 智能摘要（不只是截断）
└── SQLite LongTermMemory             └── 对接 services/knowledge（专业 RAG）
```

**改进点：**
- **短期记忆**：支持多模态内容（不只是文本）
- **上下文压缩**：不只是简单截断，需要智能摘要（可用 LLM）
- **长期记忆**：不自己实现 RAG，对接 `services/knowledge` 的专业 RAG
- **记忆检索**：基于语义检索而非简单关键词

---

#### 3. RAG 检索

```
PAICLI RAG（代码专用）                  PaiSmart RAG（文档专业）
─────────────────────────────────────────────────────────────────
CodeChunker                            VectorizationService
├── 基于 AST 分块                      ├── Tika 文档解析
├── 简单 import 分析                   ├── HanLP 中文分词
└── SQLite VectorStore                 └── Elasticsearch 向量搜索
```

**复用策略：**

| 组件 | 决策 | 说明 |
|------|------|------|
| `CodeChunker` | ✅ 保留 | 代码解析逻辑通用 |
| `CodeRetriever` | ✅ 保留 | 检索思路可复用 |
| `VectorStore` | ❌ 替换 | 换用 Elasticsearch |
| 文档解析 | ✅ 用 PaiSmart | Tika + HanLP 流水线 |
| 向量搜索 | ✅ 用 PaiSmart | Elasticsearch 混合搜索 |
| RAG 权限过滤 | ✅ 用 PaiSmart | OrgTag 权限控制 |

**改进后架构：**
```
services/agent/                         services/knowledge/
─────────────────                       ──────────────────
RAG 检索入口                              专业 RAG 实现
     │                                        │
     ▼                                        ▼
CodeRetriever ◄────── 对接 ──────────► VectorizationService
(代码检索)                                  (Tika + ES + 权限)
     │                                        │
     ▼                                        ▼
Agent Memory                              统一向量索引
```

---

### ⚠️ 调整后复用

#### 1. LlmClient 接口 + Spring AI 实现

```
PAICLI 原设计                          ZONG 调整后
───────────────────────                ──────────────────────────
LlmClient 接口                         LlmClient 接口
     │                                      │
     ▼                                 ┌───▼────────────┐
GLMClient / DeepSeekClient             │ SpringAiClient  │
(自封装 HTTP)                           │ (实现 LlmClient) │
                                      └─────────────────┘
                                              │
                                              ▼
                                      Spring AI Alibaba
                                      ChatClient / Function
```

**调整内容：**
- 保留 `LlmClient` 接口定义（PaiCLI 的抽象好）
- 不复用具体 Client，换用 Spring AI Alibaba 的实现
- 适配器模式：`SpringAiLlmClient implements LlmClient`

**接口定义示例：**
```java
public interface LlmClient {
    ChatResponse chat(List<Message> messages, List<Tool> tools, StreamListener listener);
    int maxContextWindow();
    // ...
}
```

---

#### 2. 流式渲染 → SSE 推送

```
PAICLI 原设计                          ZONG 调整后
───────────────────────                ──────────────────────────
StreamListener                         StreamListener
(thinking_delta + content_delta)       (thinking_delta + content_delta)
     │                                      │
     ▼                                      ▼
Renderer 接口                           SseEmitter / Flux<ServerSentEvent>
├─ InlineRenderer                      AgentSseController.java
├─ PlainRenderer
└─ LanternaRenderer
```

**调整内容：**
- 保留 `StreamListener` 接口
- 不复用 Renderer，换用 Spring WebFlux 的 SSE
- 前端 EventSource 消费

---

#### 3. HITL 审批

```
PAICLI 原设计                          ZONG 调整后
───────────────────────                ──────────────────────────
HitlHandler.java                       HitlService.java
(命令行确认)                            (Web 审批 API + 消息通知)
```

**调整理由：**
- CLI 的 `readLine()` 无法用于 Web
- 改为 REST API + 前端弹窗确认
- 保留危险等级判断逻辑

---

### ❌ 不复用

| 模块 | 原因 |
|------|------|
| Git 快照 | 非核心功能，zong 可后续按需添加 |
| Chrome DevTools MCP | 可作为外部 MCP Server 调用 |

### ✅ 保留备用（CLI 双入口架构）

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

**CLI 保留定位**：
- 开发者调试工具
- 自动化脚本入口
- 前端未完成时的备用方案

**CLI 不能替代 Web 的原因**：
- 无可视化工作流编排（只能用命令/DSL）
- 多模态展示能力弱（文本为主）
- 用户门槛高（非技术人员不会用）

---

---

## 迁移后的 zong agent 架构

```
services/agent/
├── core/
│   ├── llm/
│   │   ├── LlmClient.java              # 来自 PaiCLI（接口保留）
│   │   └── SpringAiLlmClient.java      # 新增：Spring AI 适配器
│   ├── agent/
│   │   ├── Agent.java                  # 直接移植
│   │   ├── PlanExecuteAgent.java      # 直接移植
│   │   ├── AgentOrchestrator.java     # 🔧 需改进：增强 Multi-Agent
│   │   └── AgentBudget.java           # 直接移植
│   └── memory/
│       ├── MemoryManager.java          # 🔧 需改进：智能压缩 + 对接 knowledge
│       ├── ConversationMemory.java     # 直接移植
│       ├── LongTermMemory.java        # 🔧 改进：对接 services/knowledge
│       └── ContextCompressor.java     # 🔧 需改进：智能摘要
├── tool/
│   ├── ToolRegistry.java              # 直接移植
│   └── plugins/
│       ├── KnowledgeTool.java          # 🔧 改进：对接 services/knowledge
│       ├── FileTool.java              # 新增
│       └── McpTool.java              # 来自 PaiCLI 调整
└── api/
    ├── AgentController.java            # 新增（REST API）
    └── AgentSseController.java         # 新增（SSE 流式）

services/knowledge/                      # 🔧 RAG 核心
├── rag/
│   ├── VectorizationService           # 来自 PaiSmart（Tika + HanLP）
│   ├── HybridSearchService            # 来自 PaiSmart（向量 + 关键词）
│   └── RagPermissionFilter            # 来自 PaiSmart（OrgTag 权限）
└── index/
    └── CodeRetriever.java             # 来自 PaiCLI（保留代码检索）
```

---

## 决策理由

```
┌─────────────────────────────────────────────────────────────────┐
│  为什么 PaiCLI 可以大量复用？                                     │
├─────────────────────────────────────────────────────────────────┤
│  1. 技术栈匹配                                                    │
│     Java 17 + 面向对象 + 设计模式 → 与 zong 完全一致               │
│                                                                  │
│  2. 成熟度验证                                                    │
│     21 期演进 → 经过生产验证，坑已填完                             │
│                                                                  │
│  3. 架构合理                                                      │
│     接口抽象 + 实现分离 → 替换 LLM 层 不影响核心逻辑               │
│                                                                  │
│  4. 复用 vs 重写 的权衡                                           │
│     重写 = 自己踩坑 vs 复用 = 站在巨人肩膀上                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 下一步

PaiCLI 架构将作为 `services/agent` 模块的实现骨架。

**实施顺序建议：**
1. 先搭建 Agent 骨架（ReAct 循环 + SSE 输出）
2. 再接入 Spring AI Alibaba（LlmClient 适配）
3. 最后扩展工具和 Memory

---

## 决策人 & 日期

- 决策人：[待填写]
- 决策日期：2026-05-15
