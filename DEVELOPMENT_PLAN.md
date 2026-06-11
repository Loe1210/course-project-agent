# 课设通 Agent 助手开发计划与进度记录

本文档是课设通 Agent 助手的开发台账，用于记录模块开发顺序、分支策略、每个模块的执行状态、检查结果、GitHub 推送与合并情况。

后续开发必须持续维护本文档，避免上下文丢失。

## 1. 开发总原则

1. 所有开发都在 `课设通Agent助手` 文件夹中进行。
2. 严格按功能模块创建分支。
3. 每个模块开发前必须先汇报计划，等待用户确认后再执行。
4. 每个模块使用 PER 流程推进：
   - `P - Planner`：开工前说明目标、改动范围、分支名、验收标准。
   - `E - Executor`：用户确认后进行开发。
   - `R - Replanner`：开发后检查、总结、修正并汇报。
5. 每个模块开发完成后必须检查。
6. 每个模块完成后必须提交到对应 GitHub 分支。
7. 用户确认后再合并到主分支。
8. 合并主分支后再进入下一个模块。
9. 每完成一步都要更新本文档。

## 2. Git 分支规范

主分支：

```text
main
```

功能分支命名：

```text
feature/<module-name>
chore/<task-name>
release/<version-name>
```

提交信息建议：

```text
feat: add course chat agent
feat: add course project PAR agent
chore: initialize project scaffold
docs: update development progress
test: add module smoke checks
fix: repair course project stream response
```

## 3. 模块开发顺序

| 顺序 | 模块 | 分支 | 状态 |
|---|---|---|---|
| 0 | 仓库与工程基线模块 | `chore/repo-baseline` | 开发中 |
| 1 | 项目脚手架与配置模块 | `feature/project-scaffold` | 未开始 |
| 2 | RAG 知识库模块 | `feature/rag-knowledge-base` | 未开始 |
| 3 | Tool 工具模块 | `feature/course-tools` | 未开始 |
| 4 | ReAct 课设对话 Agent 模块 | `feature/course-chat-agent` | 未开始 |
| 5 | PAR 课设任务 Agent 模块 | `feature/course-project-agent` | 未开始 |
| 6 | 课设产物生成模块 | `feature/artifact-generation` | 未开始 |
| 7 | 前端课设工作台模块 | `feature/course-workbench-ui` | 未开始 |
| 8 | 联调与验收模块 | `feature/integration-acceptance` | 未开始 |
| 9 | 文档与发布模块 | `release/course-project-agent-v1` | 未开始 |

状态取值：

```text
未开始 / 计划中 / 开发中 / 检查中 / 待推送 / 已推送 / 待合并 / 已合并 / 阻塞
```

## 4. 模块详情

### 0. 仓库与工程基线模块

分支：

```text
chore/repo-baseline
```

目标：

确认课设通 Agent 助手项目目录、Git 仓库、GitHub remote、主分支、构建方式和基础工程状态。

计划内容：

1. 进入 `课设通Agent助手` 文件夹。
2. 查看目录结构。
3. 检查是否已经 `git init`。
4. 检查远程 GitHub 仓库地址。
5. 检查当前分支和主分支名称。
6. 检查是否已有项目文件。
7. 输出基线检查报告。

验收标准：

1. 明确项目目录是否为空。
2. 明确 Git 仓库是否已初始化。
3. 明确 GitHub remote 是否存在。
4. 明确主分支名称。
5. 明确后续功能分支创建策略。

当前状态：

```text
开发中
```

检查结果：

```text
检查时间：2026-06-11
项目目录：C:\Users\liuju\Desktop\SuperBizAgent-release-2026-05-17\课设通Agent助手
目录状态：当前只有 DEVELOPMENT_PLAN.md，尚无项目源码
Git 状态：未初始化 Git 仓库
父目录 Git 状态：父目录也不是 Git 仓库
当前分支：无
GitHub remote：无
是否可创建 chore/repo-baseline 分支：暂不可创建，需要先 git init 并配置主分支
阻塞原因：缺少 Git 仓库和 GitHub remote，无法执行企业式分支开发、推送和合并流程
```

下一步待确认：

```text
1. 是否在 课设通Agent助手 目录执行 git init
2. 主分支是否使用 main
3. GitHub 仓库地址是什么
4. 是否需要我配置 remote origin
```

初始化进展：

```text
更新时间：2026-06-11
已执行：git init
主分支：已从 master 改为 main
本地提交身份：已配置为当前仓库局部配置 Codex <codex@example.local>
GitHub remote：仍未配置，等待用户提供仓库地址
```

### 1. 项目脚手架与配置模块

分支：

```text
feature/project-scaffold
```

目标：

创建课设通 Agent 助手基础 Spring Boot 工程骨架。

计划内容：

1. 建立 Maven 项目结构。
2. 配置 `pom.xml`。
3. 创建启动类。
4. 创建基础配置文件。
5. 创建基础包结构。
6. 创建基础 README。
7. 确保项目可编译或可启动到基础状态。

验收标准：

1. 项目结构清晰。
2. Maven 编译通过。
3. Spring Boot 启动入口存在。
4. 基础配置文件存在。

当前状态：

```text
未开始
```

### 2. RAG 知识库模块

分支：

```text
feature/rag-knowledge-base
```

目标：

实现课设知识库的上传、分片、向量化、Milvus 存储和检索能力。

计划内容：

1. 创建 `course-project-docs` 目录。
2. 准备课程设计模板、评分标准、优秀案例等文档。
3. 实现或迁移文件上传接口。
4. 实现或迁移文档分片服务。
5. 实现或迁移 DashScope Embedding 服务。
6. 实现或迁移 Milvus 索引服务。
7. 实现或迁移向量检索服务。
8. 实现 `queryCourseProjectKnowledge` 知识库工具。

验收标准：

1. 支持上传 `.txt` 和 `.md` 文档。
2. 上传后自动入向量库。
3. 可通过查询文本检索相关课设资料。
4. 检索结果包含内容、来源和相似度。

当前状态：

```text
未开始
```

### 3. Tool 工具模块

分支：

```text
feature/course-tools
```

目标：

实现课设通 Agent 可调用的本地工具集。

计划内容：

1. `TopicRecommendTools`：推荐课设题目。
2. `DatabaseDesignTools`：生成数据库设计。
3. `ApiDesignTools`：生成接口文档。
4. `ProjectTemplateTools`：生成项目目录结构。
5. `ReportTools`：生成报告大纲。
6. `DefenseTools`：生成答辩问题。
7. 保留 `DateTimeTools`。

验收标准：

1. 工具可被 Spring AI Agent 识别。
2. 工具输出结构化 Markdown 或 JSON。
3. 工具失败时返回明确错误。
4. 核心工具有简单 smoke check。

当前状态：

```text
未开始
```

### 4. ReAct 课设对话 Agent 模块

分支：

```text
feature/course-chat-agent
```

目标：

实现课设自由问答 Agent，支持普通对话、流式对话、上下文和工具调用。

计划内容：

1. 创建 `CourseChatService`。
2. 创建 `CourseChatController`。
3. 创建 `CourseChatRequest` / `CourseChatResponse`。
4. 创建课设对话系统 Prompt。
5. 集成 RAG 工具和本地工具。
6. 支持 `/api/course_chat`。
7. 支持 `/api/course_chat_stream`。
8. 支持会话历史。

验收标准：

1. 能回答软件课设相关问题。
2. 能基于知识库回答模板或评分标准问题。
3. 能流式输出。
4. 能保持多轮上下文。

当前状态：

```text
未开始
```

### 5. PAR 课设任务 Agent 模块

分支：

```text
feature/course-project-agent
```

目标：

实现多 Agent 编排的课设任务生成流程。

第一版 Agent 结构：

```text
course_project_supervisor
  ├── planner_agent
  └── executor_agent
```

增强版 Agent 结构：

```text
course_project_supervisor
  ├── topic_planner_agent
  ├── system_designer_agent
  ├── artifact_generator_agent
  └── review_defense_agent
```

计划内容：

1. 创建 `CourseProjectAgentService`。
2. 创建 `CourseProjectController`。
3. 创建 `CourseProjectRequest`。
4. 实现 SupervisorAgent。
5. 实现 Planner Agent。
6. 实现 Executor Agent。
7. 编写 PAR Prompt。
8. 支持完整课设方案生成。
9. 支持 `/api/course_project`。
10. 支持 `/api/course_project_stream`。

验收标准：

1. 输入课设题目后能自动拆解任务。
2. 能调用 RAG 和工具。
3. 能生成完整课设方案。
4. 输出包括需求、架构、数据库、接口、代码结构、测试和答辩。

当前状态：

```text
未开始
```

### 6. 课设产物生成模块

分支：

```text
feature/artifact-generation
```

目标：

支持按类型生成指定课设产物，并支持完整性审查。

计划内容：

1. 实现 `/api/course_project/artifact`。
2. 实现 `/api/course_project/review`。
3. 支持数据库设计产物。
4. 支持接口文档产物。
5. 支持代码骨架产物。
6. 支持报告大纲和正文初稿。
7. 支持测试用例。
8. 支持答辩问答。

验收标准：

1. 可以单独生成数据库设计。
2. 可以单独生成接口文档。
3. 可以单独生成报告大纲。
4. 可以输出完整性审查结果。

当前状态：

```text
未开始
```

### 7. 前端课设工作台模块

分支：

```text
feature/course-workbench-ui
```

目标：

将聊天页面升级为课设通工作台。

计划内容：

1. 修改页面标题和品牌为“课设通 Agent 助手”。
2. 保留聊天区。
3. 增加课设模式选择。
4. 增加快捷按钮。
5. 增加右侧产物面板。
6. 支持 Markdown 渲染和代码高亮。
7. 对接课设对话接口。
8. 对接课设任务接口。

验收标准：

1. 页面可打开。
2. 可以发送课设问题。
3. 可以点击生成完整方案。
4. 可以展示流式结果。
5. 可以展示结构化产物。

当前状态：

```text
未开始
```

### 8. 联调与验收模块

分支：

```text
feature/integration-acceptance
```

目标：

端到端跑通课设通 Agent 助手第一版演示流程。

计划内容：

1. 启动 Milvus。
2. 启动 Spring Boot 服务。
3. 上传课设知识库文档。
4. 测试普通课设问答。
5. 测试流式课设问答。
6. 测试完整课设方案生成。
7. 测试校园二手交易平台演示案例。
8. 修复联调问题。

验收标准：

1. 完整演示流程可跑通。
2. 核心接口返回正常。
3. 前端可展示结果。
4. 演示案例稳定。

当前状态：

```text
未开始
```

### 9. 文档与发布模块

分支：

```text
release/course-project-agent-v1
```

目标：

整理可提交、可答辩、可运行的第一版项目。

计划内容：

1. 完善 README。
2. 编写启动说明。
3. 编写接口说明。
4. 编写演示流程。
5. 编写项目亮点说明。
6. 整理课设报告素材。
7. 打 release 分支。

验收标准：

1. 新用户可按 README 启动项目。
2. 文档能说明系统架构和核心功能。
3. 演示流程清晰。
4. 主分支代码干净。

当前状态：

```text
未开始
```

## 5. 每模块检查清单

每个模块完成后必须检查：

```text
1. 代码是否编译通过
2. 关键接口是否可用
3. 是否符合需求文档
4. 是否更新 DEVELOPMENT_PLAN.md
5. 是否提交 commit
6. 是否推送到对应 GitHub 分支
7. 是否等待用户确认合并
8. 合并后是否推送主分支
```

## 6. 进度日志

| 时间 | 模块 | 分支 | 动作 | 结果 |
|---|---|---|---|---|
| 2026-06-11 | 开发计划 | 未创建分支 | 创建开发计划与进度记录文档 | 已完成 |
| 2026-06-11 | 仓库与工程基线模块 | `chore/repo-baseline` | 检查项目目录、Git 仓库、分支和 remote | 阻塞：项目尚未初始化 Git 仓库，未配置 GitHub remote |
| 2026-06-11 | 仓库与工程基线模块 | `main` | 初始化本地 Git 仓库，设置主分支和本地提交身份 | 已完成，remote 待配置 |

## 7. 当前下一步

建议下一步执行：

```text
第 0 步：仓库与工程基线模块
分支：chore/repo-baseline
```

执行前需要用户确认。
