package com.keshetong.controller;

import com.keshetong.config.FileUploadConfig;
import com.keshetong.dto.ApiResponse;
import com.keshetong.dto.FileUploadResponse;
import com.keshetong.service.VectorIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    private final FileUploadConfig fileUploadConfig;
    private final VectorIndexService vectorIndexService;

    public FileUploadController(FileUploadConfig fileUploadConfig, VectorIndexService vectorIndexService) {
        this.fileUploadConfig = fileUploadConfig;
        this.vectorIndexService = vectorIndexService;
    }

    @PostMapping(value = "/api/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("文件不能为空"));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("文件名不能为空"));
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!isAllowedExtension(fileExtension)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest("不支持的文件格式，仅支持: " + fileUploadConfig.getAllowedExtensions()));
        }

        try {
            Path uploadDir = Paths.get(fileUploadConfig.getPath()).normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(originalFilename).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            Files.copy(file.getInputStream(), filePath);
            logger.info("已上传知识库文件：{}", filePath);

            try {
                VectorIndexService.SingleFileIndexingResult indexingResult = vectorIndexService.indexSingleFile(filePath.toString());
                return ResponseEntity.ok(ApiResponse.success(
                        new FileUploadResponse(
                                originalFilename,
                                filePath.toString(),
                                file.getSize(),
                                true,
                                indexingResult.isSuccess(),
                                indexingResult.getChunkCount(),
                                indexingResult.getMessage(),
                                null
                        )
                ));
            } catch (Exception e) {
                logger.error("知识库文件入库失败：{}", filePath, e);
                return ResponseEntity.ok(ApiResponse.success(
                        new FileUploadResponse(
                                originalFilename,
                                filePath.toString(),
                                file.getSize(),
                                true,
                                false,
                                0,
                                "文件已保存，但知识库入库失败",
                                e.getMessage()
                        )
                ));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("文件上传失败: " + e.getMessage()));
        }
    }

    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        String allowedExtensions = fileUploadConfig.getAllowedExtensions();
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return false;
        }
        List<String> allowedList = Arrays.asList(allowedExtensions.split(","));
        return allowedList.contains(extension.toLowerCase());
    }
}
