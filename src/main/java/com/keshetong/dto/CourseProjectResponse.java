package com.keshetong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseProjectResponse {

    private String requestId;
    private String topic;
    private String supervisorResult;
    private String plannerResult;
    private String executorResult;
    private String databaseDesign;
    private String apiDesign;
    private String projectStructure;
    private String reportOutline;
    private String testCases;
    private String defenseQa;
    private String summary;
    private Map<String, String> failedArtifacts;
    private boolean usedTools;
}
