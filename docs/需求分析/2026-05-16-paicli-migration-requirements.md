# PaiCLI 移植与改造需求分析

> 创建日期：2026-05-16
> 创建人：[待填写]
> 状态：待审核

---

## 1. 背景与目标

### 1.1 项目背景

Zong 平台需要构建 `services/agent` 模块，作为多 Agent 服务编排的核心组件。经过对现有项目的分析，决定：
- **基础框架**：移植 PaiCLI（Java Agent CLI 产品，21 期演进，成熟稳定）
- **DAG 功能**：参考 PaiFlow 的 DAG JSON 格式和解析逻辑
- **RAG 功能**：调用 PaiSmart (services/knowledge) 的 HTTP API

### 1.2 项目目标

构建一个支持以下能力的 Agent 编排服务：
1. **三条执行路径**：ReAct 单代理循环、Plan-and-Execute（DAG 执行）、Multi-Agent 编排
2. **DAG 工作流编排**：前端传入 JSON 格式的工作流定义，解析为 DAG 图并执行 
3. **知识库集成**：通过 HTTP API 调用 services/knowledge 实现 RAG 检索
4. **SSE 流式输出**：适配 Web 环境，实现流式响应

---

## 2. 功能需求

### 2.1 Agent 核心功能（来自 PaiCLI）

#### 2.1.1 ReAct Agent（Agent.java）
- **功能描述**：单轮对话驱动的 ReAct 循环
- **核心逻辑**：
  ```
  用户输入 → 循环 {
      调用 LLM（带 tools）
      ↓
      LLM 返回 tool_calls?
      ├─ 是：执行工具 → 回灌结果 → 继续循环
      └─ 否：返回结果 → 结束
  }
  ```
- **关键特性**：
  - Token 预算管理（AgentBudget）
  - 对话历史自动压缩
  - 并行工具执行
  - 流式输出渲染（thinking + content 分区）

#### 2.1.2 Plan-and-Execute（PlanExecuteAgent.java）
- **功能描述**：先拆解任务，再按依赖顺序执行
- **核心逻辑**：
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
- **关键特性**：
  - DAG 依赖管理
  - 批次并行执行
  - 任务内 ReAct 循环
  - 自动重规划

#### 2.1.3 Multi-Agent（AgentOrchestrator.java）
- **功能描述**：Planner + Worker + Reviewer 三角色协作
- **核心逻辑**：
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
- **关键特性**：
  - Worker 池（默认 2 个）
  - 审查反馈驱动的重试
  - 步骤级并行

### 2.2 DAG JSON 解析功能（参考 PaiFlow）

#### 2.2.0 核心背景与需求

**一、PaiCLI 现有 DAG 执行模式**
- **模式一**：Plan-and-Execute（解析 DAG 图，支持并行执行）
- **模式二**：Multi-Agent 模式（主 Agent 负责任务分发，多子 Agent 并行执行 DAG 图）
- ✅ PaiCLI 已集成 DAG 执行框架，执行逻辑可直接复用

**二、PaiFlow 现有能力**
- **前端**：可视化界面编排执行流程，自动将编排结果转换为 JSON 格式
- **后端**：实现 JSON 解析，可将 JSON 解析为 DAG 图并完成执行

**三、技术复用关系**
- PaiCLI 已具备 DAG 执行能力（✅ 直接复用）
- PaiCLI 不具备 JSON 解析能力（❌ 需借鉴 PaiFlow 的核心逻辑）
- PaiCLI 不具备 DAG 图转 JSON 能力（❌ 需新增功能）

**四、核心需求（新增功能）**

PaiCLI 当前通过 AI 自动生成执行流程，存在两大痛点：
1. **生成的执行流程无法直观展示**（仅 CLI 界面，可视化效果差）
2. **用户难以手动修改 AI 生成的执行流程**

因此需要为 PaiCLI 后端**新增核心功能**：
- 将 AI 自动生成的执行流程，**反向转换为标准 JSON 格式**，并推送至前端

**五、最终目标**

实现 PaiCLI 「**AI 生成流程 → 转 JSON 可视化展示 → 用户修改 → 解析执行**」的完整闭环：

```
┌─────────────────┐
│  AI 生成执行流程  │ (DAG 图，PaiCLI 原生能力)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  DAG 图 → JSON   │ (新增：反向转换功能)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  前端可视化展示   │ (PaiFlow 前端能力)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  用户修改流程    │ (PaiFlow 前端能力)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  JSON → DAG 图   │ (借鉴 PaiFlow：正向解析)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   执行 DAG 图    │ (PaiCLI 原生执行能力)
└─────────────────┘
```

**六、总结**
- ✅ 复用 PaiCLI 原生 DAG 执行能力
- ✅ 借鉴 PaiFlow 前后端 JSON 与 DAG 转换逻辑
- ✅ 为 PaiCLI 新增执行流程转 JSON 后端功能
- ✅ 实现执行流程可视化展示与便捷修改，补齐 CLI 工具的交互短板

---

#### 2.2.1 功能描述（正向：JSON → DAG）

前端通过可视化编辑器（React Flow）编排工作流，导出 JSON 格式的工作流定义，后端解析该 JSON 并转换为 DAG 图，然后按照 DAG 依赖顺序执行各个节点。

**技术实现**：
- 复用 PaiFlow 的 `WorkflowDSL`、`Node`、`Edge` 定义
- 实现 JSON → `WorkflowDSL` 解析（Jackson 反序列化）
- 转换为 PaiCLI 的 DAG 执行格式（兼容原有执行逻辑）

---

#### 2.2.2 功能描述（反向：DAG → JSON，新增）

将 PaiCLI AI 自动生成的执行流程（DAG 图）转换为标准 JSON 格式，通过 SSE 推送给前端，实现可视化展示和修改。

**触发场景**：
1. 用户使用 ReAct 模式时，展示思考过程
2. 用户使用 Plan-and-Execute 模式时，展示 AI 生成的执行计划
3. 用户使用 Multi-Agent 模式时，展示多 Agent 协作流程

**技术实现**：
- 读取 PaiCLI 的 DAG 执行计划（PlanExecuteAgent 生成的计划）
- 读取 Multi-Agent 的执行流程（AgentOrchestrator 的编排）
- 转换为标准 JSON 格式（兼容 PaiFlow 的 JSON 格式）
- 通过 SSE 推送给前端（`event: workflow_json`）

**JSON 格式定义**（兼容 PaiFlow）：
```json
{
  "flowId": "ai-generated-001",
  "flowName": "AI 生成的执行流程",
  "nodes": [
    {
      "id": "node-llm::001",
      "type": "llm",
      "data": {
        "inputs": [...],
        "nodeMeta": {
          "title": "分析用户需求",
          "description": "AI 自动生成"
        },
        "nodeParam": {
          "model": "qwen-max",
          "prompt": "..."
        }
      }
    }
  ],
  "edges": [
    {
      "sourceNodeId": "node-start::001",
      "targetNodeId": "node-llm::001"
    }
  ]
}
```

#### 2.2.2 DAG JSON 格式定义
参考 PaiFlow 的 WorkflowDSL 格式：
```json
{
  "flowId": "workflow-001",
  "uuid": "unique-uuid",
  "nodes": [
    {
      "id": "node-start::001",
      "data": {
        "inputs": [...],
        "nodeMeta": {
          "title": "开始节点",
          "description": "工作流起点"
        },
        "nodeParam": {
          "param1": "value1"
        },
        "outputs": [...],
        "retryConfig": {...}
      }
    },
    {
      "id": "node-llm::002",
      "data": {
        "inputs": [...],
        "nodeMeta": {
          "title": "LLM 节点",
          "description": "调用大模型"
        },
        "nodeParam": {
          "model": "qwen-max",
          "prompt": "..."
        }
      }
    },
    {
      "id": "node-end::003",
      "data": {
        "inputs": [...],
        "nodeMeta": {
          "title": "结束节点"
        },
        "nodeParam": {
          "outputMode": 1
        }
      }
    }
  ],
  "edges": [
    {
      "sourceNodeId": "node-start::001",
      "targetNodeId": "node-llm::002",
      "sourceHandle": "normal"
    },
    {
      "sourceNodeId": "node-llm::002",
      "targetNodeId": "node-end::003"
    }
  ]
}
```

#### 2.2.3 节点类型定义
| 节点类型 | ID 格式 | 说明 |
|----------|----------|------|
| 开始节点 | `node-start::xxx` | 工作流起点，接收用户输入 |
| LLM 节点 | `node-llm::xxx` | 调用大模型 |
| 插件节点 | `node-plugin::xxx` | 调用外部工具/插件 |
| 条件节点 | `node-condition::xxx` | 条件分支 |
| 循环节点 | `node-loop::xxx` | 循环执行 |
| 结束节点 | `node-end::xxx` | 工作流终点 |

#### 2.2.4 DAG 执行逻辑
1. **解析阶段**：JSON → WorkflowDSL → 构建节点依赖链
2. **校验阶段**：环路检测（Kahn 算法）、节点类型校验、执行器存在性校验
3. **执行阶段**：基于节点状态的深度优先执行
   - 前置节点全部执行完毕 → 执行当前节点
   - 支持正常分支和异常分支
   - 支持节点重试（RetryConfig）

### 2.3 RAG 功能改造

#### 2.3.1 改造原因
- PaiCLI 原生 RAG 功能基于 SQLite 向量存储，适用于 CLI 场景
- Zong 平台使用 PaiSmart (services/knowledge) 提供专业的 RAG 服务（文档解析、向量搜索、权限过滤）
- 采用微服务架构，agent 和 knowledge 独立部署

#### 2.3.2 改造方案
- **删除**：PaiCLI 原生 RAG 代码（`CodeRetriever`、`VectorStore` 等）
- **新增**：`KnowledgeRagTool` 通过 HTTP API 调用 services/knowledge

#### 2.3.3 API 调用规范
- **端点**：`POST http://knowledge-service/api/knowledge/retrieve`
- **请求格式**：
  ```json
  {
    "query": "搜索查询",
    "topK": 5,
    "orgTag": "组织标签（权限过滤）"
  }
  ```
- **响应格式**：
  ```json
  {
    "code": 0,
    "data": {
      "results": [
        {
          "documentId": "doc-001",
          "content": "文档片段内容",
          "score": 0.95,
          "metadata": {...}
        }
      ]
    }
  }
  ```

### 2.4 SSE 流式输出

#### 2.4.1 功能描述
适配 Web 环境，替代 PaiCLI 的 CLI 渲染层（JLine/Lanterna），通过 SSE 实现流式输出。

#### 2.4.2 接口定义
- **端点**：`POST /api/agent/chat/stream`
- **请求参数**：
  ```json
  {
    "sessionId": "会话ID",
    "message": "用户输入",
    "workflowJson": "可选，DAG JSON 格式的工作流定义"
  }
  ```
- **SSE 事件格式**：
  ```
  event: thinking_delta
  data: {"delta": "思考内容片段"}
  
  event: content_delta
  data: {"delta": "回复内容片段"}
  
  event: done
  data: {"status": "completed"}
  ```

#### 2.4.3 StreamListener 接口适配
```java
public interface StreamListener {
    void onThinkingDelta(String delta);  // → SSE event: thinking_delta
    void onContentDelta(String delta);    // → SSE event: content_delta
    void onComplete();                   // → SSE event: done
    void onError(String error);           // → SSE event: error
}
```

---

## 3. 非功能需求

### 3.1 性能要求
- Token 预算管理：防止 LLM 死循环
- 并行执行：工具级并行（最多 4 并发）、任务级并行（DAG 批次）
- 上下文压缩：防止上下文窗口超限

### 3.2 可靠性要求
- 节点重试机制（RetryConfig）
- 异常分支处理（正常分支 vs 异常分支）
- 环路检测（防止 DAG 出现环）

### 3.3 可扩展性要求
- 工具系统：支持自定义工具插件
- 节点类型：支持扩展新的节点类型
- LLM 提供商：支持多模型适配（通义千问、DeepSeek、ChatGPT 等）

---

## 4. 技术选型

| 技术 | 选型 | 说明 |
|------|------|------|
| **核心框架** | Spring Boot 3.x | 微服务框架 |
| **LLM 调用** | Spring AI Alibaba | 通义千问为主，支持多模型 |
| **DAG 解析** | Jackson | JSON 解析 |
| **DAG 执行** | 参考 PaiFlow | 自定义 DAG 执行引擎 |
| **HTTP 调用** | Spring WebClient | 调用 services/knowledge |
| **SSE 输出** | Spring WebFlux | 流式输出 |
| **Token 管理** | PaiCLI AgentBudget | Token 预算管理 |

---

## 5. 架构设计

### 5.1 系统架构
```
┌─────────────────────────────────────────────────────────────┐
│                    frontend (React SPA)                     │
│  工作流编辑器 │ Agent 对话界面 │ 知识库管理                 │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/REST
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    gateway (API 网关)                        │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│  community    │   │    agent      │   │   knowledge    │
│  (文章社区)    │   │  (Agent编排)  │   │  (RAG服务)    │
└───────────────┘   └───────┬───────┘   └───────┬───────┘
                            │                   │
                            └───────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
            ┌──────────┐    ┌──────────┐    ┌──────────┐
            │  MySQL   │    │   ES     │    │  MinIO   │
            │ (元数据) │    │(向量搜索)│    │ (文件)   │
            └──────────┘    └──────────┘    └──────────┘
```

### 5.2 模块划分
```
services/agent/
├── core/
│   ├── llm/                      # LLM 调用层
│   │   ├── LlmClient.java              # 来自 PaiCLI（接口）
│   │   └── SpringAiLlmClient.java      # Spring AI Alibaba 实现
│   ├── agent/                    # Agent 执行层
│   │   ├── Agent.java                  # ReAct 单代理循环
│   │   ├── PlanExecuteAgent.java      # Plan-and-Execute (DAG)
│   │   ├── AgentOrchestrator.java     # Multi-Agent 编排
│   │   └── AgentBudget.java           # Token 预算管理
│   ├── memory/                   # 记忆系统
│   │   ├── MemoryManager.java          # 记忆门面
│   │   ├── ConversationMemory.java    # 短期记忆
│   │   ├── LongTermMemory.java        # 长期记忆
│   │   └── ContextCompressor.java     # 上下文压缩
│   └── workflow/                 # DAG 工作流
│       ├── WorkflowDSL.java            # 来自 PaiFlow（DAG 定义）
│       ├── Node.java                   # 来自 PaiFlow（节点定义）
│       ├── Edge.java                   # 来自 PaiFlow（边定义）
│       ├── WorkflowEngine.java        # DAG 执行引擎
│       └── VariablePool.java           # 变量池
├── tool/                          # 工具系统
│   ├── ToolRegistry.java               # 工具注册表
│   └── plugins/                       # 工具插件
│       ├── KnowledgeRagTool.java      # RAG 检索工具（调用 knowledge API）
│       ├── FileTool.java              # 文件操作工具
│       └── McpTool.java              # MCP 工具
├── api/                           # API 接口层
│   ├── AgentController.java           # REST API
│   └── AgentSseController.java       # SSE 流式输出
└── config/                        # 配置层
    └── WebClientConfig.java           # WebClient 配置（调用 knowledge）
```

---

## 6. 依赖关系

### 6.1 服务间依赖
- `services/agent` → `services/knowledge`：通过 HTTP API 调用 RAG 检索

### 6.2 外部依赖
- Spring AI Alibaba：LLM 调用
- Jackson：JSON 解析
- Spring WebFlux：SSE 流式输出
- Spring WebClient：HTTP 调用

---

## 7. 实施计划

### 7.1 阶段划分
1. **阶段1**：搭建 services/agent 基础框架，移植 PaiCLI 核心代码
2. **阶段2**：实现 DAG JSON 解析功能（参考 PaiFlow）
3. **阶段3**：改造 RAG 功能（调用 services/knowledge HTTP API）
4. **阶段4**：实现 SSE 流式输出和 REST API 接口

### 7.2 优先级
- 先完成 DAG JSON 解析功能，再改造 RAG（用户明确要求）

---

## 8. 风险与问题

### 8.1 技术风险
- PaiFlow 的 Java 工作流引擎是开发版，功能不完善（已有文档记录）
- DAG 执行引擎需要参考 PaiFlow 思路，但可能需要大量定制

### 8.2 架构风险
- 微服务间 HTTP 调用增加网络开销
- services/agent 和 services/knowledge 需要协同部署和测试

---

## 9. 验收标准

### 9.1 功能验收
- [ ] ReAct Agent 执行路径可用
- [ ] Plan-and-Execute 执行路径可用
- [ ] Multi-Agent 编排可用
- [ ] DAG JSON 解析功能可用（前端传入 JSON → 解析为 DAG 图 → 执行）
- [ ] RAG 功能可用（通过 HTTP API 调用 services/knowledge）
- [ ] SSE 流式输出可用

### 9.2 性能验收
- [ ] Token 预算管理生效
- [ ] 并行执行正常
- [ ] 上下文压缩正常

### 9.3 代码质量验收
- [ ] 代码符合 Java 17 规范
- [ ] 单元测试覆盖率 > 60%
- [ ] 集成测试通过

---

## 10. 附录

### 10.1 参考资料
- PaiCLI 项目分析：`docs/旧项目分析/PaiCLI-analysis.md`
- PaiFlow 项目分析：`docs/旧项目分析/PaiFlow-analysis.md`
- PaiSmart 复用决策：`docs/复用决策/PaiSmart-复用决策.md`
- PaiCLI 复用决策：`docs/复用决策/PaiCLI-复用决策.md`

### 10.2 相关文档
- 架构规划：`docs/架构规划/zong-架构规划.md`
- 总览：`docs/复用决策/总览.md`

---

*本文档需经用户审核确认后，方可进入实施阶段。*
