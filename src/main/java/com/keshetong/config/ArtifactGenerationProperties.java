package com.keshetong.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "artifact-generation")
public class ArtifactGenerationProperties {

    private boolean enableLlmDraft = true;

    public void setEnableLlmDraft(boolean enableLlmDraft) {
        this.enableLlmDraft = enableLlmDraft;
    }
}
