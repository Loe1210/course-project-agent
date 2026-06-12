package com.keshetong.service;

import com.keshetong.config.DocumentMcpProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DocumentMcpClientIntegrationTests {

    private DocumentMcpClient documentMcpClient;

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        if (documentMcpClient != null) {
            documentMcpClient.shutdown();
        }
    }

    @Test
    void shouldParseMarkdownAndExportDocxOverSse() throws Exception {
        Path projectRoot = Path.of("").toAbsolutePath().normalize();
        Path pythonPath = projectRoot.resolve(".venv-document-mcp").resolve("Scripts").resolve("python.exe");
        Path serverScriptPath = projectRoot.resolve("document-mcp-server").resolve("server.py");

        assumeTrue(Files.exists(pythonPath), "未检测到文档 MCP Python 环境，跳过 SSE 集成测试");
        assumeTrue(Files.exists(serverScriptPath), "未检测到文档 MCP 服务脚本，跳过 SSE 集成测试");

        DocumentMcpProperties properties = new DocumentMcpProperties();
        properties.setEnabled(true);
        properties.setPythonPath(pythonPath.toString());
        properties.setServerScriptPath(serverScriptPath.toString());
        properties.setTransport("sse");
        properties.setHost("127.0.0.1");
        properties.setPort(18081);
        properties.setSseEndpoint("/sse");
        properties.setLogLevel("INFO");
        properties.setTimeoutMs(120000);
        properties.setStartupTimeoutMs(30000);
        properties.setOutputPath(tempDir.resolve("generated-reports").toString());

        documentMcpClient = new DocumentMcpClient(properties);

        Path markdownFile = tempDir.resolve("课程设计任务书.md");
        Files.writeString(markdownFile, "# 课程设计任务书\n\n这是一个用于验证 SSE 文档能力的测试文件。", StandardCharsets.UTF_8);

        DocumentGateway.ParsedDocumentResult parsedDocument = documentMcpClient.parseDocument(markdownFile.toString());
        assertEquals("md", parsedDocument.fileType());
        assertTrue(parsedDocument.textContent().contains("SSE 文档能力"), "解析结果应包含原始 Markdown 内容");

        DocumentGateway.ExportedDocumentResult exportedDocument = documentMcpClient.exportReportDocx(
                "学生成绩管理系统",
                "report_outline",
                "学生成绩管理系统报告大纲",
                "# 一、项目背景\n\n这是 SSE 集成测试生成的报告内容。"
        );
        assertTrue(Files.exists(Path.of(exportedDocument.filePath())), "应成功生成 docx 报告文件");
        assertTrue(exportedDocument.fileName().endsWith(".docx"), "导出文件应为 docx 格式");
    }
}
