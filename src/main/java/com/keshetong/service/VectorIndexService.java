package com.keshetong.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.keshetong.config.FileUploadConfig;
import com.keshetong.config.RagProperties;
import com.keshetong.dto.DocumentChunk;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VectorIndexService {

    private static final Logger logger = LoggerFactory.getLogger(VectorIndexService.class);

    private final MilvusServiceClient milvusClient;
    private final VectorEmbeddingService embeddingService;
    private final DocumentChunkService chunkService;
    private final FileUploadConfig fileUploadConfig;
    private final RagProperties ragProperties;

    public VectorIndexService(@Lazy MilvusServiceClient milvusClient,
                              VectorEmbeddingService embeddingService,
                              DocumentChunkService chunkService,
                              FileUploadConfig fileUploadConfig,
                              RagProperties ragProperties) {
        this.milvusClient = milvusClient;
        this.embeddingService = embeddingService;
        this.chunkService = chunkService;
        this.fileUploadConfig = fileUploadConfig;
        this.ragProperties = ragProperties;
    }

    public IndexingResult indexDirectory(String directoryPath) {
        IndexingResult result = new IndexingResult();
        result.setStartTime(LocalDateTime.now());
        try {
            String targetPath = directoryPath == null || directoryPath.isBlank() ? fileUploadConfig.getPath() : directoryPath;
            Path dirPath = Paths.get(targetPath).normalize();
            File directory = dirPath.toFile();
            if (!directory.exists() || !directory.isDirectory()) {
                throw new IllegalArgumentException("目录不存在：" + targetPath);
            }
            result.setDirectoryPath(directory.getAbsolutePath());

            File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".md"));
            if (files == null || files.length == 0) {
                result.setTotalFiles(0);
                result.setSuccess(true);
                result.setEndTime(LocalDateTime.now());
                return result;
            }

            result.setTotalFiles(files.length);
            for (File file : files) {
                try {
                    indexSingleFile(file.getAbsolutePath());
                    result.incrementSuccessCount();
                } catch (Exception e) {
                    result.incrementFailCount();
                    result.addFailedFile(file.getAbsolutePath(), e.getMessage());
                }
            }

            result.setSuccess(result.getFailCount() == 0);
            result.setEndTime(LocalDateTime.now());
            return result;
        } catch (Exception e) {
            logger.error("批量建立知识库索引失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
            return result;
        }
    }

    public void indexSingleFile(String filePath) throws Exception {
        Path path = Paths.get(filePath).normalize();
        File file = path.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在：" + filePath);
        }

        String content = Files.readString(path);
        deleteExistingData(path.toString());
        List<DocumentChunk> chunks = chunkService.chunkDocument(content, path.toString());
        for (DocumentChunk chunk : chunks) {
            List<Float> vector = embeddingService.generateEmbedding(chunk.getContent());
            Map<String, Object> metadata = buildMetadata(path.toString(), chunk, chunks.size());
            insertToMilvus(chunk.getContent(), vector, metadata, chunk.getChunkIndex());
        }
    }

    private void deleteExistingData(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            String normalizedPath = path.toString().replace(File.separator, "/");
            milvusClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(ragProperties.getCollectionName())
                            .build()
            );
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(ragProperties.getCollectionName())
                    .withExpr(String.format("metadata[\"_source\"] == \"%s\"", normalizedPath))
                    .build();
            milvusClient.delete(deleteParam);
        } catch (Exception e) {
            logger.warn("跳过删除历史向量数据：{}", e.getMessage());
        }
    }

    private Map<String, Object> buildMetadata(String filePath, DocumentChunk chunk, int totalChunks) {
        Map<String, Object> metadata = new HashMap<>();
        Path path = Paths.get(filePath).normalize();
        String normalizedPath = path.toString().replace(File.separator, "/");
        String fileName = path.getFileName() != null ? path.getFileName().toString() : "";
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
        }
        metadata.put("_source", normalizedPath);
        metadata.put("_extension", extension);
        metadata.put("_file_name", fileName);
        metadata.put("chunkIndex", chunk.getChunkIndex());
        metadata.put("totalChunks", totalChunks);
        if (chunk.getTitle() != null && !chunk.getTitle().isEmpty()) {
            metadata.put("title", chunk.getTitle());
        }
        return metadata;
    }

    private void insertToMilvus(String content, List<Float> vector, Map<String, Object> metadata, int chunkIndex) {
        try {
            R<RpcStatus> loadResponse = milvusClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(ragProperties.getCollectionName())
                            .build()
            );
            if (loadResponse.getStatus() != 0 && loadResponse.getStatus() != 65535) {
                throw new RuntimeException("加载 Milvus 集合失败：" + loadResponse.getMessage());
            }

            String source = (String) metadata.get("_source");
            String id = UUID.nameUUIDFromBytes((source + "_" + chunkIndex).getBytes()).toString();
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", Collections.singletonList(id)));
            fields.add(new InsertParam.Field("content", Collections.singletonList(content)));
            fields.add(new InsertParam.Field("vector", Collections.singletonList(vector)));
            JsonObject metadataJson = new Gson().toJsonTree(metadata).getAsJsonObject();
            fields.add(new InsertParam.Field("metadata", Collections.singletonList(metadataJson)));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(ragProperties.getCollectionName())
                    .withFields(fields)
                    .build();

            R<MutationResult> response = milvusClient.insert(insertParam);
            if (response.getStatus() != 0) {
                throw new RuntimeException("写入向量数据失败：" + response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("写入文档分片到 Milvus 失败：" + e.getMessage(), e);
        }
    }

    @Getter
    public static class IndexingResult {
        @Setter
        private boolean success;
        @Setter
        private String directoryPath;
        @Setter
        private int totalFiles;
        private int successCount;
        private int failCount;
        @Setter
        private LocalDateTime startTime;
        @Setter
        private LocalDateTime endTime;
        @Setter
        private String errorMessage;
        private final Map<String, String> failedFiles = new HashMap<>();

        public void incrementSuccessCount() {
            successCount++;
        }

        public void incrementFailCount() {
            failCount++;
        }

        public void addFailedFile(String filePath, String error) {
            failedFiles.put(filePath, error);
        }
    }
}
