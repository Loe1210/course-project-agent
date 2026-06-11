# 课设通 Agent 助手

课设通 Agent 助手是一个面向大学生软件课程设计的智能协作平台，目标是帮助学生完成从选题、需求分析、系统设计、数据库设计、接口设计、代码骨架、课设报告到答辩准备的全过程。

## 核心架构

系统规划采用双 Agent 模式：

1. ReAct 课设对话 Agent：负责自由问答、局部解释、即时工具调用。
2. PAR 课设任务 Agent：负责完整课设任务的规划、执行、再规划和产物生成。

底层能力包括：

- Spring Boot
- Spring AI Alibaba
- DashScope
- RAG 知识库
- Milvus 向量数据库
- 本地 Tool Calling
- MCP 扩展工具
- SSE 流式输出

## 当前状态

当前处于项目脚手架阶段，后续会按 `DEVELOPMENT_PLAN.md` 中的模块顺序逐步开发。

## 本地运行

```bash
mvn spring-boot:run
```

默认服务地址：

```text
http://localhost:9900
```

健康检查：

```text
GET /api/health
```
