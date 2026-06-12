package com.keshetong.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "course-project-agent")
public class CourseProjectAgentProperties {

    private boolean enableTools = true;
    private String defaultRequestPrefix = "course-project";

    public void setEnableTools(boolean enableTools) {
        this.enableTools = enableTools;
    }

    public void setDefaultRequestPrefix(String defaultRequestPrefix) {
        this.defaultRequestPrefix = defaultRequestPrefix;
    }
}
