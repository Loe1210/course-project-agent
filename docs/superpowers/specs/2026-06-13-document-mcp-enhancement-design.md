# 课设通 Agent 助手文档能力增强与 MCP 设计文档

## 1. 设计目标

本轮迭代目标不是单纯增加 `docx` 文件白名单，而是为课设通补齐一套可扩展的文档能力层：

1. 支持 `docx/pdf/md/txt` 统一上传与解析。
2. 将解析结果继续接入现有 RAG 链路，而不是另起一套知识库流程。
3. 支持将报告相关成果导出为 `docx`，贴合软件课设的真实交付形式。
4. 通过独立的 `document-mcp-server` 暴露文档工具，降低主服务与具体文档解析库的耦合。

## 2. 核心判断

### 2.1 为什么这轮不直接把文档库塞进主服务

如果主服务直接依赖某个文档解析库，短期实现确实更快，但会把上传解析、报告导出和具体技术选型绑死在 Spring Boot 应用里。后续如果替换解析器、增加模板套打、扩展批量导出，改动面会很大。

因此更合适的做法是：

- 成熟文档工具负责真正干活
- MCP 负责标准化接入
- 主服务只依赖“文档能力接口”，不直接依赖具体实现细节

### 2.2 为什么采用“成熟文档工具 + 自建 MCP Server”

当前可复用的成熟文档工具是存在的，例如 MarkItDown 对 `Word`、`PDF`、`Excel` 等格式的 Markdown/文本提取能力已经比较成熟，适合 RAG 场景。但现成、成熟、适合直接做核心链路的文档类 MCP Server 并不明显。

因此本轮采用：

- 文档解析：`MarkItDown`
- Word 导出：当前落地采用 `python-docx`
- MCP 服务层：我们自己实现 `document-mcp-server`

## 3. 架构设计

### 3.1 总体结构

```text
前端上传/下载
-> 课设通主服务
-> MCP Client 调用层
-> document-mcp-server
-> 文档解析/导出工具
```

### 3.2 组件职责

`课设通主服务`

- 负责上传接口、RAG 分片、向量化、Milvus 入库
- 负责 PAR 主流程、产物生成主流程
- 负责把文档解析和导出请求转发给 MCP 工具

`DocumentMcpClient`

- 负责封装主服务到 MCP 服务的调用
- 负责以 `McpSyncClient + WebFluxSseClientTransport` 方式连接本地 `document-mcp-server`
- 负责请求构造、响应解析、异常转换
- 对上层暴露统一中文接口

`document-mcp-server`

- 负责文档相关工具的暴露与执行
- 不承担课设业务逻辑，只负责文档能力

`parse_document`

- 输入：文件路径、文件类型
- 输出：Markdown/纯文本、元数据、状态信息

`export_report_docx`

- 输入：报告标题、章节内容、输出目录
- 输出：生成的 docx 路径、文件名、状态信息

## 4. 数据流

### 4.1 知识库上传链路

```text
前端上传 docx/pdf/md/txt
-> 主服务保存原文件
-> 调用 parse_document
-> 返回纯文本/Markdown
-> DocumentChunkService 分片
-> DashScope Embedding 向量化
-> Milvus 入库
```

### 4.2 报告导出链路

```text
用户生成报告大纲或报告正文
-> 主服务聚合文本内容
-> 调用 export_report_docx
-> 生成 Word 文件
-> 前端展示下载入口
```

### 4.3 MCP 调用链路

```text
主服务触发文档能力
-> DocumentMcpClient 检查本地 document-mcp-server 是否已启动
-> 未启动则拉起 Python FastMCP 服务（SSE）
-> McpSyncClient 通过 WebFluxSseClientTransport 连接 /sse
-> 调用 parse_document_tool 或 export_report_docx_tool
-> 解析工具结果并回传给主服务
```

## 5. 目录规划

建议新增目录如下：

```text
课设通Agent助手/
├── document-mcp-server/
│   ├── server.py
│   ├── invoke_tool.py
│   ├── requirements.txt
│   └── services/
│       ├── document_parser.py
│       └── docx_exporter.py
├── src/main/java/com/keshetong/
│   ├── config/DocumentMcpProperties.java
│   ├── controller/ArtifactDownloadController.java
│   └── service/
│       ├── DocumentGateway.java
│       └── DocumentMcpClient.java
```

## 6. 错误处理

需要统一中文错误语义，至少覆盖以下场景：

1. 文件类型不支持。
2. 文档解析失败。
3. MCP 服务不可用。
4. docx 导出失败。
5. 解析结果为空，无法进入向量化流程。

主服务不应把底层第三方库原始异常直接暴露给前端，而应转换成面向用户和开发联调都可理解的中文描述。

## 7. 测试策略

本轮至少覆盖：

1. `txt/md/docx/pdf` 文件类型白名单测试。
2. 文档解析结果非空测试。
3. RAG 上传链路在解析后仍可继续分片和入库的服务测试。
4. `docx` 报告导出文件存在性测试。
5. bridge 调用异常时的中文错误返回测试。

## 8. 范围边界

本轮不做：

1. 复杂 Word 模板套打。
2. PDF 高保真版式还原。
3. 云端远程 MCP 部署。
4. 文档在线预览编辑。
5. 多租户文档隔离平台化能力。

本轮只聚焦“能解析、能入库、能导出、能通过标准 SSE MCP 调用”。

## 9. 开发顺序

1. 搭 `document-mcp-server` 骨架。
2. 接入 `MarkItDown`，实现 `parse_document`。
3. 接入 `python-docx`，实现 `export_report_docx`。
4. 补主服务 `DocumentMcpClient`。
5. 改上传链路对接 MCP。
6. 改报告导出链路对接 MCP。
7. 补前端提示和下载入口。
8. 跑本地测试和检查。

## 10. 验收结论

本轮完成后，课设通应具备以下能力：

1. 老师发的 `docx` 任务书和报告模板可以直接上传进知识库。
2. `pdf` 资料可以统一解析后进入 RAG。
3. 生成的课设报告可以导出为 `docx` 文件。
4. 文档能力通过 MCP 接入，主服务不直接耦合到底层解析实现。
