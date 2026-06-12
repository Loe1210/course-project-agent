package com.keshetong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseChatRequest {

    private String conversationId;
    private String message;
    private Boolean useTools;
}
