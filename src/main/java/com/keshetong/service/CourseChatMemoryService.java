package com.keshetong.service;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CourseChatMemoryService {

    private final Map<String, List<Message>> conversationStore = new ConcurrentHashMap<>();

    public List<Message> getConversationHistory(String conversationId) {
        return new ArrayList<>(conversationStore.getOrDefault(conversationId, List.of()));
    }

    public void appendUserMessage(String conversationId, String content, int maxMessages) {
        appendMessage(conversationId, new UserMessage(content), maxMessages);
    }

    public void appendAssistantMessage(String conversationId, String content, int maxMessages) {
        appendMessage(conversationId, new AssistantMessage(content), maxMessages);
    }

    public void clearConversation(String conversationId) {
        conversationStore.remove(conversationId);
    }

    private void appendMessage(String conversationId, Message message, int maxMessages) {
        conversationStore.compute(conversationId, (key, history) -> {
            List<Message> updatedHistory = history == null ? new ArrayList<>() : new ArrayList<>(history);
            updatedHistory.add(message);
            if (updatedHistory.size() > maxMessages) {
                int start = updatedHistory.size() - maxMessages;
                updatedHistory = new ArrayList<>(updatedHistory.subList(start, updatedHistory.size()));
            }
            return updatedHistory;
        });
    }
}
