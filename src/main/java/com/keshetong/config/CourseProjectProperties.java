package com.keshetong.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Configuration
@ConfigurationProperties(prefix = "course-project")
public class CourseProjectProperties {

    private String defaultTopic = "校园二手交易平台";
    private List<String> defaultTechStack = new ArrayList<>(List.of("Spring Boot", "Vue", "MySQL"));

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    public void setDefaultTechStack(List<String> defaultTechStack) {
        this.defaultTechStack = defaultTechStack;
    }
}
