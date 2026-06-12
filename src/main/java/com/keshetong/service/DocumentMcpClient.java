package com.keshetong.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keshetong.config.DocumentMcpProperties;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentMcpClient implements DocumentGateway {

    private static final Logger logger = LoggerFactory.getLogger(DocumentMcpClient.class);
    private static final String PARSE_DOCUMENT_TOOL = "parse_document_tool";
    private static final String EXPORT_REPORT_DOCX_TOOL = "export_report_docx_tool";

    private final DocumentMcpProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Object lifecycleMonitor = new Object();

    private volatile Process serverProcess;
    private volatile McpSyncClient syncClient;

    public DocumentMcpClient(DocumentMcpProperties properties) {
        this.properties = properties;
    }

    @Override
    public ParsedDocumentResult parseDocument(String filePath) {
        Path path = Paths.get(filePath).normalize();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("待解析文档不存在：" + filePath);
        }

        String extension = resolveExtension(path.getFileName() == null ? "" : path.getFileName().toString());
        if (!properties.isEnabled()) {
            return fallbackParse(path, extension);
        }

        Map<String, Object> result = callTool(PARSE_DOCUMENT_TOOL, Map.of("file_path", path.toString()), "文档解析失败");
        String title = getString(result, "title");
        String markdown = getString(result, "markdown");
        String textContent = getString(result, "text_content");
        String fileType = getString(result, "file_type");
        if (textContent == null || textContent.isBlank()) {
            throw new RuntimeException("文档解析结果为空，无法写入知识库");
        }
        return new ParsedDocumentResult(title, markdown, textContent, normalizeValue(fileType, extension));
    }

    @Override
    public ExportedDocumentResult exportReportDocx(String topic, String artifactType, String title, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("报告内容不能为空，无法导出 Word 文件");
        }
        if (!properties.isEnabled()) {
            throw new RuntimeException("文档 MCP 服务未启用，无法导出 Word 文件");
        }

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("topic", topic);
        args.put("artifact_type", artifactType);
        args.put("title", title);
        args.put("content", content);
        args.put("output_dir", resolveOutputDir().toString());

        Map<String, Object> result = callTool(EXPORT_REPORT_DOCX_TOOL, args, "报告导出失败");
        String fileName = getString(result, "file_name");
        String filePath = getString(result, "file_path");
        if (fileName == null || fileName.isBlank() || filePath == null || filePath.isBlank()) {
            throw new RuntimeException("报告导出结果不完整，未返回文件信息");
        }
        return new ExportedDocumentResult(fileName, filePath, "/api/course_project/artifact/download/" + fileName);
    }

    private Map<String, Object> callTool(String toolName, Map<String, Object> args, String defaultErrorMessage) {
        McpSyncClient client = ensureClientReady();
        try {
            McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(toolName, args));
            if (Boolean.TRUE.equals(result.isError())) {
                throw new RuntimeException(extractTextContent(result, defaultErrorMessage));
            }
            return extractStructuredContent(result, defaultErrorMessage);
        } catch (RuntimeException e) {
            resetClientState();
            throw e;
        } catch (Exception e) {
            resetClientState();
            throw new RuntimeException(defaultErrorMessage + "：" + e.getMessage(), e);
        }
    }

    private McpSyncClient ensureClientReady() {
        McpSyncClient current = this.syncClient;
        if (current != null) {
            return current;
        }
        synchronized (lifecycleMonitor) {
            if (this.syncClient != null) {
                return this.syncClient;
            }
            ensureServerStarted();
            try {
                WebFluxSseClientTransport transport = WebFluxSseClientTransport.builder(
                                WebClient.builder().baseUrl(buildBaseUrl()))
                        .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
                        .sseEndpoint(properties.getSseEndpoint())
                        .build();

                McpSyncClient createdClient = McpClient.sync(transport)
                        .clientInfo(new McpSchema.Implementation("course-project-document-client", "课设通文档客户端", "1.0.0"))
                        .requestTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                        .initializationTimeout(Duration.ofMillis(properties.getStartupTimeoutMs()))
                        .build();
                createdClient.initialize();
                this.syncClient = createdClient;
                logger.info("文档 MCP 客户端已通过 SSE 建立连接：{}", buildBaseUrl());
                return createdClient;
            } catch (Exception e) {
                resetClientState();
                throw new RuntimeException("初始化文档 MCP 客户端失败：" + e.getMessage(), e);
            }
        }
    }

    private void ensureServerStarted() {
        Process currentProcess = this.serverProcess;
        if (currentProcess != null && currentProcess.isAlive()) {
            waitForServerReady();
            return;
        }

        Path pythonPath = resolveRequiredPath(properties.getPythonPath(), "未配置文档 MCP Python 解释器路径");
        Path serverScriptPath = resolveRequiredPath(properties.getServerScriptPath(), "未配置文档 MCP 服务脚本路径");
        List<String> command = new ArrayList<>();
        command.add(pythonPath.toString());
        command.add(serverScriptPath.toString());
        command.add("--transport");
        command.add(normalizeValue(properties.getTransport(), "sse"));
        command.add("--host");
        command.add(normalizeValue(properties.getHost(), "127.0.0.1"));
        command.add("--port");
        command.add(String.valueOf(properties.getPort()));
        command.add("--log-level");
        command.add(normalizeValue(properties.getLogLevel(), "INFO"));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("PYTHONUTF8", "1");

        try {
            Process process = processBuilder.start();
            this.serverProcess = process;
            startLogPump(process);
            waitForServerReady();
            logger.info("文档 MCP 服务已启动：{}", buildBaseUrl());
        } catch (IOException e) {
            throw new RuntimeException("启动文档 MCP 服务失败：" + e.getMessage(), e);
        }
    }

    private void waitForServerReady() {
        long deadline = System.currentTimeMillis() + properties.getStartupTimeoutMs();
        while (System.currentTimeMillis() < deadline) {
            Process currentProcess = this.serverProcess;
            if (currentProcess != null && !currentProcess.isAlive()) {
                throw new RuntimeException("文档 MCP 服务启动失败，进程已提前退出");
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()), 1000);
                return;
            } catch (IOException ignored) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待文档 MCP 服务启动时被中断", e);
                }
            }
        }
        throw new RuntimeException("等待文档 MCP 服务启动超时，请检查 Python 环境或端口占用情况");
    }

    private void startLogPump(Process process) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        logger.info("文档 MCP 服务输出：{}", line);
                    }
                }
            } catch (IOException e) {
                logger.warn("读取文档 MCP 服务日志失败：{}", e.getMessage());
            }
        }, "document-mcp-log-pump");
        thread.setDaemon(true);
        thread.start();
    }

    private ParsedDocumentResult fallbackParse(Path path, String extension) {
        if (!"txt".equals(extension) && !"md".equals(extension)) {
            throw new RuntimeException("文档 MCP 服务未启用，暂不支持解析该格式：" + extension);
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            String title = path.getFileName() == null ? "" : path.getFileName().toString();
            return new ParsedDocumentResult(title, content, content, extension);
        } catch (IOException e) {
            throw new RuntimeException("读取本地文本文件失败：" + e.getMessage(), e);
        }
    }

    private Map<String, Object> extractStructuredContent(McpSchema.CallToolResult result, String defaultErrorMessage) {
        Object structuredContent = result.structuredContent();
        if (structuredContent != null) {
            return objectMapper.convertValue(structuredContent, new TypeReference<>() {
            });
        }
        String textContent = extractTextContent(result, defaultErrorMessage);
        try {
            return objectMapper.readValue(textContent, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            throw new RuntimeException(textContent);
        }
    }

    private String extractTextContent(McpSchema.CallToolResult result, String defaultMessage) {
        if (result.content() == null || result.content().isEmpty()) {
            return defaultMessage;
        }
        for (McpSchema.Content content : result.content()) {
            if (content instanceof McpSchema.TextContent textContent) {
                String text = textContent.text();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return defaultMessage;
    }

    private Path resolveRequiredPath(String configuredPath, String errorMessage) {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new RuntimeException(errorMessage);
        }
        Path path = Paths.get(configuredPath).normalize();
        if (!path.isAbsolute()) {
            path = Paths.get("").resolve(path).normalize();
        }
        if (!Files.exists(path)) {
            throw new RuntimeException("文档 MCP 依赖文件不存在：" + path);
        }
        return path;
    }

    private Path resolveOutputDir() {
        String outputPath = properties.getOutputPath();
        if (outputPath == null || outputPath.isBlank()) {
            throw new RuntimeException("未配置报告导出目录");
        }
        Path path = Paths.get(outputPath).normalize();
        if (!path.isAbsolute()) {
            path = Paths.get("").resolve(path).normalize();
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("创建报告导出目录失败：" + e.getMessage(), e);
        }
        return path;
    }

    private String buildBaseUrl() {
        return "http://" + normalizeValue(properties.getHost(), "127.0.0.1") + ":" + properties.getPort();
    }

    private String resolveExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    private String getString(Map<String, Object> response, String fieldName) {
        Object value = response.get(fieldName);
        return value == null ? null : String.valueOf(value);
    }

    private String normalizeValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void resetClientState() {
        synchronized (lifecycleMonitor) {
            closeClientQuietly();
            destroyProcessQuietly();
            this.syncClient = null;
            this.serverProcess = null;
        }
    }

    private void closeClientQuietly() {
        if (this.syncClient == null) {
            return;
        }
        try {
            this.syncClient.closeGracefully();
        } catch (Exception e) {
            logger.warn("关闭文档 MCP 客户端时出现异常：{}", e.getMessage());
        }
    }

    private void destroyProcessQuietly() {
        if (this.serverProcess == null) {
            return;
        }
        try {
            this.serverProcess.destroy();
            if (!this.serverProcess.waitFor(3, TimeUnit.SECONDS)) {
                this.serverProcess.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.serverProcess.destroyForcibly();
        } catch (Exception e) {
            logger.warn("关闭文档 MCP 服务进程时出现异常：{}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        resetClientState();
    }
}
