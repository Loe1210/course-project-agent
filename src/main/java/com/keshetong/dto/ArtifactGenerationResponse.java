package com.keshetong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArtifactGenerationResponse {

    private String requestId;
    private String topic;
    private String artifactType;
    private String content;
}
