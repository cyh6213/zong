# zhishiku 复用决策

> 讨论日期：2026-05-15
> 
> **讨论完成度**：✅ 统一 AI 调用层 | ✅ 通用组件 | ✅ 文章社区

---

## 1. 统一 AI 调用层

### 1.1 定位

```
┌─────────────────────────────────────────────────────────────────┐
│  zhishiku AI 能力 = zong 统一 AI 调用层的基础                      │
│                                                                  │
│  工厂模式多模型封装 + 流式返回                                     │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 技术方案

| 项目 | 决策 | 说明 |
|------|------|------|
| **主框架** | Spring AI Alibaba | 通义千问为主 |
| **接口定义** | PaiCLI LlmClient | 保留接口规范 |
| **模型支持** | 按需扩展 | ChatGPT、DeepSeek 等 |

### 1.3 架构设计

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

### 1.4 复用决策汇总

| 模块 | 决策 | 说明 |
|------|------|------|
| AI 工厂模式 | ✅ 直接复用 | zhishiku `AiSourceEnum` + 工厂模式 |
| 多模型封装 | ✅ 参考设计 | 流式返回、异常降级逻辑 |
| Spring AI 集成 | ✅ 新增 | 用 Spring AI Alibaba 替代原 direct 调用 |

---

## 2. paicoding-core 通用组件

### 2.1 定位

```
┌─────────────────────────────────────────────────────────────────┐
│  paicoding-core = zong shared/ 模块的基础                        │
│                                                                  │
│  Redis 封装 / 异步框架 / 链路追踪 / 敏感词过滤                    │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 迁移组件清单

| 组件 | 说明 | 迁移位置 |
|------|------|----------|
| Redis 封装 | Pipeline 批处理、缓存穿透防护 | `shared/cache/` |
| 异步执行框架 | 注解式异步、超时控制、兜底逻辑 | `shared/async/` |
| 链路追踪 | AOP + MDC + TraceID 全链路日志 | `shared/trace/` |
| 敏感词过滤 | 敏感词检测与替换 | `shared/util/` |

### 2.3 模块结构

```
shared/
├── cache/
│   ├── RedisClient.java            # 来自 zhishiku
│   └── RedisPipeline.java          # Pipeline 批处理
├── async/
│   ├── AsyncExecute.java           # 注解式异步
│   └── AsyncUtil.java             # 异步工具类
├── trace/
│   ├── MdcAspect.java              # AOP 切面
│   ├── MdcDot.java                 # TraceID 埋点
│   └── TraceContext.java           # 链路上下文
├── util/
│   ├── SensitiveService.java       # 敏感词过滤
│   └── SensitiveWordsLoader.java   # 词库加载
└── config/
    └── DynamicConfigBinder.java    # 动态配置（参考）
```

### 2.4 复用决策汇总

| 模块 | 决策 | 说明 |
|------|------|------|
| Redis 封装 | ✅ 直接迁移 | Pipeline 批处理能力强 |
| 异步执行框架 | ✅ 直接迁移 | 注解式 + 超时控制 |
| 链路追踪 | ✅ 直接迁移 | AOP + MDC 成熟方案 |
| 敏感词过滤 | ✅ 直接迁移 | 词库 + 匹配算法 |
| 动态配置 | ⚠️ 参考设计 | 可后续按需迁移 |

---

## 3. 文章社区功能

### 3.1 定位

```
┌─────────────────────────────────────────────────────────────────┐
│  zhishiku 文章社区 = zong services/community 的核心              │
│                                                                  │
│  文章管理 / 评论互动 / Markdown 处理                              │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 迁移功能清单

| 功能 | 说明 | 迁移位置 |
|------|------|----------|
| 文章管理 | 发布、编辑、删除、搜索 | `services/community/article/` |
| 评论互动 | 评论、嵌套回复、点赞 | `services/community/comment/` |
| Markdown 处理 | 解析、代码高亮、目录生成 | `services/community/markdown/` |
| 用户互动 | 收藏、关注、通知 | `services/community/interaction/` |

### 3.3 模块结构

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
│   ├── FollowService.java          # 关注
│   └── NotifyService.java          # 通知
└── user/
    └── UserService.java            # 用户相关
```

### 3.4 复用决策汇总

| 模块 | 决策 | 说明 |
|------|------|------|
| 文章管理 | ✅ 直接迁移 | CRUD、分类、标签、搜索 |
| 评论互动 | ✅ 直接迁移 | 嵌套回复、点赞 |
| Markdown 处理 | ✅ 直接迁移 | 代码高亮、目录生成 |
| 收藏/关注 | ✅ 直接迁移 | 用户互动逻辑 |
| 通知系统 | ✅ 直接迁移 | 消息通知 |

---

## 4. 不复用部分

| 模块 | 原因 |
|------|------|
| Thymeleaf 前端 | zong 用 React |
| 数据库表结构 | 需重新设计适配微服务 |
| Spring Boot 2.7.1 | zong 用 Spring Boot 3.x |
| 阿里云 OSS 封装 | zong 用 MinIO |

---

## 5. 完整复用决策汇总

| 模块 | 决策 | 迁移目标 |
|------|------|----------|
| **统一 AI 调用层** | ✅ 直接迁移 | `shared/ai/` |
| **Redis 封装** | ✅ 直接迁移 | `shared/cache/` |
| **异步执行框架** | ✅ 直接迁移 | `shared/async/` |
| **链路追踪** | ✅ 直接迁移 | `shared/trace/` |
| **敏感词过滤** | ✅ 直接迁移 | `shared/util/` |
| **文章管理** | ✅ 直接迁移 | `services/community/` |
| **评论互动** | ✅ 直接迁移 | `services/community/` |
| **Markdown 处理** | ✅ 直接迁移 | `services/community/` |
| **通知系统** | ✅ 直接迁移 | `services/community/` |

---

*本文档基于 2026-05-15 的讨论生成。*
