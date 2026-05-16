# PaiCLI 移植与改造任务清单

> 创建日期：2026-05-16
> 创建人：[待填写]
> 状态：待审核
> 关联需求：`docs/需求分析/2026-05-16-paicli-migration-requirements.md`

---

## 1. 任务总览

| 任务编号 | 任务名称 | 优先级 | 状态 | 依赖 | 预计工时 |
|----------|----------|--------|------|------|----------|
| T001 | 搭建 services/agent 基础框架 | P0 | 待开始 | 无 | 4h |
| T002 | 移植 LlmClient 接口及 Spring AI 实现 | P0 | 待开始 | T001 | 2h |
| T003 | 移植三条执行路径（ReAct/Plan/Multi-Agent） | P0 | 待开始 | T002 | 8h |
| T004 | 移植 MemoryManager 系统 | P0 | 待开始 | T001 | 4h |
| T005 | 移植 ToolRegistry 工具系统 | P0 | 待开始 | T001 | 3h |
| T006 | 移植 AgentBudget Token 预算管理 | P0 | 待开始 | T001 | 1h |
| T007 | 移植 MCP 客户端 | P1 | 待开始 | T001 | 3h |
| T008 | 实现 DAG JSON 解析功能（正向：JSON→DAG） | P0 | 待开始 | T003 | 6h |
| **T008-2** | **实现 DAG 图转 JSON 功能（反向：DAG→JSON）** | **P0** | **待开始** | **T003** | **6h** |
| T009 | 实现 DAG 执行引擎 | P0 | 待开始 | T008, T008-2 | 8h |
| T010 | 删除 PaiCLI 原生 RAG 代码 | P0 | 待开始 | T001 | 1h |
| T011 | 新增 KnowledgeRagTool 调用 knowledge API | P0 | 待开始 | T010 | 4h |
| T012 | 实现 SSE 流式输出（含 workflow_json 事件） | P0 | 待开始 | T003, T009 | 5h |
| T013 | 实现 REST API 接口 | P0 | 待开始 | T012 | 3h |
| T014 | 集成测试 | P0 | 待开始 | T013, T011 | 4h |

---

## 2. 详细任务描述

### 阶段1：搭建基础框架（T001-T007）

#### T001：搭建 services/agent 基础框架
- **任务描述**：创建 `services/agent` 模块的基础结构，配置 Spring Boot 3.x + Spring AI Alibaba 依赖
- **交付物**：
  - `pom.xml`（Maven 配置）
  - `application.yml`（应用配置）
  - 项目目录结构（`core/`, `tool/`, `api/`, `config/`）
- **验收标准**：
  - [ ] 项目能正常启动
  - [ ] Spring AI Alibaba 依赖配置正确
  - [ ] 目录结构符合架构设计

#### T002：移植 LlmClient 接口及 Spring AI 实现
- **任务描述**：移植 PaiCLI 的 `LlmClient` 接口，并实现 `SpringAiLlmClient`（基于 Spring AI Alibaba）
- **来源**：`PaiCLI/llm/LlmClient.java`, `PaiCLI/llm/GLMClient.java` 等
- **交付物**：
  - `core/llm/LlmClient.java`（接口）
  - `core/llm/SpringAiLlmClient.java`（实现）
  - `core/llm/ChatResponse.java`（响应封装）
- **验收标准**：
  - [ ] `LlmClient` 接口定义完整（chat、maxContextWindow 等方法）
  - [ ] `SpringAiLlmClient` 能正常调用通义千问模型
  - [ ] 支持流式输出（StreamListener）

#### T003：移植三条执行路径
- **任务描述**：移植 PaiCLI 的三条 Agent 执行路径
- **来源**：
  - `PaiCLI/agent/Agent.java`（ReAct）
  - `PaiCLI/agent/PlanExecuteAgent.java`（Plan+DAG）
  - `PaiCLI/agent/AgentOrchestrator.java`（Multi-Agent）
- **交付物**：
  - `core/agent/Agent.java`
  - `core/agent/PlanExecuteAgent.java`
  - `core/agent/AgentOrchestrator.java`
  - `core/agent/AgentRole.java`（角色枚举）
- **验收标准**：
  - [ ] ReAct 路径：单代理循环正常
  - [ ] Plan 路径：任务拆解 + DAG 执行正常
  - [ ] Multi-Agent 路径：Planner + Worker + Reviewer 协作正常

#### T004：移植 MemoryManager 系统
- **任务描述**：移植 PaiCLI 的记忆系统（短期 + 长期 + 压缩）
- **来源**：`PaiCLI/memory/MemoryManager.java` 等
- **交付物**：
  - `core/memory/MemoryManager.java`
  - `core/memory/ConversationMemory.java`（短期记忆）
  - `core/memory/LongTermMemory.java`（长期记忆，对接 knowledge）
  - `core/memory/ContextCompressor.java`（上下文压缩）
- **验收标准**：
  - [ ] 短期记忆：当前会话消息列表正常
  - [ ] 长期记忆：对接 `services/knowledge`（后续实现）
  - [ ] 上下文压缩：防止窗口超限

#### T005：移植 ToolRegistry 工具系统
- **任务描述**：移植 PaiCLI 的工具注册与执行系统
- **来源**：`PaiCLI/tool/ToolRegistry.java`
- **交付物**：
  - `tool/ToolRegistry.java`
  - `tool/ToolDefinition.java`
  - `tool/ToolExecutor.java`
  - `tool/ToolInvocation.java`
- **验收标准**：
  - [ ] 工具注册正常
  - [ ] 工具并行执行正常
  - [ ] 超时控制正常

#### T006：移植 AgentBudget Token 预算管理
- **任务描述**：移植 PaiCLI 的 Token 预算管理机制
- **来源**：`PaiCLI/agent/AgentBudget.java`
- **交付物**：
  - `core/agent/AgentBudget.java`
- **验收标准**：
  - [ ] 动态计算 budget（80% * maxContextWindow）
  - [ ] 触发压缩（75% * budget）
  - [ ] 硬轮数限制防死循环

#### T007：移植 MCP 客户端
- **任务描述**：移植 PaiCLI 的 MCP 协议客户端（stdio + HTTP 传输层）
- **来源**：`PaiCLI/mcp/McpClient.java` 等
- **交付物**：
  - `tool/plugins/McpClient.java`
  - `tool/plugins/McpServerManager.java`
  - `tool/plugins/transport/StdioTransport.java`
  - `tool/plugins/transport/HttpTransport.java`
- **验收标准**：
  - [ ] stdio 传输层正常（启动子进程）
  - [ ] HTTP 传输层正常（REST 调用）
  - [ ] 工具动态发现正常

---

### 阶段2：实现 DAG JSON 解析功能（T008, T008-2, T009）

#### T008：实现 DAG JSON 解析功能（正向：JSON→DAG）
- **任务描述**：参考 PaiFlow 的 DAG JSON 格式，实现 JSON → WorkflowDSL 解析
- **参考**：`PaiFlow/core-workflow-java/.../WorkflowDSL.java`, `Node.java`, `Edge.java`
- **交付物**：
  - `core/workflow/WorkflowDSL.java`（工作流定义）
  - `core/workflow/Node.java`（节点定义）
  - `core/workflow/Edge.java`（边定义）
  - `core/workflow/NodeData.java`（节点数据）
  - `core/workflow/NodeTypeEnum.java`（节点类型枚举）
- **验收标准**：
  - [ ] JSON 能正确反序列化为 WorkflowDSL
  - [ ] Node、Edge 对象正确解析
  - [ ] 支持所有节点类型（start/llm/plugin/condition/loop/end）

#### T008-2：实现 DAG 图转 JSON 功能（反向：DAG→JSON，新增）
- **任务描述**：将 PaiCLI AI 自动生成的执行流程（DAG 图）转换为标准 JSON 格式，通过 SSE 推送给前端
- **背景**：
  - PaiCLI 现有两种 DAG 执行模式（Plan-and-Execute、Multi-Agent）
  - 用户痛点：AI 生成的执行流程无法直观展示，难以手动修改
  - 需要新增功能：DAG 图 → JSON → 前端可视化展示 → 用户修改 → 再执行
- **交付物**：
  - `core/workflow/DagToJsonConverter.java`（DAG 图转 JSON 转换器）
  - 修改 `core/agent/PlanExecuteAgent.java`（添加 DAG→JSON 转换调用）
  - 修改 `core/agent/AgentOrchestrator.java`（添加 DAG→JSON 转换调用）
  - 修改 `api/AgentSseController.java`（添加 `workflow_json` SSE 事件）
- **功能要点**：
  - 读取 PlanExecuteAgent 生成的执行计划（DAG 图）
  - 读取 AgentOrchestrator 的多 Agent 编排流程
  - 转换为标准 JSON 格式（兼容 PaiFlow 格式）
  - 通过 SSE 推送 JSON（`event: workflow_json`）
- **验收标准**：
  - [ ] Plan-and-Execute 模式：AI 生成的计划能正确转换为 JSON
  - [ ] Multi-Agent 模式：多 Agent 协作流程能正确转换为 JSON
  - [ ] JSON 格式兼容 PaiFlow（能被前端正确解析和展示）
  - [ ] SSE 推送正常（`event: workflow_json`）
  - [ ] 前端能正确展示 JSON 生成的 DAG 图

#### T009：实现 DAG 执行引擎
- **任务描述**：实现基于节点依赖的 DAG 执行引擎
- **依赖**：T008（JSON→DAG）、T008-2（DAG→JSON）
- **参考**：`PaiFlow/core-workflow-java/.../WorkflowEngine.java`
- **交付物**：
  - `core/workflow/WorkflowEngine.java`（执行引擎）
  - `core/workflow/VariablePool.java`（变量池）
  - `core/workflow/NodeExecutor.java`（节点执行器接口）
  - `core/workflow/impl/StartNodeExecutor.java`
  - `core/workflow/impl/LlmNodeExecutor.java`
  - `core/workflow/impl/PluginNodeExecutor.java`
  - `core/workflow/impl/EndNodeExecutor.java`
  - 修改 `core/agent/PlanExecuteAgent.java`（集成 DAG 执行引擎）
  - 修改 `core/agent/AgentOrchestrator.java`（集成 DAG 执行引擎）
- **验收标准**：
  - [ ] 构建节点依赖链正常（buildNodeExecuteChain）
  - [ ] 环路检测正常（Kahn 算法）
  - [ ] 节点按依赖顺序执行
  - [ ] 支持正常分支和异常分支
  - [ ] 支持节点重试（RetryConfig）
  - [ ] 正向流程（JSON→DAG→执行）正常工作
  - [ ] 反向流程（AI生成→DAG→JSON→展示）正常工作

---

### 阶段3：改造 RAG 功能（T010-T011）

#### T010：删除 PaiCLI 原生 RAG 代码
- **任务描述**：删除 PaiCLI 原生的 RAG 相关代码（不适用微服务架构）
- **删除内容**：
  - `PaiCLI/rag/VectorStore.java`
  - `PaiCLI/rag/CodeIndex.java`
  - `PaiCLI/rag/CodeRetriever.java`
- **验收标准**：
  - [ ] 相关代码已删除
  - [ ] 编译通过
  - [ ] 功能正常（不涉及 RAG 的部分）

#### T011：新增 KnowledgeRagTool 调用 knowledge API
- **任务描述**：新增 `KnowledgeRagTool`，通过 HTTP API 调用 `services/knowledge` 的 RAG 检索
- **交付物**：
  - `tool/plugins/KnowledgeRagTool.java`
  - `config/WebClientConfig.java`（WebClient 配置）
  - `dto/KnowledgeRetrieveRequest.java`（请求 DTO）
  - `dto/KnowledgeRetrieveResponse.java`（响应 DTO）
- **API 规范**：
  - 端点：`POST http://knowledge-service/api/knowledge/retrieve`
  - 请求：`{ "query": "...", "topK": 5, "orgTag": "..." }`
  - 响应：`{ "code": 0, "data": { "results": [...] } }`
- **验收标准**：
  - [ ] HTTP API 调用正常
  - [ ] 返回结果正确解析
  - [ ] 异常处理正常（网络超时、服务不可用等）

---

### 阶段4：实现 SSE 和 REST API（T012-T013）

#### T012：实现 SSE 流式输出（含 workflow_json 事件）
- **任务描述**：实现 SSE 流式输出，适配 Web 环境（替代 PaiCLI 的 CLI 渲染层），并支持推送 DAG 图 JSON
- **交付物**：
  - `api/AgentSseController.java`（SSE 控制器）
  - `core/llm/StreamListener.java`（流式监听接口）
  - `core/llm/SseStreamListener.java`（SSE 实现）
  - 修改 `core/agent/Agent.java`（集成 SSE 输出）
  - 修改 `core/agent/PlanExecuteAgent.java`（集成 SSE 输出 + workflow_json 事件）
  - 修改 `core/agent/AgentOrchestrator.java`（集成 SSE 输出 + workflow_json 事件）
- **接口规范**：
  - 端点：`POST /api/agent/chat/stream`
  - 请求：`{ "sessionId": "...", "message": "...", "workflowJson": "..." }`
  - SSE 事件：
    - `thinking_delta`：思考内容片段
    - `content_delta`：回复内容片段
    - `workflow_json`：**DAG 图 JSON（新增）**
    - `done`：完成
    - `error`：错误
- **验收标准**：
  - [ ] SSE 连接正常建立
  - [ ] 流式输出正常（thinking + content 分区）
  - [ ] `workflow_json` 事件正常推送（AI 生成流程后）
  - [ ] 连接自动关闭（完成或超时）

#### T013：实现 REST API 接口
- **任务描述**：实现 REST API 接口，提供 Agent 服务的 HTTP 入口
- **交付物**：
  - `api/AgentController.java`（REST 控制器）
  - `dto/AgentChatRequest.java`（请求 DTO）
  - `dto/AgentChatResponse.java`（响应 DTO）
  - `dto/WorkflowExecuteRequest.java`（工作流执行请求）
- **接口列表**：
  | 接口路径 | 方法 | 功能 |
  |---------|------|------|
  | `/api/agent/chat` | POST | 非流式对话 |
  | `/api/agent/chat/stream` | POST | 流式对话（SSE） |
  | `/api/agent/workflow/execute` | POST | 执行工作流 |
  | `/api/agent/workflow/status/{id}` | GET | 查询执行状态 |
- **验收标准**：
  - [ ] 所有接口正常响应
  - [ ] 参数校验正常
  - [ ] 异常处理正常（全局异常处理器）

---

### 阶段5：集成测试（T014）

#### T014：集成测试
- **任务描述**：进行完整的集成测试，确保各模块协同工作
- **测试场景**：
  1. ReAct 单代理循环 + RAG 检索
  2. Plan-and-Execute + DAG 执行
  3. Multi-Agent 编排 + SSE 流式输出
  4. 前端传入 DAG JSON → 解析 → 执行 → SSE 输出结果
- **交付物**：
  - 测试用例文档
  - 测试脚本（Postman/ curl）
  - 测试报告
- **验收标准**：
  - [ ] 所有测试场景通过
  - [ ] 性能满足要求（Token 预算、并行执行、上下文压缩）
  - [ ] 无明显 Bug

---

## 3. 任务依赖关系

```
T001 (基础框架)
  ├─ T002 (LlmClient)
  │    └─ T003 (三条执行路径)
  │         ├─ T008 (DAG JSON 解析：JSON→DAG)
  │         ├─ T008-2 (DAG 图转 JSON：DAG→JSON)  【新增】
  │         │    └─ T009 (DAG 执行引擎)
  │         └─ T012 (SSE 流式输出)
  │              └─ T013 (REST API)
  ├─ T004 (MemoryManager)
  ├─ T005 (ToolRegistry)
  ├─ T006 (AgentBudget)
  ├─ T007 (MCP 客户端)
  └─ T010 (删除原生 RAG)
       └─ T011 (KnowledgeRagTool)
            └─ T014 (集成测试)

关键路径：
1. 基础框架 → LlmClient → 三条执行路径 → DAG 功能 → SSE → REST API
2. DAG 功能包含正向（T008）和反向（T008-2）两个转换
3. 反向转换（T008-2）是新增功能，实现 AI 生成流程 → JSON → 前端展示
```

---

## 4. 工时估算

| 阶段 | 任务数 | 总工时 | 负责人 | 预计完成日期 |
|------|--------|--------|--------|--------------|
| 阶段1：基础框架 | 7 | 25h | [待分配] | [待填写] |
| 阶段2：DAG 功能 | 3 | 20h | [待分配] | [待填写] |
| 阶段3：RAG 改造 | 2 | 5h | [待分配] | [待填写] |
| 阶段4：SSE/REST | 2 | 8h | [待分配] | [待填写] |
| 阶段5：集成测试 | 1 | 4h | [待分配] | [待填写] |
| **总计** | **15** | **62h** | - | - |

**工时变化说明**：
- 新增任务 T008-2（DAG 图转 JSON）：+6h
- T012（SSE 输出）工时调整：4h → 5h（添加 workflow_json 事件）
- **总工时**：55h → 62h

---

## 5. 风险与问题

### 5.1 技术风险
- **风险1**：PaiFlow 的 Java 工作流引擎是开发版，功能不完善
  - **应对措施**：参考思路，自主实现 DAG 执行引擎
- **风险2**：DAG 执行引擎复杂度高，可能需要大量调试
  - **应对措施**：先做最小可用版本，迭代完善

### 5.2 架构风险
- **风险1**：微服务间 HTTP 调用增加网络开销
  - **应对措施**：使用 WebClient 异步调用，设置合理超时
- **风险2**：services/agent 和 services/knowledge 需要协同部署和测试
  - **应对措施**：先 Mock knowledge API，后集成测试

### 5.3 进度风险
- **风险1**：任务量大（55h），可能延期
  - **应对措施**：优先级排序，先完成核心功能（T001-T009）

---

## 6. 验收标准汇总

### 6.1 功能验收
- [ ] ReAct Agent 执行路径可用
- [ ] Plan-and-Execute 执行路径可用
- [ ] Multi-Agent 编排可用
- [ ] DAG JSON 解析功能可用（前端传入 JSON → 解析为 DAG 图 → 执行）
- [ ] **DAG 图转 JSON 功能可用**（AI 生成流程 → 转换为 JSON → 推送前端）
- [ ] RAG 功能可用（通过 HTTP API 调用 services/knowledge）
- [ ] SSE 流式输出可用（含 `workflow_json` 事件）
- [ ] REST API 接口可用
- [ ] **完整闭环可用**：AI 生成流程 → 转 JSON 可视化展示 → 用户修改 → 解析执行

### 6.2 性能验收
- [ ] Token 预算管理生效
- [ ] 并行执行正常
- [ ] 上下文压缩正常

### 6.3 代码质量验收
- [ ] 代码符合 Java 17 规范
- [ ] 单元测试覆盖率 > 60%
- [ ] 集成测试通过

---

## 7. 附录

### 7.1 参考资料
- PaiCLI 项目分析：`docs/旧项目分析/PaiCLI-analysis.md`
- PaiFlow 项目分析：`docs/旧项目分析/PaiFlow-analysis.md`
- PaiSmart 复用决策：`docs/复用决策/PaiSmart-复用决策.md`
- PaiCLI 复用决策：`docs/复用决策/PaiCLI-复用决策.md`

### 7.2 相关文档
- 需求分析：`docs/需求分析/2026-05-16-paicli-migration-requirements.md`
- 变更记录：`docs/变更记录/2026-05-16-paicli-migration-changelog.md`（实施过程中逐步记录）

---

## 8. 实施注意事项

### 8.1 代码移植原则

**重要：移植代码时要减去原先别人的印记**

- ❌ 移除原作者的姓名、标记、版权声明等个人信息
- ❌ 修改包名（从原项目包名改为 `com.zong.agent`）
- ❌ 移除原项目的特定标识、Logo、水印等
- ✅ 保留代码注释和文档（匿名化处理）
- ✅ 保留开源协议声明（如适用）

### 8.2 变更记录方式

- **边写代码边记录**：每完成一个任务的代码修改，立即在变更记录文档中记录
- **记录内容**：文件路径、变更类型（新增/修改/删除）、变更说明、影响范围
- **记录格式**：参考标准变更记录格式

---

*本文档需经用户审核确认后，方可进入实施阶段。*
