package com.keshetong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseChatResponse {

    private String conversationId;
    private String answer;
    private boolean usedTools;
}
