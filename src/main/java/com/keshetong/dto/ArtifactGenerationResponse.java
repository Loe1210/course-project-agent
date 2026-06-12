package com.keshetong.dto;

import lombok.Getter;

@Getter
public class ArtifactGenerationResponse {

    private final String requestId;
    private final String topic;
    private final String artifactType;
    private final String content;
    private final String exportedFileName;
    private final String exportedFilePath;
    private final String downloadUrl;

    public ArtifactGenerationResponse(String requestId, String topic, String artifactType, String content) {
        this(requestId, topic, artifactType, content, null, null, null);
    }

    public ArtifactGenerationResponse(String requestId,
                                      String topic,
                                      String artifactType,
                                      String content,
                                      String exportedFileName,
                                      String exportedFilePath,
                                      String downloadUrl) {
        this.requestId = requestId;
        this.topic = topic;
        this.artifactType = artifactType;
        this.content = content;
        this.exportedFileName = exportedFileName;
        this.exportedFilePath = exportedFilePath;
        this.downloadUrl = downloadUrl;
    }
}
