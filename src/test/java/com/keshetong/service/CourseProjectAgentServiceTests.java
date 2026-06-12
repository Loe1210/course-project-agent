package com.keshetong.service;

import com.keshetong.config.CourseProjectAgentProperties;
import com.keshetong.config.CourseProjectProperties;
import com.keshetong.dto.CourseProjectRequest;
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
                new CourseAgentToolRegistry(null, null, null, null, null, null, null, null)
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
                new CourseAgentToolRegistry(null, null, null, null, null, null, null, null)
        );
        CourseProjectRequest request = new CourseProjectRequest();
        request.setTopic("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generateCourseProjectPlan(request));
        assertEquals("课设题目不能为空", exception.getMessage());
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
