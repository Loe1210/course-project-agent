package com.keshetong.service;

import com.keshetong.agent.tool.ApiDesignTools;
import com.keshetong.agent.tool.CourseKnowledgeTools;
import com.keshetong.agent.tool.DatabaseDesignTools;
import com.keshetong.agent.tool.DateTimeTools;
import com.keshetong.agent.tool.DefenseTools;
import com.keshetong.agent.tool.ProjectTemplateTools;
import com.keshetong.agent.tool.ReportTools;
import com.keshetong.agent.tool.TopicRecommendTools;
import com.keshetong.config.CourseChatProperties;
import com.keshetong.dto.CourseChatRequest;
import com.keshetong.dto.CourseChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CourseChatService {

    private static final String SYSTEM_PROMPT = """
            你是“课设通 Agent 助手”，专门服务于大学生软件课程设计场景。
            你的职责是帮助学生完成课设相关问答、选题分析、需求梳理、数据库设计、接口设计、项目结构规划、报告写作和答辩准备。
            
            请始终遵守以下规则：
            1. 全程使用中文回答。
            2. 回答要优先贴合软件课程设计场景，避免泛泛而谈。
            3. 当用户询问评分标准、模板、设计规范、报告写法、答辩准备等内容时，优先调用知识库工具或本地工具。
            4. 当用户需要结构化产出时，优先使用清晰的小标题、列表、步骤和示例。
            5. 如果用户问题信息不足，请先基于常见课设场景给出可执行建议，再明确说明你的假设。
            6. 不要编造学校或老师的特定要求；如果知识库中没有，就明确说明是通用建议。
            7. 如果用户提问明显超出课设范围，仍尽量从软件课程设计实现角度给出帮助。
            """;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final CourseChatProperties courseChatProperties;
    private final CourseChatMemoryService memoryService;
    private final TopicRecommendTools topicRecommendTools;
    private final DatabaseDesignTools databaseDesignTools;
    private final ApiDesignTools apiDesignTools;
    private final ProjectTemplateTools projectTemplateTools;
    private final ReportTools reportTools;
    private final DefenseTools defenseTools;
    private final DateTimeTools dateTimeTools;
    private final CourseKnowledgeTools courseKnowledgeTools;

    public CourseChatService(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                             CourseChatProperties courseChatProperties,
                             CourseChatMemoryService memoryService,
                             TopicRecommendTools topicRecommendTools,
                             DatabaseDesignTools databaseDesignTools,
                             ApiDesignTools apiDesignTools,
                             ProjectTemplateTools projectTemplateTools,
                             ReportTools reportTools,
                             DefenseTools defenseTools,
                             DateTimeTools dateTimeTools,
                             CourseKnowledgeTools courseKnowledgeTools) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.courseChatProperties = courseChatProperties;
        this.memoryService = memoryService;
        this.topicRecommendTools = topicRecommendTools;
        this.databaseDesignTools = databaseDesignTools;
        this.apiDesignTools = apiDesignTools;
        this.projectTemplateTools = projectTemplateTools;
        this.reportTools = reportTools;
        this.defenseTools = defenseTools;
        this.dateTimeTools = dateTimeTools;
        this.courseKnowledgeTools = courseKnowledgeTools;
    }

    public CourseChatResponse chat(CourseChatRequest request) {
        validateRequest(request);
        String conversationId = resolveConversationId(request.getConversationId());
        memoryService.appendUserMessage(conversationId, request.getMessage(), courseChatProperties.getMaxHistoryMessages());
        try {
            String answer = buildRequestSpec(conversationId, request).call().content();
            memoryService.appendAssistantMessage(conversationId, answer, courseChatProperties.getMaxHistoryMessages());
            return new CourseChatResponse(conversationId, answer, shouldUseTools(request));
        } catch (Exception e) {
            throw new RuntimeException("课设对话调用失败：" + e.getMessage(), e);
        }
    }

    public Flux<String> streamChat(CourseChatRequest request) {
        validateRequest(request);
        String conversationId = resolveConversationId(request.getConversationId());
        memoryService.appendUserMessage(conversationId, request.getMessage(), courseChatProperties.getMaxHistoryMessages());
        AtomicReference<StringBuilder> aggregatedAnswer = new AtomicReference<>(new StringBuilder());
        try {
            return buildRequestSpec(conversationId, request)
                    .stream()
                    .content()
                    .doOnNext(chunk -> aggregatedAnswer.get().append(chunk))
                    .doOnComplete(() -> memoryService.appendAssistantMessage(
                            conversationId,
                            aggregatedAnswer.get().toString(),
                            courseChatProperties.getMaxHistoryMessages()
                    ))
                    .doOnError(error -> memoryService.clearConversation(conversationId));
        } catch (Exception e) {
            memoryService.clearConversation(conversationId);
            return Flux.error(new RuntimeException("课设流式对话调用失败：" + e.getMessage(), e));
        }
    }

    public String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return courseChatProperties.getDefaultConversationPrefix() + "-" + UUID.randomUUID();
        }
        return conversationId.trim();
    }

    private ChatClient.ChatClientRequestSpec buildRequestSpec(String conversationId, CourseChatRequest request) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("ChatClient.Builder 不可用，请检查 Spring AI DashScope 配置");
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));
        List<Message> history = memoryService.getConversationHistory(conversationId);
        if (!history.isEmpty()) {
            messages.addAll(history);
        }

        ChatClient.ChatClientRequestSpec requestSpec = builder.build()
                .prompt()
                .messages(messages);

        if (shouldUseTools(request)) {
            requestSpec = requestSpec.tools(
                    topicRecommendTools,
                    databaseDesignTools,
                    apiDesignTools,
                    projectTemplateTools,
                    reportTools,
                    defenseTools,
                    dateTimeTools,
                    courseKnowledgeTools
            );
        }

        return requestSpec;
    }

    private boolean shouldUseTools(CourseChatRequest request) {
        if (!courseChatProperties.isEnableTools()) {
            return false;
        }
        return request.getUseTools() == null || request.getUseTools();
    }

    private void validateRequest(CourseChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("用户消息不能为空");
        }
    }
}
