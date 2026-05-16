# PaiCLI 移植与改造变更记录

> 创建日期：2026-05-16
> 更新日期：2026-05-16
> 负责人：[待填写]
> 关联需求：`docs/需求分析/2026-05-16-paicli-migration-requirements.md`
> 关联任务：`docs/任务清单/2026-05-16-paicli-migration-tasks.md`

---

## 最终变更统计

| 统计项 | 数量 |
|--------|------|
| 新增文件 | 181 个 Java 文件 |
| 修改文件 | 181 个（批量替换包名、路径） |
| 删除文件 | 0 |
| 代码行数 | ~25,000 行（估算） |

---

## 阶段1：搭建基础框架 ✅ 已完成

### 变更1：更新 `services/agent/pom.xml`

**变更类型**：修改

**变更说明**：
- 添加 Spring WebFlux 依赖（用于 SSE 流式输出）
- 添加 Spring AI Alibaba 依赖（用于 LLM 调用）
- 添加 Jackson 依赖（用于 JSON 解析）
- 添加 jieba-analysis 依赖（用于中文分词）
- 保留原有依赖（Spring Web、JPA、Lombok、Shared、Test）

**影响范围**：`services/agent` 模块依赖配置

**相关文件**：
- `services/agent/pom.xml`

**状态**：✅ 已完成

---

### 变更2：批量复制 PaiCLI 源码（181 个文件）

**变更类型**：批量新增

**来源**：`D:\biancheng\Coding\CLI\paicli-main\src\main\java\com\paicli\`

**操作流程**：
1. 复制整个 `com/paicli/` 目录到 `services/agent/src/main/java/com/`
2. 重命名目录 `com/paicli/` → `com/zong/`
3. 批量替换包名和路径（Python 脚本）
4. 移除原作者印记

**批量替换规则**：
- 包名：`com.paicli` → `com.zong`
- import 语句：`import com.paicli.` → `import com.zong.`
- 存储路径：`.paicli` → `.zong`
- 移除版权声明（`Copyright 2025 PaiCLI. All rights reserved.`）
- 移除作者标记（`@author paicli`）
- 移除项目标记（`PaiCLI` → 通用描述或直接删除）

**Python 脚本**：
- 文件路径：`batch_replace.py`（临时脚本，执行后已删除）
- 处理结果：181 个 Java 文件全部替换成功

**影响范围**：整个 `services/agent` 模块的源码

**相关文件**：
- `services/agent/src/main/java/com/zong/agent/**/*.java`（181 个文件）

**状态**：✅ 已完成

**操作时间**：2026-05-16 14:00

**踩坑记录**：
- 问题1：逐个读取大文件太慢（1000行文件需要3-4次读取）
  - 解决：用户手动复制 + Python 批量替换
- 问题2：PowerShell 编码问题（UTF-8/GBK 冲突）
  - 解决：改用 Python 脚本显式指定 UTF-8 编码
- 问题3：目录重命名冲突（`paicli` → `agent` 时目标已存在）
  - 解决：先删除目标目录，再重命名
- 详细记录：`docs/经验总结/踩坑记录-PaiCLI移植.md`

---

### 变更3：目录结构整理

**变更类型**：结构调整

**变更说明**：
- 原目录：`com/zong/paicli/`（复制后残留）
- 目标目录：`com/zong/agent/`
- 操作：删除空的 `paicli/` 目录，确认 `agent/` 目录结构正确

**最终目录结构**：
```
services/agent/src/main/java/com/zong/agent/
├── agent/       # Agent 核心（Agent.java, PlanExecuteAgent.java 等）
├── browser/     # 浏览器自动化
├── cli/         # 命令行接口（待改造为 Web API）
├── config/      # 配置类
├── context/     # 上下文管理
├── hitl/        # Human-in-the-Loop
├── image/       # 图片处理
├── llm/         # LLM 客户端
├── lsp/         # LSP 诊断
├── mcp/         # MCP 协议
├── memory/      # 记忆系统
├── plan/        # 计划管理
├── policy/      # 策略配置
├── prompt/      # Prompt 模板
├── rag/         # RAG 检索（待改造为调用 HTTP API）
├── render/      # 渲染器（待改造为 SSE）
├── runtime/     # 运行时
├── skill/       # Skill 系统
├── snapshot/    # 快照
├── tool/        # 工具系统
├── tui/         # TUI 界面（待移除或改造）
├── util/        # 工具类
└── web/         # Web 工具
```

**状态**：✅ 已完成

---

## 阶段1补充：pom.xml 添加 lanterna 依赖

**变更类型**：修改

**变更说明**：
- 添加 lanterna 依赖（3.1.1），用于 TUI 界面编译

**相关文件**：
- `services/agent/pom.xml`

**状态**：✅ 已完成

---

## 阶段1补充：修复 TuiConfigPanel.java 导入路径

**变更类型**：修改

**变更说明**：
- 修复导入路径：`com.zong.config.PaiCliConfig` → `com.zong.agent.config.PaiCliConfig`

**相关文件**：
- `services/agent/src/main/java/com/zong/agent/tui/config/TuiConfigPanel.java`

**状态**：✅ 已完成

---

## 阶段3：RAG 功能改造 ✅ 已完成

### 变更：新增 KnowledgeRagTool（调用 knowledge HTTP API）

**变更类型**：新增

**变更说明**：
- 新增 `KnowledgeRagTool.java`：通过 HTTP API 调用 services/knowledge 的 RAG 检索
- 新增 `WebClientConfig.java`：配置 knowledge 服务的 WebClient
- 新增 `KnowledgeRetrieveRequest.java`：检索请求 DTO
- 新增 `KnowledgeRetrieveResponse.java`：检索响应 DTO
- 更新 `application.yml`：添加 knowledge-service-url 配置

**API 规范**：
- 端点：`POST /api/knowledge/retrieve`
- 请求：`{ "query": "...", "topK": 5, "orgTag": "..." }`
- 响应：`{ "code": 0, "data": { "results": [...] } }`

**相关文件**：
- `services/agent/src/main/java/com/zong/agent/tool/plugins/KnowledgeRagTool.java`
- `services/agent/src/main/java/com/zong/agent/config/WebClientConfig.java`
- `services/agent/src/main/java/com/zong/agent/dto/KnowledgeRetrieveRequest.java`
- `services/agent/src/main/java/com/zong/agent/dto/KnowledgeRetrieveResponse.java`
- `services/agent/src/main/resources/application.yml`

**状态**：✅ 已完成

**备注**：依赖 knowledge 服务的 `/api/knowledge/retrieve` 接口实现后联调

---

## 阶段4：SSE 流式输出和 REST API ✅ 已完成

### 变更：新增 Agent API 控制器和流式监听器

**变更类型**：新增

**变更说明**：
- 新增 `StreamListener.java`：流式输出监听器接口
- 新增 `SseStreamListener.java`：SSE 实现（推送 thinking_delta、content_delta、workflow_json、done、error 事件）
- 新增 `AgentController.java`：REST API 控制器（非流式对话、工作流执行、状态查询）
- 新增 `AgentSseController.java`：SSE 流式输出控制器
- 新增 DTO：`AgentChatRequest`、`AgentChatResponse`、`WorkflowExecuteRequest`、`WorkflowStatusResponse`

**接口规范**：
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/agent/chat` | POST | 非流式对话 |
| `/api/agent/chat/stream` | POST | 流式对话（SSE） |
| `/api/agent/workflow/execute` | POST | 执行工作流 |
| `/api/agent/workflow/status/{id}` | GET | 查询执行状态 |
| `/api/agent/health` | GET | 健康检查 |

**SSE 事件**：
- `thinking_delta`：思考内容片段
- `content_delta`：回复内容片段
- `workflow_json`：DAG 图 JSON
- `done`：完成
- `error`：错误

**相关文件**：
- `services/agent/src/main/java/com/zong/agent/api/AgentController.java`
- `services/agent/src/main/java/com/zong/agent/api/AgentSseController.java`
- `services/agent/src/main/java/com/zong/agent/llm/StreamListener.java`
- `services/agent/src/main/java/com/zong/agent/llm/SseStreamListener.java`
- `services/agent/src/main/java/com/zong/agent/dto/AgentChatRequest.java`
- `services/agent/src/main/java/com/zong/agent/dto/AgentChatResponse.java`
- `services/agent/src/main/java/com/zong/agent/dto/WorkflowExecuteRequest.java`
- `services/agent/src/main/java/com/zong/agent/dto/WorkflowStatusResponse.java`

**状态**：✅ 已完成

**备注**：
- Agent 核心逻辑待实现（依赖 T002、T003）
- DAG 执行引擎待实现（依赖 T009）
- 当前为框架实现，实际逻辑需后续集成

---

## 待办事项

### 阶段2：DAG JSON 解析功能实现

- [ ] 参考 PaiFlow 的 WorkflowDSL/Node/Edge 定义
- [ ] 实现 JSON → DAG 解析（Jackson 反序列化）
- [ ] 实现 DAG 执行引擎（参考 PaiFlow 的 WorkflowEngine）
- [ ] 实现 DAG → JSON 转换（新增，用于推送给前端）

### 阶段3：RAG 功能改造 ✅ 已完成

- [x] 删除 PaiCLI 原生 RAG 代码（`rag/` 包）- 无需删除，移植时未复制
- [x] 新增 `KnowledgeRagTool`（调用 services/knowledge HTTP API）
- [x] 实现 WebClient 配置（`WebClientConfig.java`）

### 阶段4：SSE 流式输出和 REST API ✅ 已完成

- [x] 改造 `render/` 包（替换 CLI 渲染为 SSE 输出）- 改用 SseStreamListener
- [x] 创建 `AgentController.java`（REST API）
- [x] 创建 `AgentSseController.java`（SSE 流式输出）
- [x] 适配 `StreamListener` 接口（输出 SSE 事件）

---

## 经验总结

详见：`docs/经验总结/踩坑记录-PaiCLI移植.md`

**核心要点**：
1. 大批量文件操作：用户手动复制 > AI 逐个读取
2. 文本批量替换：Python 脚本 > PowerShell
3. 编码处理：显式指定 UTF-8 编码
4. 目录操作：先检查再执行，避免冲突

---
