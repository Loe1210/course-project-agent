package com.keshetong.service;

import com.keshetong.agent.tool.ApiDesignTools;
import com.keshetong.agent.tool.DatabaseDesignTools;
import com.keshetong.agent.tool.DefenseTools;
import com.keshetong.agent.tool.ProjectTemplateTools;
import com.keshetong.agent.tool.ReportTools;
import com.keshetong.config.ArtifactGenerationProperties;
import com.keshetong.config.CourseProjectProperties;
import com.keshetong.dto.ArtifactGenerationRequest;
import com.keshetong.dto.ArtifactGenerationResponse;
import com.keshetong.dto.ArtifactReviewRequest;
import com.keshetong.dto.ArtifactReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactGenerationServiceTests {

    private ArtifactGenerationService artifactGenerationService;

    @BeforeEach
    void setUp() {
        CourseProjectProperties projectProperties = new CourseProjectProperties();
        projectProperties.setDefaultTopic("校园二手交易平台");
        CourseToolSupportService supportService = new CourseToolSupportService(projectProperties);
        artifactGenerationService = new ArtifactGenerationService(
                emptyProvider(),
                artifactProperties(),
                projectProperties,
                new DatabaseDesignTools(supportService),
                new ApiDesignTools(supportService),
                new ProjectTemplateTools(supportService),
                new ReportTools(supportService),
                new DefenseTools(supportService)
        );
    }

    @Test
    void shouldGenerateDatabaseArtifact() {
        ArtifactGenerationRequest request = new ArtifactGenerationRequest();
        request.setTopic("学生成绩管理系统");
        request.setArtifactType("database_design");
        ArtifactGenerationResponse response = artifactGenerationService.generateArtifact(request);
        assertEquals("database_design", response.getArtifactType());
        assertTrue(response.getContent().contains("数据库设计建议"));
    }

    @Test
    void shouldGenerateCompletenessReview() {
        ArtifactReviewRequest request = new ArtifactReviewRequest();
        request.setTopic("学生成绩管理系统");
        request.setContent("这里包含需求分析、数据库设计、接口设计和测试用例");
        ArtifactReviewResponse response = artifactGenerationService.reviewArtifact(request);
        assertEquals("completeness", response.getReviewType());
        assertTrue(response.getReviewResult().contains("课设完整性审查"));
    }

    @Test
    void shouldRejectUnknownArtifactType() {
        ArtifactGenerationRequest request = new ArtifactGenerationRequest();
        request.setTopic("学生成绩管理系统");
        request.setArtifactType("unknown");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> artifactGenerationService.generateArtifact(request));
        assertEquals("不支持的产物类型：unknown", exception.getMessage());
    }

    private ArtifactGenerationProperties artifactProperties() {
        ArtifactGenerationProperties properties = new ArtifactGenerationProperties();
        properties.setEnableLlmDraft(false);
        return properties;
    }

    private ObjectProvider<org.springframework.ai.chat.client.ChatClient.Builder> emptyProvider() {
        return new ObjectProvider<>() {
            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getObject(Object... args) {
                return null;
            }

            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getIfAvailable() {
                return null;
            }

            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getIfUnique() {
                return null;
            }

            @Override
            public org.springframework.ai.chat.client.ChatClient.Builder getObject() {
                return null;
            }
        };
    }
}
