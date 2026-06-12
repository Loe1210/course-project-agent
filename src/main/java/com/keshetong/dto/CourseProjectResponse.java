package com.keshetong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseProjectResponse {

    private String requestId;
    private String topic;
    private String supervisorResult;
    private String plannerResult;
    private String executorResult;
    private boolean usedTools;
}
