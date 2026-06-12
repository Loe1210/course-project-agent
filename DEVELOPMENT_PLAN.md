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
10. 代码中的提示词、报错、日志、接口返回描述统一使用中文。
11. 课设通 Agent 对外问答统一使用中文。

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
| 0 | 仓库与工程基线模块 | `chore/repo-baseline` | 已合并 |
| 1 | 项目脚手架与配置模块 | `feature/project-scaffold` | 已合并 |
| 2 | RAG 知识库模块 | `feature/rag-knowledge-base` | 已合并 |
| 3 | Tool 工具模块 | `feature/course-tools` | 已合并 |
| 4 | ReAct 课设对话 Agent 模块 | `feature/course-chat-agent` | 已合并 |
| 5 | PAR 课设任务 Agent 模块 | `feature/course-project-agent` | 已合并 |
| 6 | 课设产物生成模块 | `feature/artifact-generation` | 待合并 |
| 7 | 前端课设工作台模块 | `feature/course-workbench-ui` | 待合并 |
| 8 | 文档能力增强与 MCP 模块 | `feature/document-mcp-enhancement` | 计划中 |
| 9 | 联调与验收模块 | `feature/integration-acceptance` | 未开始 |
| 10 | 文档与发布模块 | `release/course-project-agent-v1` | 未开始 |

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
已合并
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
初始提交：已在 main 提交 DEVELOPMENT_PLAN.md
基线分支：已创建 chore/repo-baseline
GitHub remote：已配置为 https://github.com/Loe1210/course-project-agent.git
远程推送：main 已推送并跟踪 origin/main
远程推送：chore/repo-baseline 已推送并跟踪 origin/chore/repo-baseline
合并状态：chore/repo-baseline 已合并回 main
当前状态：第 0 步完成，等待进入第 1 步项目脚手架与配置模块
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
已合并
```

开发记录：

```text
开发时间：2026-06-11
开发分支：feature/project-scaffold
已创建：Maven Spring Boot 项目骨架
已创建：pom.xml
已创建：CourseProjectAgentApplication 启动类
已创建：/api/health 健康检查接口
已创建：application.yml 基础配置
已创建：controller / service / config / dto / agent/tool 基础包结构
已创建：静态首页 src/main/resources/static/index.html
已创建：README.md
已创建：.gitignore
已创建：Spring Boot 基础测试类
```

检查结果：

```text
Java 检查：通过，当前环境可用 Java 17
javac 检查：通过，当前环境可用 javac 17
pom.xml XML 解析：通过，artifactId=course-project-agent
关键路径检查：通过，启动类与 pom.xml mainClass 一致
Maven 位置：C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.4.1\plugins\maven\lib\maven3\bin\mvn.cmd
Maven 版本：Apache Maven 3.9.11
Maven 编译检查：通过，使用 IDEA 自带 Maven 执行 mvn test
测试结果：Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
合并状态：feature/project-scaffold 已合并回 main
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
已合并
```

开发记录：

```text
开发时间：2026-06-11
开发分支：feature/rag-knowledge-base
已创建：course-project-docs 初始知识库目录和 6 份课设资料文档
已创建：DocumentChunkConfig / FileUploadConfig / MilvusProperties / RagProperties
已创建：MilvusConfig / MilvusClientFactory / MilvusConstants
已创建：DocumentChunk / FileUploadResponse / ApiResponse
已创建：DocumentChunkService / VectorEmbeddingService / VectorIndexService / VectorSearchService
已创建：FileUploadController
已创建：CourseKnowledgeTools.queryCourseProjectKnowledge
已补充：pom.xml 的 gson 依赖
```

检查结果：

```text
测试工具：IDEA 自带 Maven 3.9.11
测试命令：mvn test
测试结果：BUILD SUCCESS
测试明细：Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
覆盖内容：Spring Boot 上下文启动、文档分片单元测试
已知提示：VectorSearchService 编译阶段存在 deprecated API 提示，但不影响当前构建成功
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
已合并
```

开发记录：

```text
开发时间：2026-06-12
开发分支：feature/course-tools
已创建：CourseProjectProperties 课设默认配置
已创建：CourseToolSupportService 工具层公共支持服务
已创建：TopicRecommendTools
已创建：DatabaseDesignTools
已创建：ApiDesignTools
已创建：ProjectTemplateTools
已创建：ReportTools
已创建：DefenseTools
已创建：DateTimeTools
已创建：CourseToolsSmokeTests
```

检查结果：

```text
测试工具：IDEA 自带 Maven 3.9.11
测试命令：mvn test
测试结果：BUILD SUCCESS
测试明细：Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
覆盖内容：工具模块 smoke test、Spring Boot 上下文启动、文档分片单元测试
说明：所有工具描述、返回内容、失败文案均为中文
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
已合并
```

开发记录：

```text
开发时间：2026-06-12
开发分支：feature/course-chat-agent
已创建：CourseChatProperties 对话配置
已创建：CourseChatRequest / CourseChatResponse
已创建：CourseChatMemoryService 本地会话记忆服务
已创建：CourseChatService
已创建：CourseChatController
已实现：中文系统 Prompt
已集成：RAG 知识库工具与本地课设工具
已支持：/api/course_chat
已支持：/api/course_chat_stream
已支持：基于 conversationId 的多轮上下文
已创建：CourseChatMemoryServiceTests
已创建：CourseChatControllerTests
已补充：application.yml 中的 course-chat 配置
```

检查结果：

```text
测试工具：IDEA 自带 Maven 3.9.11
测试命令：mvn test
测试结果：BUILD SUCCESS
测试明细：Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
覆盖内容：工具模块 smoke test、对话控制器测试、会话记忆测试、Spring Boot 上下文启动、文档分片单元测试
说明：对话 Agent 系统 Prompt、接口返回和异常文案均为中文
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
已合并
```

开发记录：

```text
开发时间：2026-06-12
开发分支：feature/course-project-agent
已创建：CourseProjectAgentProperties 任务 Agent 配置
已创建：CourseProjectRequest / CourseProjectResponse
已创建：CourseAgentToolRegistry 共享工具注册表
已创建：CourseProjectAgentService
已创建：CourseProjectController
已实现：Supervisor -> Planner -> Executor 三阶段中文 Prompt
已集成：RAG 知识库工具与本地课设工具
已支持：/api/course_project
已支持：/api/course_project_stream
已支持：基于 requestId 的任务请求编号生成
已创建：CourseProjectControllerTests
已创建：CourseProjectAgentServiceTests
已补充：application.yml 中的 course-project-agent 配置
已优化：统一工具注入方式，CourseChatService 改为复用 CourseAgentToolRegistry
```

检查结果：

```text
测试工具：IDEA 自带 Maven 3.9.11
测试命令：mvn test
测试结果：BUILD SUCCESS
测试明细：Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
覆盖内容：工具模块 smoke test、对话控制器测试、课设任务控制器测试、会话记忆测试、任务 Agent 基础测试、Spring Boot 上下文启动、文档分片单元测试
说明：PAR 任务 Agent 的 Prompt、接口返回和异常文案均为中文
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
待合并
```

开发记录：

```text
开发时间：2026-06-12
开发分支：feature/artifact-generation
已创建：ArtifactGenerationProperties 产物生成配置
已创建：ArtifactGenerationRequest / ArtifactGenerationResponse
已创建：ArtifactReviewRequest / ArtifactReviewResponse
已创建：ArtifactGenerationService
已创建：ArtifactGenerationController
已支持：/api/course_project/artifact
已支持：/api/course_project/review
已支持：单独生成数据库设计、接口设计、项目结构、报告大纲、报告初稿、测试用例、答辩问答
已支持：基于规则的完整性审查
已支持：可选的大模型审查与报告初稿生成
已创建：ArtifactGenerationControllerTests
已创建：ArtifactGenerationServiceTests
已补充：application.yml 中的 artifact-generation 配置
```

检查结果：

```text
测试工具：IDEA 自带 Maven 3.9.11
测试命令：mvn test
测试结果：BUILD SUCCESS
测试明细：Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
覆盖内容：工具模块 smoke test、产物控制器测试、产物服务测试、对话控制器测试、课设任务控制器测试、会话记忆测试、任务 Agent 基础测试、Spring Boot 上下文启动、文档分片单元测试
说明：产物生成与审查接口的返回和异常文案均为中文
补充说明：该模块分支已推送到远程，但尚未单独合并回 main；当前已合并进入 feature/course-workbench-ui，供前端工作台联调使用
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
待合并
```

开发记录：

```text
开发时间：2026-06-12
开发分支：feature/course-workbench-ui
已重构：src/main/resources/static/index.html 为三栏式课设工作台
已创建：src/main/resources/static/app.css
已创建：src/main/resources/static/app.js
已实现：课设对话 / 完整方案 / 产物生成 / 方案审查 四种模式切换
已实现：最近请求记录与快捷操作
已实现：右侧结构化结果面板
已实现：普通请求与流式请求前端调用
已实现：中文状态提示、错误提示和空状态文案
已联通：/api/course_chat
已联通：/api/course_chat_stream
已联通：/api/course_project
已联通：/api/course_project_stream
已联通：/api/course_project/artifact
已联通：/api/course_project/review
已补充：前端工作台设计文档 docs/superpowers/specs/2026-06-12-course-workbench-ui-design.md
```

检查结果：

```text
静态资源检查：通过，首页 / 与 /app.js 返回 200
测试工具：IDEA 自带 Maven 3.9.11
测试命令：mvn test
测试结果：BUILD SUCCESS
测试明细：Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
覆盖内容：工具模块 smoke test、产物控制器测试、产物服务测试、对话控制器测试、课设任务控制器测试、会话记忆测试、任务 Agent 基础测试、Spring Boot 上下文启动、文档分片单元测试
说明：前端所有界面文案、按钮文案、错误提示和状态提示均为中文
说明：真实大模型对话、RAG 检索与 Milvus 联调留到“联调与验收模块”统一验证
推送状态：feature/course-workbench-ui 已推送到 origin/feature/course-workbench-ui
```

### 8. 文档能力增强与 MCP 模块

分支：

```text
feature/document-mcp-enhancement
```

目标：

为课设通补齐 `docx/pdf/md/txt` 文档统一接入、`docx` 报告导出，以及独立文档 MCP 服务层，打通“课设资料上传增强知识库”和“报告成果下载交付”的完整闭环。

计划内容：

1. 新增 `document-mcp-server` 目录和基础运行骨架。
2. 接入成熟文档工具实现统一解析能力。
3. 实现 `parse_document` MCP 工具，支持 `docx/pdf/md/txt` 转 Markdown 或纯文本。
4. 实现 `export_report_docx` MCP 工具，支持报告内容导出为 Word 文件。
5. 改造课设通知识库上传链路，上传后先走文档解析，再进入分片、向量化、Milvus 入库。
6. 改造课设产物生成链路，支持报告导出下载。
7. 更新前端上传提示、知识库说明和报告下载入口。
8. 补充本轮模块测试与联调检查。

验收标准：

1. 支持上传 `.txt`、`.md`、`.docx`、`.pdf` 文档。
2. 上传后的文档可被统一解析并进入现有 RAG 链路。
3. 报告大纲或报告正文可导出为 `.docx` 文件。
4. 主服务通过 MCP 工具调用文档能力，而不是直接强耦合到具体文档解析库。
5. 提示词、报错、日志、接口返回描述保持中文。

当前状态：

```text
检查中
```

设计决策：

```text
设计时间：2026-06-13
开发分支：feature/document-mcp-enhancement
文档解析内核：优先采用成熟开源工具 MarkItDown
报告导出内核：当前实现采用 python-docx 生成 docx 文件
接入方式：新增独立 document-mcp-server，对主服务暴露 parse_document 和 export_report_docx 两个核心工具
主服务职责：负责上传接口、RAG 分片与向量化、PAR 流程、产物生成主链路
MCP 服务职责：负责文档解析与 Word 导出，降低主服务与具体文档库的耦合
MCP 调用方案：主服务采用标准 McpSyncClient + WebFluxSseClientTransport，通过 SSE 连接本地 document-mcp-server
Spring AI 自动装配：当前未启用通用 spring.ai.mcp.client 自动发现，而是为文档服务单独封装标准 MCP SDK 调用层，避免影响现有工具链
本轮策略：先落地标准 SSE MCP 文档链路，再进入联调与验收模块
```

开发记录：

```text
开发时间：2026-06-13
开发分支：feature/document-mcp-enhancement
已创建：document-mcp-server/server.py FastMCP 文档服务
已创建：document-mcp-server/invoke_tool.py 本地 bridge 调用入口
已创建：document-mcp-server/services/document_parser.py
已创建：document-mcp-server/services/docx_exporter.py
已创建：DocumentMcpProperties / DocumentGateway / DocumentMcpClient
已创建：ArtifactDownloadController
已改造：知识库上传白名单，支持 txt/md/docx/pdf
已改造：VectorIndexService 先解析文档，再进入现有分片、向量化、Milvus 链路
已改造：ArtifactGenerationService 在报告类产物生成后自动导出 docx 并返回下载地址
已改造：前端知识库提示文案与单项报告产物下载入口
已补充：本地 Python 虚拟环境 .venv-document-mcp 与 document-mcp-server/requirements.txt
已升级：document-mcp-server/server.py 支持通过命令行参数以 SSE 模式启动
已升级：DocumentMcpProperties 增加 server-script-path、transport、host、port、sse-endpoint、startup-timeout-ms 等配置
已升级：DocumentMcpClient 从本地 bridge 调用切换为标准 McpSyncClient + WebFluxSseClientTransport 调用
已补充：DocumentMcpClientIntegrationTests，用真实 Python 文档服务验证 SSE 文档解析与 docx 导出链路
```

检查结果：

```text
后端测试工具：IDEA 自带 Maven 3.9.11
测试命令：mvn test
测试结果：BUILD SUCCESS
测试明细：Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
额外检查：document-mcp-server 的 /sse 端点可正常启动并返回 200
额外检查：DocumentMcpClientIntegrationTests 已真实跑通「Java MCP Client -> SSE -> Python 文档服务 -> 文档解析 / docx 导出」链路
说明：当前文档能力已切换为标准 SSE MCP 调用，不再由主服务直接调用本地 bridge 完成核心流程
```

### 9. 联调与验收模块

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

### 10. 文档与发布模块

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
| 2026-06-11 | 仓库与工程基线模块 | `chore/repo-baseline` | 创建基线功能分支并记录当前状态 | 本地完成，待配置 GitHub remote 后推送 |
| 2026-06-11 | 仓库与工程基线模块 | `main` | 配置 GitHub remote 并推送 main | 已完成 |
| 2026-06-11 | 仓库与工程基线模块 | `chore/repo-baseline` | 推送基线功能分支 | 已完成，待用户确认合并 |
| 2026-06-11 | 仓库与工程基线模块 | `main` | 合并 chore/repo-baseline 并推送 main | 已完成 |
| 2026-06-11 | 项目脚手架与配置模块 | `feature/project-scaffold` | 创建 Spring Boot Maven 基础工程与健康检查接口 | 本地完成，IDEA 自带 Maven 执行 mvn test 通过 |
| 2026-06-11 | 项目脚手架与配置模块 | `main` | 合并 feature/project-scaffold | 已完成 |
| 2026-06-11 | RAG 知识库模块 | `feature/rag-knowledge-base` | 实现知识库文档、上传、分片、向量化、Milvus 检索和知识库工具 | 本地完成，IDEA 自带 Maven 执行 mvn test 通过 |
| 2026-06-11 | 中文化修正规范 | `feature/chinese-message-standardization` | 统一 RAG 模块提示词、报错、日志与 Agent 问答语言为中文 | 本地完成，IDEA 自带 Maven 执行 mvn test 通过 |
| 2026-06-12 | Tool 工具模块 | `feature/course-tools` | 实现选题推荐、数据库设计、接口设计、项目结构、报告大纲、答辩准备与时间工具 | 本地完成，IDEA 自带 Maven 执行 mvn test 通过 |
| 2026-06-12 | ReAct 课设对话 Agent 模块 | `feature/course-chat-agent` | 实现中文课设对话 Agent、流式接口、工具调用与本地会话记忆 | 本地完成，IDEA 自带 Maven 执行 mvn test 通过 |
| 2026-06-12 | PAR 课设任务 Agent 模块 | `feature/course-project-agent` | 实现 Supervisor、Planner、Executor 三阶段课设方案生成与流式接口 | 本地完成，IDEA 自带 Maven 执行 mvn test 通过 |
| 2026-06-12 | 课设产物生成模块 | `feature/artifact-generation` | 实现单产物生成与完整性审查接口，支持报告初稿、测试用例与答辩问答生成 | 本地完成，IDEA 自带 Maven 执行 mvn test 通过 |
| 2026-06-13 | 文档能力增强与 MCP 模块 | `feature/document-mcp-enhancement` | 确认采用“成熟文档工具 + 自建 document-mcp-server”的增强方案并创建功能分支 | 已完成，进入设计落档与开发准备 |
| 2026-06-13 | 文档能力增强与 MCP 模块 | `feature/document-mcp-enhancement` | 实现文档解析 bridge、FastMCP 文档服务、RAG 接入、报告 docx 导出与前端下载入口 | 本地完成，mvn test 通过，等待推送 |
| 2026-06-13 | 文档能力增强与 MCP 模块 | `feature/document-mcp-enhancement` | 将文档接入方式从本地 bridge 升级为标准 SSE MCP 调用，并补充真实集成测试 | 本地完成，22 项测试通过 |

## 7. 当前下一步

建议下一步执行：

```text
第 8 步：文档能力增强与 MCP 模块
建议分支：feature/document-mcp-enhancement
下一步动作：将当前 SSE MCP 版本提交并推送到 GitHub 对应分支，向用户汇报本轮检查结果，再决定是否继续联调与合并
```
