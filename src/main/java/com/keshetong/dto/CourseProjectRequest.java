package com.keshetong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseProjectRequest {

    private String requestId;
    private String topic;
    private String direction;
    private String requirements;
    private String techStack;
    private Boolean useTools;
}
