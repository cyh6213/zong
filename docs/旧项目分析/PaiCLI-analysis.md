# PaiCLI 项目分析摘要

> 分析日期：2026-05-15
> 分析目的：为 zong 平台 services/agent 模块提供参考
> 项目路径：D:\biancheng\Coding\CLI\paicli-main

---

## 1. 项目概览

| 项目信息 | 详情 |
|---------|------|
| **项目名称** | PaiCLI（Java Agent CLI 产品） |
| **项目类型** | Java CLI 应用（对标 Claude Code） |
| **文件规模** | 289 Java 源文件，演进到第 21 期 |
| **定位** | 面向商业使用的 Java Agent CLI 产品 |
| **技术栈** | Java 17 + Maven + Spring AI Alibaba（用户经验） |

**核心功能定位（21 期演进）：**

| 期数 | 功能 | 说明 |
|------|------|------|
| 1 | ReAct Agent | 单轮对话驱动的 ReAct 循环 |
| 2 | Plan-and-Execute + DAG | 先拆解任务，再按依赖顺序执行 |
| 3 | Memory | 短期记忆 + 长期记忆 + 上下文压缩 |
| 4 | RAG 检索 | 代码向量化 + SQLite 持久化 + 语义检索 |
| 5 | Multi-Agent | Planner + Worker + Reviewer 三角色协作 |
| 6 | HITL | Human-in-the-Loop 审批流 |
| 7 | 并行执行 | 批量工具并行 + DAG 批次并行 |
| 8 | 多模型适配 | GLM / DeepSeek / Step / Kimi |
| 9 | 联网能力 | web_search + web_fetch |
| 10 | MCP 协议核心 | stdio + Streamable HTTP |
| 12 | 长上下文工程 | Token 预算 + Prompt Cache |
| 13 | Chrome DevTools MCP | 浏览器自动化 |
| 14 | CDP 会话复用 | 登录态复用 |
| 15 | Skill 系统 | Prompt 模板热插拔 |
| 16 | TUI 产品化 | inline / lanterna / plain 三形态 |
| 17 | LSP 诊断注入 | JavaParser 语法诊断 |
| 18 | Git Side-History | 快照与回滚 |
| 19 | Prompt 分层架构 | Prompt 外部化 |
| 20 | Runtime API | 异步后台任务 + HTTP API |
| 21 | 图片输入 | 多模态支持 |

---

## 2. 技术栈

### 2.1 核心技术

| 技术 | 版本 | 用途 |
|------|------|------|
| **Java** | 17 | 编程语言 |
| **Maven** | - | 构建工具 |
| **OkHttp** | 4.12.0 | HTTP 客户端 |
| **Jackson** | 2.16.0 | JSON 处理 |
| **JLine** | 4.0.0 | 终端交互 |
| **SQLite** | 3.49.1.0 | 向量存储 + 持久化 |
| **JavaParser** | 3.28.0 | AST 解析 |
| **JGit** | 7.6.0 | Git 快照 |
| **Jsoup** | 1.18.1 | HTML 解析 |
| **Lanterna** | 3.1.3 | TUI 渲染 |
| **Logback** | 1.5.18 | 日志 |

### 2.2 AI 集成

| 技术 | 说明 |
|------|------|
| **LlmClient 接口** | LLM 抽象，支持多 Provider |
| **GLMClient** | 智谱 GLM-5.1 / GLM-5V-Turbo |
| **DeepSeekClient** | DeepSeek V4 |
| **StepClient** | 阶跃星辰 StepFun |
| **KimiClient** | Kimi / Moonshot K2.6 |
| **EmbeddingClient** | 向量化（Ollama / 远程 API） |

---

## 3. 架构分析

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLI 入口层 (Main.java)                     │
│              命令解析 / REPL / HITL 审批 / TUI 渲染               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Agent 执行层（三条路径）                      │
├──────────────────┬──────────────────┬──────────────────────────┤
│    Agent.java    │ PlanExecuteAgent │  AgentOrchestrator.java  │
│    (ReAct)       │   (Plan+DAG)     │     (Multi-Agent)        │
│                  │                  │                          │
│  单代理循环       │  规划→确认→执行   │  Planner+Worker+Reviewer │
└──────────────────┴──────────────────┴──────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   MemoryManager  │ │   ToolRegistry   │ │   LlmClient     │
│  短期/长期/压缩   │ │   工具注册执行   │ │   LLM 接口抽象  │
└─────────────────┘ └─────────────────┘ └─────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   RAG 检索      │ │   MCP 客户端    │ │   Web 工具      │
│  VectorStore    │ │   McpServerMgr  │ │  search/fetch   │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### 3.2 项目模块划分

```
src/main/java/com/paicli/
├── agent/                     # Agent 核心（ReAct / Plan / Multi-Agent）
│   ├── Agent.java             # ReAct 单代理循环
│   ├── PlanExecuteAgent.java  # Plan-and-Execute
│   ├── AgentOrchestrator.java # Multi-Agent 编排器
│   ├── SubAgent.java          # 子代理（Planner/Worker/Reviewer）
│   ├── AgentBudget.java       # Token 预算管理
│   └── AgentRole.java         # 角色枚举
├── cli/                       # CLI 入口
│   ├── Main.java              # 主入口
│   └── CliCommandParser.java  # 命令解析
├── llm/                       # LLM 客户端
│   ├── LlmClient.java         # 核心接口
│   ├── GLMClient.java          # 智谱
│   ├── DeepSeekClient.java    # DeepSeek
│   ├── StepClient.java        # 阶跃星辰
│   └── KimiClient.java        # Kimi
├── memory/                    # 记忆系统
│   ├── MemoryManager.java     # 记忆门面
│   ├── ConversationMemory.java# 短期记忆
│   ├── LongTermMemory.java    # 长期记忆
│   ├── ContextCompressor.java # 上下文压缩
│   └── TokenBudget.java       # Token 预算
├── plan/                      # 规划系统
│   ├── Planner.java           # 规划器
│   ├── ExecutionPlan.java     # 执行计划
│   └── Task.java              # 任务定义
├── tool/                      # 工具系统
│   └── ToolRegistry.java      # 工具注册表
├── mcp/                       # MCP 协议
│   ├── McpClient.java         # MCP 客户端
│   ├── McpServerManager.java  # Server 管理
│   └── transport/             # 传输层（stdio/HTTP）
├── rag/                       # RAG 检索
│   ├── VectorStore.java        # SQLite 向量存储
│   ├── CodeIndex.java         # 索引管理
│   └── CodeRetriever.java     # 检索入口
├── web/                       # Web 工具
│   ├── SearchProvider.java    # 搜索抽象
│   └── WebFetcher.java        # 网页抓取
├── skill/                     # Skill 系统
│   ├── SkillRegistry.java     # Skill 注册
│   └── SkillContextBuffer.java# Skill 上下文
├── hitl/                      # HITL 审批
│   └── HitlHandler.java      # 审批处理
├── policy/                    # 安全策略
│   ├── PathGuard.java         # 路径围栏
│   └── CommandGuard.java      # 命令黑名单
├── render/                    # 渲染层
│   ├── Renderer.java          # 渲染接口
│   ├── InlineRenderer.java   # inline 流式
│   └── PlainRenderer.java    # 纯文本
└── runtime/                  # Runtime API
    └── api/RuntimeApiServer.java
```

### 3.3 三条执行路径详解

#### 路径一：ReAct Agent（Agent.java）

```
用户输入 → 循环 {
    调用 LLM（带 tools）
    ↓
    LLM 返回 tool_calls?
    ├─ 是：执行工具 → 回灌结果 → 继续循环
    └─ 否：返回结果 → 结束
}
```

**核心特性：**
- Token 预算管理（`AgentBudget`）
- 对话历史自动压缩
- 并行工具执行
- 流式输出渲染（reasoning + content 分区）
- LSP 诊断注入

#### 路径二：Plan-and-Execute（PlanExecuteAgent.java）

```
用户输入 → Planner 生成计划 → 用户确认 → 执行 {
    while (有待执行任务) {
        获取可执行批次（依赖已完成）
        ├─ 单任务：串行执行
        └─ 多任务：并行执行（最多 4 线程）
        ↓
        执行每个任务（内部 ReAct 循环）
        ↓
        任务失败 + 进度 < 50%：重规划
    }
}
```

**核心特性：**
- DAG 依赖管理
- 批次并行执行
- 任务内 ReAct 循环
- 自动重规划

#### 路径三：Multi-Agent（AgentOrchestrator.java）

```
用户输入 → Planner 拆解任务 → 解析步骤 → 执行 {
    while (有待执行步骤) {
        获取可执行批次
        ├─ 单步骤：Worker 执行 → Reviewer 审查
        └─ 多步骤：并行 Worker → 并行 Reviewer
        ↓
        审查通过？→ 标记完成
        └─ 不通过 → 重试（最多 2 次）
    }
}
```

**核心特性：**
- Planner + Worker + Reviewer 三角色
- Worker 池（默认 2 个）
- 审查反馈驱动的重试
- 步骤级并行

---

## 4. 核心设计模式

### 4.1 LLM 接口抽象（LlmClient）

```java
public interface LlmClient {
    ChatResponse chat(List<Message> messages, List<Tool> tools, StreamListener listener);
    int maxContextWindow();
    // ...
}
```

**优点：** 新增 Provider 只需 ~20 行代码（模板方法基类 `AbstractOpenAiCompatibleClient`）

### 4.2 工具注册执行（ToolRegistry）

```java
public class ToolRegistry {
    void registerTool(ToolDefinition def, ToolExecutor executor);
    List<ToolExecutionResult> executeTools(List<ToolInvocation> invocations);
}
```

**优点：** 统一入口，支持并行执行、统一超时、取消

### 4.3 记忆系统（MemoryManager）

```java
public class MemoryManager {
    // 短期记忆：当前对话
    // 长期记忆：跨会话持久化
    // 上下文压缩：防 window 超限
}
```

### 4.4 流式渲染（Renderer 接口）

```java
public interface Renderer {
    OutputStream stream();           // 流式输出
    void beginThinking(String msg);  // 开始思考
    void appendThinking(String delta);// 思考增量
    void updateStatus(StatusInfo info);
}
```

**实现：** InlineRenderer / PlainRenderer / LanternaRenderer

---

## 5. 对 zong services/agent 的参考价值

### 5.1 高复用价值设计

| 设计 | 说明 | 迁移目标 |
|------|------|----------|
| **LlmClient 接口** | 多 Provider 抽象，统一调用方式 | 直接复用或扩展 |
| **三条执行路径** | ReAct / Plan / Multi-Agent | 核心参考 |
| **MemoryManager** | 短期/长期/压缩分层 | 直接复用 |
| **ToolRegistry** | 工具注册 + 并行执行 | 直接复用 |
| **AgentBudget** | Token 预算管理 | 直接复用 |
| **流式渲染** | reasoning + content 分区 | 参考设计 |

### 5.2 中复用价值设计

| 设计 | 说明 |
|------|------|
| **MCP 客户端** | stdio + HTTP 传输，支持动态工具 |
| **RAG 检索** | SQLite 向量存储 |
| **HITL 审批** | 三级危险等级 + 审批流 |
| **Skill 系统** | Prompt 模板热插拔 |
| **PromptAssembler** | 分层 Prompt 组装 |

### 5.3 低复用价值设计

| 设计 | 说明 | 原因 |
|------|------|------|
| **CLI/TUI 渲染** | JLine/Lanterna 终端交互 | zong 是 Web 应用 |
| **Git 快照** | JGit side-git | 非核心功能 |
| **Chrome DevTools MCP** | 浏览器自动化 | 可作为外部 MCP Server |

---

## 6. 架构亮点

### 6.1 Token 预算管理

```java
public class AgentBudget {
    // 按模型动态计算 budget = 80% * maxContextWindow
    // 触发压缩：triggerTokens = 75% * budget
    // 硬轮数限制防死循环
}
```

### 6.2 并行执行策略

```java
// 工具级并行
toolRegistry.executeTools(invocations);  // 最多 4 并发

// 任务级并行（Plan & Multi-Agent）
ExecutorService executor = Executors.newFixedThreadPool(4);
```

### 6.3 流式渲染分层

```
LLM Stream
    ↓
StreamListener (reasoning_delta + content_delta)
    ↓
Renderer 接口
    ├─ InlineRenderer：JLine 动态渲染
    ├─ PlainRenderer：println 兜底
    └─ LanternaRenderer：全屏 TUI
```

---

## 7. 迁移建议

### 7.1 直接复用（推荐）

以下模块可直接迁移到 `services/agent`：

```
services/agent/
├── core/
│   ├── LlmClient.java          # 来自 PaiCLI llm/
│   ├── Agent.java              # 来自 PaiCLI agent/
│   ├── PlanExecuteAgent.java   # 来自 PaiCLI agent/
│   ├── AgentOrchestrator.java  # 来自 PaiCLI agent/
│   ├── AgentBudget.java        # 来自 PaiCLI agent/
│   └── MemoryManager.java      # 来自 PaiCLI memory/
├── tool/
│   ├── ToolRegistry.java       # 来自 PaiCLI tool/
│   └── plugins/                # 扩展点
├── memory/
│   ├── ConversationMemory.java
│   ├── LongTermMemory.java
│   └── ContextCompressor.java
└── runtime/
    └── RuntimeApiServer.java   # 来自 PaiCLI runtime/
```

### 7.2 参考设计

以下模块参考 PaiCLI 思路自研：

- Web 接口层（REST API 替代 CLI）
- WebSocket/SSE 推送（替代流式渲染）
- 前端工作流编辑器（React Flow）

### 7.3 不迁移

- CLI / TUI 渲染层
- 终端交互（JLine）
- Git 快照
- 浏览器 MCP

---

## 8. 总结

PaiCLI 是一个**极度成熟**的 Java Agent 实现，21 期演进覆盖了：

- ✅ 三种执行范式（ReAct / Plan / Multi-Agent）
- ✅ 完整的 Memory 系统
- ✅ 工具注册与并行执行
- ✅ MCP 协议支持
- ✅ Token 预算管理
- ✅ 流式渲染架构
- ✅ HITL 安全审批

**对 zong 的价值：**
- **可直接复用** LLM 抽象、三条执行路径、Memory 系统
- **参考设计** Web 接口层、前端编辑器
- **技术栈匹配** Java 17 + Spring AI Alibaba（用户有经验）

---

*本文档由 AI 自动生成，基于项目扫描分析。如需补充或修正，请手动编辑。*
