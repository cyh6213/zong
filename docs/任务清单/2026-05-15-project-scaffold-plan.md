# Zong 项目目录结构初始化 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按照 Spec 创建 zong 项目的完整目录结构和脚手架代码

**Architecture:** Monorepo 扁平结构，3 个 Spring Boot 微服务 + 1 个 Spring Cloud Gateway + 1 个 React 前端 + 共享模块 + 部署配置

**Tech Stack:** React + TypeScript + Vite + Ant Design + Zustand | Java 17 + Spring Boot 3.x + Spring Cloud Gateway + Spring AI Alibaba | Docker + docker-compose

---

## File Structure

| 操作 | 路径 | 职责 |
|------|------|------|
| Create | `.gitignore` | Git 忽略规则 |
| Create | `README.md` | 项目简介 |
| Create | `docs/需求分析/.gitkeep` | 文档目录占位 |
| Create | `docs/变更记录/.gitkeep` | 文档目录占位 |
| Create | `docs/任务清单/.gitkeep` | 文档目录占位 |
| Create | `docs/经验总结/可复用流程/.gitkeep` | 文档目录占位 |
| Create | `docs/经验总结/架构决策/.gitkeep` | 文档目录占位 |
| Create | `docs/经验总结/踩坑记录/.gitkeep` | 文档目录占位 |
| Scaffold | `frontend/` | React + Vite 前端脚手架 |
| Create | `services/community/pom.xml` | 社区服务 Maven 配置 |
| Create | `services/community/src/main/java/com/zong/community/CommunityApplication.java` | 启动类 |
| Create | `services/community/src/main/resources/application.yml` | 配置文件 |
| Create | `services/community/src/test/java/com/zong/community/CommunityApplicationTest.java` | 测试类 |
| Create | `services/community/Dockerfile` | 容器构建 |
| Create | `services/knowledge/pom.xml` | 知识库服务 Maven 配置 |
| Create | `services/knowledge/src/main/java/com/zong/knowledge/KnowledgeApplication.java` | 启动类 |
| Create | `services/knowledge/src/main/java/com/zong/knowledge/service/rag/` | RAG 目录占位 |
| Create | `services/knowledge/src/main/resources/application.yml` | 配置文件 |
| Create | `services/knowledge/src/test/java/com/zong/knowledge/KnowledgeApplicationTest.java` | 测试类 |
| Create | `services/knowledge/Dockerfile` | 容器构建 |
| Create | `services/agent/pom.xml` | Agent 服务 Maven 配置 |
| Create | `services/agent/src/main/java/com/zong/agent/AgentApplication.java` | 启动类 |
| Create | `services/agent/src/main/java/com/zong/agent/service/orchestration/` | 编排目录占位 |
| Create | `services/agent/src/main/java/com/zong/agent/service/agent/` | Agent 目录占位 |
| Create | `services/agent/src/main/java/com/zong/agent/service/rag/` | 轻量 RAG 目录占位 |
| Create | `services/agent/src/main/resources/application.yml` | 配置文件 |
| Create | `services/agent/src/test/java/com/zong/agent/AgentApplicationTest.java` | 测试类 |
| Create | `services/agent/Dockerfile` | 容器构建 |
| Create | `gateway/pom.xml` | 网关 Maven 配置 |
| Create | `gateway/src/main/java/com/zong/gateway/GatewayApplication.java` | 启动类 |
| Create | `gateway/src/main/java/com/zong/gateway/filter/` | 过滤器目录占位 |
| Create | `gateway/src/main/java/com/zong/gateway/route/` | 路由目录占位 |
| Create | `gateway/src/main/java/com/zong/gateway/orchestration/` | 编排目录占位 |
| Create | `gateway/src/main/resources/application.yml` | 网关路由配置 |
| Create | `gateway/Dockerfile` | 容器构建 |
| Create | `shared/pom.xml` | 共享模块 Maven 配置 |
| Create | `shared/src/main/java/com/zong/shared/response/Result.java` | 统一响应格式 |
| Create | `shared/src/main/java/com/zong/shared/exception/BusinessException.java` | 业务异常 |
| Create | `shared/src/main/java/com/zong/shared/exception/GlobalExceptionHandler.java` | 全局异常处理 |
| Create | `shared/src/main/java/com/zong/shared/constants/ServiceConstants.java` | 服务常量 |
| Create | `deploy/docker-compose.yml` | 本地开发编排 |
| Create | `deploy/nginx/default.conf` | 前端反向代理 |
| Create | `deploy/scripts/start-dev.sh` | 开发启动脚本 |

---

### Task 1: 创建 .gitignore

**Files:**
- Create: `.gitignore`

- [ ] **Step 1: 创建 .gitignore 文件**

```
# Java
*.class
*.jar
*.war
target/
!.mvn/wrapper/maven-wrapper.jar

# Node
node_modules/
dist/
.env.local
.env.*.local

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS
.DS_Store
Thumbs.db

# Logs
*.log
logs/

# 不入库的参考资源
agent-skills/

# 构建产物
build/
out/
```

- [ ] **Step 2: 提交**

```bash
git add .gitignore
git commit -m "chore: add .gitignore"
```

---

### Task 2: 创建 docs/ 中文文档目录

**Files:**
- Create: `docs/需求分析/.gitkeep`
- Create: `docs/变更记录/.gitkeep`
- Create: `docs/任务清单/.gitkeep`
- Create: `docs/经验总结/可复用流程/.gitkeep`
- Create: `docs/经验总结/架构决策/.gitkeep`
- Create: `docs/经验总结/踩坑记录/.gitkeep`

- [ ] **Step 1: 创建目录结构**

```bash
mkdir -p docs/需求分析
mkdir -p docs/变更记录
mkdir -p docs/任务清单
mkdir -p docs/经验总结/可复用流程
mkdir -p docs/经验总结/架构决策
mkdir -p docs/经验总结/踩坑记录
```

- [ ] **Step 2: 添加 .gitkeep 占位文件**

```bash
touch docs/需求分析/.gitkeep
touch docs/变更记录/.gitkeep
touch docs/任务清单/.gitkeep
touch docs/经验总结/可复用流程/.gitkeep
touch docs/经验总结/架构决策/.gitkeep
touch docs/经验总结/踩坑记录/.gitkeep
```

- [ ] **Step 3: 提交**

```bash
git add docs/
git commit -m "chore: create docs directory structure with Chinese names"
```

---

### Task 3: 创建 README.md

**Files:**
- Create: `README.md`

- [ ] **Step 1: 创建 README.md**

```markdown
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
```

- [ ] **Step 2: 提交**

```bash
git add README.md
git commit -m "docs: add project README"
```

---

### Task 4: 初始化 frontend/ React+Vite 脚手架

**Files:**
- Create: `frontend/` (整个目录由 Vite 生成)

- [ ] **Step 1: 用 Vite 创建 React + TypeScript 项目**

```bash
cd d:/biancheng/Coding/zong
npm create vite@latest frontend -- --template react-ts
```

- [ ] **Step 2: 安装依赖**

```bash
cd frontend
npm install
npm install antd zustand react-router-dom axios
```

- [ ] **Step 3: 创建业务目录结构**

```bash
cd d:/biancheng/Coding/zong/frontend
mkdir -p src/components/Layout
mkdir -p src/pages/Community
mkdir -p src/pages/Knowledge
mkdir -p src/pages/Agent
mkdir -p src/pages/Dashboard
mkdir -p src/services
mkdir -p src/stores
mkdir -p src/hooks
mkdir -p src/types
mkdir -p src/router
```

- [ ] **Step 4: 创建 API 服务层占位文件**

创建 `frontend/src/services/communityApi.ts`：

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_COMMUNITY_API_URL || '/api/community',
});

export default api;
```

创建 `frontend/src/services/knowledgeApi.ts`：

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_KNOWLEDGE_API_URL || '/api/knowledge',
});

export default api;
```

创建 `frontend/src/services/agentApi.ts`：

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_AGENT_API_URL || '/api/agent',
});

export default api;
```

- [ ] **Step 5: 创建 .env 文件**

```
VITE_COMMUNITY_API_URL=/api/community
VITE_KNOWLEDGE_API_URL=/api/knowledge
VITE_AGENT_API_URL=/api/agent
```

- [ ] **Step 6: 验证前端可启动**

```bash
cd d:/biancheng/Coding/zong/frontend
npm run dev
```

Expected: Vite 开发服务器启动，可访问 http://localhost:5173

- [ ] **Step 7: 提交**

```bash
cd d:/biancheng/Coding/zong
git add frontend/
git commit -m "feat: initialize frontend with React + Vite + Ant Design + Zustand"
```

---

### Task 5: 创建 shared/ 共享模块

**Files:**
- Create: `shared/pom.xml`
- Create: `shared/src/main/java/com/zong/shared/response/Result.java`
- Create: `shared/src/main/java/com/zong/shared/exception/BusinessException.java`
- Create: `shared/src/main/java/com/zong/shared/exception/GlobalExceptionHandler.java`
- Create: `shared/src/main/java/com/zong/shared/constants/ServiceConstants.java`

- [ ] **Step 1: 创建 shared/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.zong</groupId>
    <artifactId>shared</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>shared</name>
    <description>Zong shared utilities</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: 创建 Result.java**

```java
package com.zong.shared.response;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
```

- [ ] **Step 3: 创建 BusinessException.java**

```java
package com.zong.shared.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 4: 创建 GlobalExceptionHandler.java**

```java
package com.zong.shared.exception;

import com.zong.shared.response.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error(500, "Internal server error");
    }
}
```

- [ ] **Step 5: 创建 ServiceConstants.java**

```java
package com.zong.shared.constants;

public final class ServiceConstants {
    private ServiceConstants() {}

    public static final String COMMUNITY_SERVICE = "community";
    public static final String KNOWLEDGE_SERVICE = "knowledge";
    public static final String AGENT_SERVICE = "agent";
}
```

- [ ] **Step 6: 创建目录结构并提交**

```bash
cd d:/biancheng/Coding/zong
mkdir -p shared/src/main/java/com/zong/shared/response
mkdir -p shared/src/main/java/com/zong/shared/exception
mkdir -p shared/src/main/java/com/zong/shared/constants
```

然后写入上述文件，提交：

```bash
git add shared/
git commit -m "feat: create shared module with Result, BusinessException, GlobalExceptionHandler, ServiceConstants"
```

---

### Task 6: 创建 services/community 服务骨架

**Files:**
- Create: `services/community/pom.xml`
- Create: `services/community/src/main/java/com/zong/community/CommunityApplication.java`
- Create: `services/community/src/main/resources/application.yml`
- Create: `services/community/src/test/java/com/zong/community/CommunityApplicationTest.java`
- Create: `services/community/Dockerfile`

- [ ] **Step 1: 创建目录结构**

```bash
cd d:/biancheng/Coding/zong
mkdir -p services/community/src/main/java/com/zong/community/controller
mkdir -p services/community/src/main/java/com/zong/community/service
mkdir -p services/community/src/main/java/com/zong/community/repository
mkdir -p services/community/src/main/java/com/zong/community/model/entity
mkdir -p services/community/src/main/java/com/zong/community/model/dto
mkdir -p services/community/src/main/java/com/zong/community/model/vo
mkdir -p services/community/src/main/java/com/zong/community/config
mkdir -p services/community/src/main/resources
mkdir -p services/community/src/test/java/com/zong/community
```

- [ ] **Step 2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.zong</groupId>
    <artifactId>community</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>community</name>
    <description>Developer community service</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.zong</groupId>
            <artifactId>shared</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 创建 CommunityApplication.java**

```java
package com.zong.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8081

spring:
  application:
    name: community
```

- [ ] **Step 5: 创建 CommunityApplicationTest.java**

```java
package com.zong.community;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTest {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 6: 创建 Dockerfile**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/community-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 7: 提交**

```bash
cd d:/biancheng/Coding/zong
git add services/community/
git commit -m "feat: create community service skeleton"
```

---

### Task 7: 创建 services/knowledge 服务骨架

**Files:**
- Create: `services/knowledge/pom.xml`
- Create: `services/knowledge/src/main/java/com/zong/knowledge/KnowledgeApplication.java`
- Create: `services/knowledge/src/main/java/com/zong/knowledge/service/rag/` (占位)
- Create: `services/knowledge/src/main/resources/application.yml`
- Create: `services/knowledge/src/test/java/com/zong/knowledge/KnowledgeApplicationTest.java`
- Create: `services/knowledge/Dockerfile`

- [ ] **Step 1: 创建目录结构**

```bash
cd d:/biancheng/Coding/zong
mkdir -p services/knowledge/src/main/java/com/zong/knowledge/controller
mkdir -p services/knowledge/src/main/java/com/zong/knowledge/service/rag
mkdir -p services/knowledge/src/main/java/com/zong/knowledge/repository
mkdir -p services/knowledge/src/main/java/com/zong/knowledge/model/entity
mkdir -p services/knowledge/src/main/java/com/zong/knowledge/model/dto
mkdir -p services/knowledge/src/main/java/com/zong/knowledge/model/vo
mkdir -p services/knowledge/src/main/java/com/zong/knowledge/config
mkdir -p services/knowledge/src/main/resources
mkdir -p services/knowledge/src/test/java/com/zong/knowledge
```

- [ ] **Step 2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.zong</groupId>
    <artifactId>knowledge</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>knowledge</name>
    <description>Knowledge base service with full RAG and permission filtering</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter</artifactId>
            <version>1.0.0-M2</version>
        </dependency>
        <dependency>
            <groupId>com.zong</groupId>
            <artifactId>shared</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
        </repository>
    </repositories>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 创建 KnowledgeApplication.java**

```java
package com.zong.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KnowledgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8082

spring:
  application:
    name: knowledge
```

- [ ] **Step 5: 创建 KnowledgeApplicationTest.java**

```java
package com.zong.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KnowledgeApplicationTest {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 6: 创建 Dockerfile**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/knowledge-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 7: 提交**

```bash
cd d:/bianching/Coding/zong
git add services/knowledge/
git commit -m "feat: create knowledge service skeleton with RAG support"
```

---

### Task 8: 创建 services/agent 服务骨架

**Files:**
- Create: `services/agent/pom.xml`
- Create: `services/agent/src/main/java/com/zong/agent/AgentApplication.java`
- Create: `services/agent/src/main/java/com/zong/agent/service/orchestration/` (占位)
- Create: `services/agent/src/main/java/com/zong/agent/service/agent/` (占位)
- Create: `services/agent/src/main/java/com/zong/agent/service/rag/` (占位)
- Create: `services/agent/src/main/resources/application.yml`
- Create: `services/agent/src/test/java/com/zong/agent/AgentApplicationTest.java`
- Create: `services/agent/Dockerfile`

- [ ] **Step 1: 创建目录结构**

```bash
cd d:/bianching/Coding/zong
mkdir -p services/agent/src/main/java/com/zong/agent/controller
mkdir -p services/agent/src/main/java/com/zong/agent/service/orchestration
mkdir -p services/agent/src/main/java/com/zong/agent/service/agent
mkdir -p services/agent/src/main/java/com/zong/agent/service/rag
mkdir -p services/agent/src/main/java/com/zong/agent/repository
mkdir -p services/agent/src/main/java/com/zong/agent/model/entity
mkdir -p services/agent/src/main/java/com/zong/agent/model/dto
mkdir -p services/agent/src/main/java/com/zong/agent/model/vo
mkdir -p services/agent/src/main/java/com/zong/agent/config
mkdir -p services/agent/src/main/resources
mkdir -p services/agent/src/test/java/com/zong/agent
```

- [ ] **Step 2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.zong</groupId>
    <artifactId>agent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>agent</name>
    <description>Multi-agent orchestration service with lightweight RAG</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.zong</groupId>
            <artifactId>shared</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 创建 AgentApplication.java**

```java
package com.zong.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8083

spring:
  application:
    name: agent
```

- [ ] **Step 5: 创建 AgentApplicationTest.java**

```java
package com.zong.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AgentApplicationTest {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 6: 创建 Dockerfile**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/agent-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 7: 提交**

```bash
cd d:/bianching/Coding/zong
git add services/agent/
git commit -m "feat: create agent service skeleton with orchestration and RAG"
```

---

### Task 9: 创建 gateway/ 网关骨架

**Files:**
- Create: `gateway/pom.xml`
- Create: `gateway/src/main/java/com/zong/gateway/GatewayApplication.java`
- Create: `gateway/src/main/java/com/zong/gateway/filter/` (占位)
- Create: `gateway/src/main/java/com/zong/gateway/route/` (占位)
- Create: `gateway/src/main/java/com/zong/gateway/orchestration/` (占位)
- Create: `gateway/src/main/resources/application.yml`
- Create: `gateway/Dockerfile`

- [ ] **Step 1: 创建目录结构**

```bash
cd d:/bianching/Coding/zong
mkdir -p gateway/src/main/java/com/zong/gateway/filter
mkdir -p gateway/src/main/java/com/zong/gateway/route
mkdir -p gateway/src/main/java/com/zong/gateway/orchestration
mkdir -p gateway/src/main/resources
```

- [ ] **Step 2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.zong</groupId>
    <artifactId>gateway</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>gateway</name>
    <description>API Gateway with scene orchestration</description>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.1</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway-mvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 创建 GatewayApplication.java**

```java
package com.zong.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway
  cloud:
    gateway:
      routes:
        - id: community
          uri: http://localhost:8081
          predicates:
            - Path=/api/community/**
          filters:
            - StripPrefix=2
        - id: knowledge
          uri: http://localhost:8082
          predicates:
            - Path=/api/knowledge/**
          filters:
            - StripPrefix=2
        - id: agent
          uri: http://localhost:8083
          predicates:
            - Path=/api/agent/**
          filters:
            - StripPrefix=2
```

- [ ] **Step 5: 创建 Dockerfile**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 6: 提交**

```bash
cd d:/bianching/Coding/zong
git add gateway/
git commit -m "feat: create gateway skeleton with route configuration"
```

---

### Task 10: 创建 deploy/ 部署配置

**Files:**
- Create: `deploy/docker-compose.yml`
- Create: `deploy/nginx/default.conf`
- Create: `deploy/scripts/start-dev.sh`

- [ ] **Step 1: 创建目录结构**

```bash
cd d:/bianching/Coding/zong
mkdir -p deploy/nginx
mkdir -p deploy/scripts
```

- [ ] **Step 2: 创建 docker-compose.yml**

```yaml
version: '3.8'

services:
  frontend:
    build:
      context: ../frontend
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - gateway

  gateway:
    build:
      context: ../gateway
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    depends_on:
      - community
      - knowledge
      - agent

  community:
    build:
      context: ../services/community
      dockerfile: Dockerfile
    ports:
      - "8081:8081"

  knowledge:
    build:
      context: ../services/knowledge
      dockerfile: Dockerfile
    ports:
      - "8082:8082"

  agent:
    build:
      context: ../services/agent
      dockerfile: Dockerfile
    ports:
      - "8083:8083"
```

- [ ] **Step 3: 创建 nginx/default.conf**

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://gateway:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

- [ ] **Step 4: 创建 scripts/start-dev.sh**

```bash
#!/bin/bash
set -e

echo "Starting Zong development environment..."
docker-compose -f docker-compose.yml up --build -d

echo ""
echo "Services running:"
echo "  Frontend:  http://localhost:3000"
echo "  Gateway:   http://localhost:8080"
echo "  Community: http://localhost:8081"
echo "  Knowledge: http://localhost:8082"
echo "  Agent:     http://localhost:8083"
```

- [ ] **Step 5: 提交**

```bash
cd d:/bianching/Coding/zong
git add deploy/
git commit -m "feat: create deploy configuration with docker-compose and nginx"
```

---

### Task 11: 最终验证和提交

- [ ] **Step 1: 验证目录结构完整**

```bash
cd d:/bianching/Coding/zong
find . -not -path './agent-skills/*' -not -path './.git/*' | sort
```

Expected: 应看到所有 spec 中定义的目录和文件

- [ ] **Step 2: 验证 git 状态**

```bash
cd d:/bianching/Coding/zong
git log --oneline
```

Expected: 应看到所有 task 的提交记录

- [ ] **Step 3: 推送到远程**

```bash
cd d:/bianching/Coding/zong
git push -u origin main
```
