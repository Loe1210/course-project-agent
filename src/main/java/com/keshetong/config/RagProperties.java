package com.keshetong.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private int topK = 3;
    private String collectionName = "course_project_knowledge";

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
