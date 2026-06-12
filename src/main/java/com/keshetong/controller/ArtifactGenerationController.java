package com.keshetong.controller;

import com.keshetong.dto.ApiResponse;
import com.keshetong.dto.ArtifactGenerationRequest;
import com.keshetong.dto.ArtifactGenerationResponse;
import com.keshetong.dto.ArtifactReviewRequest;
import com.keshetong.dto.ArtifactReviewResponse;
import com.keshetong.service.ArtifactGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArtifactGenerationController {

    private final ArtifactGenerationService artifactGenerationService;

    public ArtifactGenerationController(ArtifactGenerationService artifactGenerationService) {
        this.artifactGenerationService = artifactGenerationService;
    }

    @PostMapping("/api/course_project/artifact")
    public ResponseEntity<ApiResponse<ArtifactGenerationResponse>> generateArtifact(@RequestBody ArtifactGenerationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(artifactGenerationService.generateArtifact(request)));
    }

    @PostMapping("/api/course_project/review")
    public ResponseEntity<ApiResponse<ArtifactReviewResponse>> reviewArtifact(@RequestBody ArtifactReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(artifactGenerationService.reviewArtifact(request)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
    }
}
