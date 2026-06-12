package com.keshetong.service;

import com.keshetong.config.CourseProjectAgentProperties;
import com.keshetong.config.CourseProjectProperties;
import com.keshetong.dto.CourseProjectRequest;
import com.keshetong.agent.tool.ApiDesignTools;
import com.keshetong.agent.tool.DatabaseDesignTools;
import com.keshetong.agent.tool.DefenseTools;
import com.keshetong.agent.tool.ProjectTemplateTools;
import com.keshetong.agent.tool.ReportTools;
import com.keshetong.config.ArtifactGenerationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourseProjectAgentServiceTests {

    @Test
    void shouldGenerateRequestIdWhenMissing() {
        CourseProjectAgentProperties properties = new CourseProjectAgentProperties();
        properties.setDefaultRequestPrefix("course-project");
        CourseProjectProperties projectProperties = new CourseProjectProperties();
        CourseProjectAgentService service = new CourseProjectAgentService(
                emptyProvider(),
                properties,
                projectProperties,
                new CourseAgentToolRegistry(null, null, null, null, null, null, null, null),
                createArtifactGenerationService(projectProperties)
        );
        String requestId = service.resolveRequestId(null);
        assertEquals(true, requestId.startsWith("course-project-"));
    }

    @Test
    void shouldRejectEmptyTopic() {
        CourseProjectAgentProperties properties = new CourseProjectAgentProperties();
        CourseProjectProperties projectProperties = new CourseProjectProperties();
        CourseProjectAgentService service = new CourseProjectAgentService(
                emptyProvider(),
                properties,
                projectProperties,
                new CourseAgentToolRegistry(null, null, null, null, null, null, null, null),
                createArtifactGenerationService(projectProperties)
        );
        CourseProjectRequest request = new CourseProjectRequest();
        request.setTopic("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generateCourseProjectPlan(request));
        assertEquals("课设题目不能为空", exception.getMessage());
    }

    private ArtifactGenerationService createArtifactGenerationService(CourseProjectProperties projectProperties) {
        CourseToolSupportService supportService = new CourseToolSupportService(projectProperties);
        ArtifactGenerationProperties artifactProperties = new ArtifactGenerationProperties();
        artifactProperties.setEnableLlmDraft(false);
        return new ArtifactGenerationService(
                emptyProvider(),
                artifactProperties,
                projectProperties,
                new DatabaseDesignTools(supportService),
                new ApiDesignTools(supportService),
                new ProjectTemplateTools(supportService),
                new ReportTools(supportService),
                new DefenseTools(supportService)
        );
    }

    private ObjectProvider<ChatClient.Builder> emptyProvider() {
        return new ObjectProvider<>() {
            @Override
            public ChatClient.Builder getObject(Object... args) {
                return null;
            }

            @Override
            public ChatClient.Builder getIfAvailable() {
                return null;
            }

            @Override
            public ChatClient.Builder getIfUnique() {
                return null;
            }

            @Override
            public ChatClient.Builder getObject() {
                return null;
            }
        };
    }
}
