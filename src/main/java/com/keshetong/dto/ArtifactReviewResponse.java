package com.keshetong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArtifactReviewResponse {

    private String requestId;
    private String topic;
    private String reviewType;
    private String reviewResult;
}
