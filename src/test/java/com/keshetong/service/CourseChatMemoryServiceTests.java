package com.keshetong.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseChatMemoryServiceTests {

    @Test
    void shouldKeepLatestMessagesWithinLimit() {
        CourseChatMemoryService memoryService = new CourseChatMemoryService();
        String conversationId = "demo-conversation";

        memoryService.appendUserMessage(conversationId, "第一条", 3);
        memoryService.appendAssistantMessage(conversationId, "第二条", 3);
        memoryService.appendUserMessage(conversationId, "第三条", 3);
        memoryService.appendAssistantMessage(conversationId, "第四条", 3);

        List<Message> history = memoryService.getConversationHistory(conversationId);
        assertEquals(3, history.size());
        assertEquals("第二条", history.get(0).getText());
        assertEquals("第四条", history.get(2).getText());
    }
}
