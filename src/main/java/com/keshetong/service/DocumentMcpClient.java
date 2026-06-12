package com.keshetong.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.keshetong.config.DocumentMcpProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentMcpClient implements DocumentGateway {

    private static final Logger logger = LoggerFactory.getLogger(DocumentMcpClient.class);
    private static final Gson GSON = new Gson();

    private final DocumentMcpProperties properties;

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

        JsonObject payload = new JsonObject();
        payload.addProperty("file_path", path.toString());
        payload.addProperty("file_type", extension);

        JsonObject response = invokeBridge("parse_document", payload);
        ensureSuccess(response, "文档解析失败");
        String title = getString(response, "title");
        String markdown = getString(response, "markdown");
        String textContent = getString(response, "text_content");
        String fileType = getString(response, "file_type");
        if (textContent == null || textContent.isBlank()) {
            throw new RuntimeException("文档解析结果为空，无法写入知识库");
        }
        return new ParsedDocumentResult(title, markdown, textContent, fileType == null || fileType.isBlank() ? extension : fileType);
    }

    @Override
    public ExportedDocumentResult exportReportDocx(String topic, String artifactType, String title, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("报告内容不能为空，无法导出 Word 文件");
        }
        if (!properties.isEnabled()) {
            throw new RuntimeException("文档 MCP 服务未启用，无法导出 Word 文件");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("topic", topic);
        payload.addProperty("artifact_type", artifactType);
        payload.addProperty("title", title);
        payload.addProperty("content", content);
        payload.addProperty("output_dir", resolveOutputDir().toString());

        JsonObject response = invokeBridge("export_report_docx", payload);
        ensureSuccess(response, "报告导出失败");
        String fileName = getString(response, "file_name");
        String filePath = getString(response, "file_path");
        if (fileName == null || fileName.isBlank() || filePath == null || filePath.isBlank()) {
            throw new RuntimeException("报告导出结果不完整，未返回文件信息");
        }
        return new ExportedDocumentResult(fileName, filePath, "/api/course_project/artifact/download/" + fileName);
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

    private JsonObject invokeBridge(String action, JsonObject payload) {
        Path pythonPath = resolveRequiredPath(properties.getPythonPath(), "未配置文档 MCP Python 解释器路径");
        Path bridgeScript = resolveRequiredPath(properties.getBridgeScriptPath(), "未配置文档 MCP bridge 脚本路径");
        List<String> command = new ArrayList<>();
        command.add(pythonPath.toString());
        command.add(bridgeScript.toString());
        command.add(action);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("PYTHONUTF8", "1");

        try {
            Process process = processBuilder.start();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(GSON.toJson(payload));
            }

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().reduce("", (left, right) -> left + right);
            }

            boolean finished = process.waitFor(Duration.ofMillis(properties.getTimeoutMs()).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("调用文档 MCP 工具超时：" + action);
            }

            if (output == null || output.isBlank()) {
                throw new RuntimeException("文档 MCP 工具未返回结果：" + action);
            }

            JsonObject response = GSON.fromJson(output, JsonObject.class);
            if (response == null) {
                throw new RuntimeException("文档 MCP 工具返回了无法解析的结果：" + action);
            }
            return response;
        } catch (IOException e) {
            logger.error("启动文档 MCP bridge 失败：{}", action, e);
            throw new RuntimeException("启动文档 MCP 工具失败：" + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("调用文档 MCP 工具时被中断", e);
        }
    }

    private void ensureSuccess(JsonObject response, String defaultMessage) {
        if (!response.has("success") || !response.get("success").getAsBoolean()) {
            String error = getString(response, "error");
            throw new RuntimeException(error == null || error.isBlank() ? defaultMessage : error);
        }
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

    private String resolveExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    private String getString(JsonObject response, String fieldName) {
        if (!response.has(fieldName) || response.get(fieldName).isJsonNull()) {
            return null;
        }
        return response.get(fieldName).getAsString();
    }
}
