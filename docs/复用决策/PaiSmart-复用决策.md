# PaiSmart 复用决策记录

## 基本信息

| 项目 | 定位 | 分析日期 |
|------|------|----------|
| PaiSmart | 开发者社区平台（RAG + 权限 + 认证） | 2026-05-15 |

---

## 复用决策总览

| 模块 | 决策 | 说明 |
|------|------|------|
| OrgTag 权限系统 | ✅ 直接复用 | 多租户隔离，迁移到 `services/knowledge` |
| 混合搜索 | ✅ 直接复用 | ES 向量 + 关键词混合 |
| 文档解析 | ✅ 直接复用 | Tika + HanLP 流水线 |
| 登录/JWT 认证 | ✅ 直接复用 | 迁移到 `gateway/` |
| 异步处理 | ⚠️ 调整后复用 | RocketMQ 替代 Kafka |
| 向量引擎 | ✅ 直接复用 | Elasticsearch |
| MinIO 封装 | ✅ 直接复用 | 文件存储 |
| 聊天通信 | ⚠️ 调整后复用 | SSE 替代 WebSocket |
| Vue 前端 | ❌ 不复用 | zong 使用 React |
| Spring Data JPA | ❌ 替换 | 换用 MyBatis-Plus |

---

## 详细决策

### ✅ 直接复用

#### 1. OrgTag 权限系统

```
PaiSmart 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
OrgTagAuthorizationFilter                 OrgTagPermissionFilter
(基于组织标签的权限过滤)                   (直接迁移)

OrgTagCacheService                       OrgTagCacheService
(组织标签缓存)                             (直接迁移)

OrganizationTag                          OrganizationTag
(组织标签实体)                             (迁移实体)
```

**复用理由：**
- 多租户隔离是知识库的核心需求
- OrgTag 已验证，可以直接迁移
- 确保用户只能访问有权限的文档

**迁移目标：** `services/knowledge/permission/`

---

#### 2. 混合搜索（HybridSearch）

```
PaiSmart 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
HybridSearchService                      HybridSearchService
(向量 + 关键词混合搜索)                    (直接迁移)

ElasticsearchService                     ElasticsearchService
(ES 客户端封装)                           (直接迁移)

DocumentVector                           DocumentVector
(向量索引实体)                            (迁移实体)
```

**复用理由：**
- ES 向量搜索 + 关键词搜索的混合策略
- zong 使用相同技术栈（ES），迁移成本低
- 搜索质量好，已在 PaiSmart 验证

---

#### 3. 文档解析流水线

```
PaiSmart 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
ParseService                             ParseService
(文档解析)                                (直接迁移)

VectorizationService                      VectorizationService
(向量化)                                 (直接迁移)

Tika + HanLP                            Tika + HanLP
(文档解析 + 分词)                         (直接迁移)
```

**复用理由：**
- Tika 支持多格式文档（PDF/Word/PPT 等）
- HanLP 中文分词效果好
- 流水线已验证，可直接迁移

---

#### 4. JWT 登录认证

```
PaiSmart 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
JwtUtils                                 JwtUtils
(Token 生成与验证)                        (直接迁移)

TokenCacheService                        TokenCacheService
(Token 缓存)                             (迁移到 Redis)

UserController                           UserController
(用户管理)                               (迁移)
```

**复用理由：**
- JWT 认证逻辑成熟，已验证
- 迁移到 `gateway/` 层统一管理
- 其他服务通过 `gateway/` 鉴权

---

#### 5. MinIO 文件存储

```
PaiSmart 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
MinioTemplate                            MinioTemplate
(MinIO 操作封装)                         (直接复用)
```

**复用理由：**
- zong 也使用 MinIO，技术一致
- 直接复用，减少重复代码

---

### ⚠️ 调整后复用

#### 1. 异步处理（RocketMQ 替代 Kafka）

```
PaiSmart 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
Kafka                                   RocketMQ
(异步文件处理)                            (替代 Kafka)

@KafkaListener                           @RocketMQListener
```

**调整理由：**
- 用户倾向使用 RocketMQ（有经验）
- Kafka 在 PaiSmart 有错误处理问题
- RocketMQ 在国内生态更好

**改进点：**
- 完善错误处理和重试机制
- 消息持久化保证

---

#### 2. 聊天通信（SSE 替代 WebSocket）

```
PaiSmart 原设计                          zong 实现
─────────────────────────────────────────────────────────────────
WebSocket                                SSE
(实时聊天)                                (Agent 流式输出)

ChatController                           AgentSseController
(WebSocket 端点)                         (SSE 端点)
```

**调整理由：**
- Agent 核心场景是 LLM 流式输出
- SSE 更简单，后端压力小
- WebSocket 对 agent 场景过度设计

---

### ❌ 不复用

| 模块 | 原因 |
|------|------|
| Vue 前端 | zong 使用 React + TypeScript |
| Spring Data JPA | 换用 MyBatis-Plus |

---

## 迁移后的 zong knowledge 架构

```
services/knowledge/
├── core/
│   ├── permission/
│   │   ├── OrgTagPermissionFilter.java  # 来自 PaiSmart
│   │   ├── OrgTagCacheService.java     # 来自 PaiSmart
│   │   └── OrganizationTag.java         # 来自 PaiSmart
│   ├── search/
│   │   ├── HybridSearchService.java     # 来自 PaiSmart
│   │   └── ElasticsearchService.java    # 来自 PaiSmart
│   ├── parse/
│   │   ├── ParseService.java            # 来自 PaiSmart
│   │   └── VectorizationService.java    # 来自 PaiSmart
│   └── storage/
│       └── MinioTemplate.java           # 来自 PaiSmart
├── async/
│   └── RocketMQProcessor.java           # 新增（RocketMQ 替代 Kafka）
└── api/
    ├── KnowledgeController.java        # 新增（REST API）
    └── SseController.java              # 新增（SSE 流式）
```

---

## 技术栈汇总

| 技术 | 来源 | 说明 |
|------|------|------|
| Elasticsearch | PaiSmart | 向量搜索 + 全文搜索 |
| RocketMQ | 新选型 | 异步消息处理 |
| Tika + HanLP | PaiSmart | 文档解析 + 中文分词 |
| MinIO | PaiSmart | 对象存储 |
| JWT | PaiSmart | 认证授权 |
| MyBatis-Plus | zong 自选 | 数据访问层 |

---

## 决策人 & 日期

- 决策人：[待填写]
- 决策日期：2026-05-15
