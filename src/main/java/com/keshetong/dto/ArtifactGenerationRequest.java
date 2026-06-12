package com.keshetong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtifactGenerationRequest {

    private String requestId;
    private String topic;
    private String artifactType;
    private String direction;
    private String requirements;
    private String techStack;
    private String existingPlan;
}
