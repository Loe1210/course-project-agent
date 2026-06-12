package com.keshetong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtifactReviewRequest {

    private String requestId;
    private String topic;
    private String content;
    private String reviewType;
}
