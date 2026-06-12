package com.keshetong.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "course-chat")
public class CourseChatProperties {

    private int maxHistoryMessages = 12;
    private boolean enableTools = true;
    private String defaultConversationPrefix = "course-chat";

    public void setMaxHistoryMessages(int maxHistoryMessages) {
        this.maxHistoryMessages = maxHistoryMessages;
    }

    public void setEnableTools(boolean enableTools) {
        this.enableTools = enableTools;
    }

    public void setDefaultConversationPrefix(String defaultConversationPrefix) {
        this.defaultConversationPrefix = defaultConversationPrefix;
    }
}
