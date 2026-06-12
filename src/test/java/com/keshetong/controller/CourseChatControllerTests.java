package com.keshetong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keshetong.dto.CourseChatRequest;
import com.keshetong.dto.CourseChatResponse;
import com.keshetong.service.CourseChatService;
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

@WebMvcTest(CourseChatController.class)
class CourseChatControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseChatService courseChatService;

    @Test
    void shouldReturnChatResponse() throws Exception {
        CourseChatRequest request = new CourseChatRequest();
        request.setConversationId("chat-001");
        request.setMessage("帮我规划一个学生成绩管理系统");
        request.setUseTools(true);

        Mockito.when(courseChatService.chat(Mockito.any(CourseChatRequest.class)))
                .thenReturn(new CourseChatResponse("chat-001", "这里是回答内容", true));

        mockMvc.perform(post("/api/course_chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.conversationId").value("chat-001"))
                .andExpect(jsonPath("$.data.answer").value("这里是回答内容"))
                .andExpect(jsonPath("$.data.usedTools").value(true));
    }

    @Test
    void shouldHandleIllegalArgumentException() throws Exception {
        CourseChatRequest request = new CourseChatRequest();
        request.setMessage("");

        Mockito.when(courseChatService.chat(Mockito.any(CourseChatRequest.class)))
                .thenThrow(new IllegalArgumentException("用户消息不能为空"));

        mockMvc.perform(post("/api/course_chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户消息不能为空"));
    }
}
