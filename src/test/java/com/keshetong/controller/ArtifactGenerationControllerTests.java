package com.keshetong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keshetong.dto.ArtifactGenerationRequest;
import com.keshetong.dto.ArtifactGenerationResponse;
import com.keshetong.dto.ArtifactReviewRequest;
import com.keshetong.dto.ArtifactReviewResponse;
import com.keshetong.service.ArtifactGenerationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArtifactGenerationController.class)
class ArtifactGenerationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArtifactGenerationService artifactGenerationService;

    @Test
    void shouldGenerateArtifact() throws Exception {
        ArtifactGenerationRequest request = new ArtifactGenerationRequest();
        request.setRequestId("artifact-001");
        request.setTopic("学生成绩管理系统");
        request.setArtifactType("database_design");

        Mockito.when(artifactGenerationService.generateArtifact(Mockito.any(ArtifactGenerationRequest.class)))
                .thenReturn(new ArtifactGenerationResponse("artifact-001", "学生成绩管理系统", "database_design", "数据库内容"));

        mockMvc.perform(post("/api/course_project/artifact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.artifactType").value("database_design"))
                .andExpect(jsonPath("$.data.content").value("数据库内容"));
    }

    @Test
    void shouldReviewArtifact() throws Exception {
        ArtifactReviewRequest request = new ArtifactReviewRequest();
        request.setRequestId("review-001");
        request.setTopic("学生成绩管理系统");
        request.setContent("这里有需求分析和数据库设计");
        request.setReviewType("completeness");

        Mockito.when(artifactGenerationService.reviewArtifact(Mockito.any(ArtifactReviewRequest.class)))
                .thenReturn(new ArtifactReviewResponse("review-001", "学生成绩管理系统", "completeness", "审查结果"));

        mockMvc.perform(post("/api/course_project/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reviewType").value("completeness"));
    }
}
