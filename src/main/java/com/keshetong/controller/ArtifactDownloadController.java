package com.keshetong.controller;

import com.keshetong.config.DocumentMcpProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ArtifactDownloadController {

    private final DocumentMcpProperties documentMcpProperties;

    public ArtifactDownloadController(DocumentMcpProperties documentMcpProperties) {
        this.documentMcpProperties = documentMcpProperties;
    }

    @GetMapping("/api/course_project/artifact/download/{fileName}")
    public ResponseEntity<Resource> downloadArtifact(@PathVariable String fileName) {
        String outputPath = documentMcpProperties.getOutputPath();
        if (outputPath == null || outputPath.isBlank()) {
            return ResponseEntity.internalServerError().build();
        }
        Path exportDir = Paths.get(outputPath).normalize();
        if (!exportDir.isAbsolute()) {
            exportDir = Paths.get("").resolve(exportDir).normalize();
        }
        Path target = exportDir.resolve(fileName).normalize();
        if (!target.startsWith(exportDir) || !Files.exists(target) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(target));
    }
}
