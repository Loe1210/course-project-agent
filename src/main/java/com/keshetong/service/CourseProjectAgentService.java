package com.keshetong.service;

import com.keshetong.config.CourseProjectAgentProperties;
import com.keshetong.config.CourseProjectProperties;
import com.keshetong.dto.ArtifactGenerationRequest;
import com.keshetong.dto.ArtifactGenerationResponse;
import com.keshetong.dto.CourseProjectRequest;
import com.keshetong.dto.CourseProjectResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CourseProjectAgentService {

    private static final String SUPERVISOR_PROMPT = """
            你是“课设通 Agent 助手”中的 Supervisor Agent。
            你的职责是先理解用户的课程设计任务，再把任务拆成后续 Planner 和 Executor 可执行的阶段。
            
            请遵守以下要求：
            1. 全程使用中文。
            2. 输出要面向软件课程设计，不要泛化成企业项目咨询。
            3. 你本阶段只负责：明确目标、识别关键产物、拆分执行步骤、指出风险点。
            4. 输出必须包含以下小节：
               - 任务目标
               - 关键产物
               - 执行阶段
               - 风险与注意事项
            5. 不要直接生成完整课设方案，把完整方案交给后续阶段。
            """;

    private static final String PLANNER_PROMPT = """
            你是“课设通 Agent 助手”中的 Planner Agent。
            你的职责是根据用户需求和 Supervisor 的拆解结果，形成课程设计实施方案。
            
            请遵守以下要求：
            1. 全程使用中文。
            2. 可按需调用工具和知识库，优先利用现有工具生成选题、数据库、接口、项目结构、报告和答辩建议。
            3. 输出必须包含以下小节：
               - 课设定位
               - 功能模块规划
               - 技术栈建议
               - 数据库设计要点
               - 接口设计要点
               - 项目结构建议
               - 开发排期建议
            4. 输出是规划稿，不要写成最终交付成品。
            """;

    private static final String EXECUTOR_PROMPT = """
            你是“课设通 Agent 助手”中的 Executor Agent。
            你的职责是根据 Supervisor 和 Planner 的结果，生成一份可以直接拿去做课设的完整方案初稿。
            
            请遵守以下要求：
            1. 全程使用中文。
            2. 输出必须结构化、可执行、适合大学生课设落地。
            3. 最终内容至少包含以下章节：
               - 一、项目概述
               - 二、需求分析
               - 三、功能模块设计
               - 四、技术架构与技术选型
               - 五、数据库设计建议
               - 六、接口设计建议
               - 七、项目目录结构建议
               - 八、开发实施计划
               - 九、测试与验收建议
               - 十、报告与答辩准备建议
            4. 对于用户未明确说明的部分，可以给出合理默认方案，并明确写出你的假设。
            5. 如果工具产出了结构化内容，要优先吸收并整合进最终结果。
            """;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final CourseProjectAgentProperties properties;
    private final CourseProjectProperties courseProjectProperties;
    private final CourseAgentToolRegistry toolRegistry;
    private final ArtifactGenerationService artifactGenerationService;

    public CourseProjectAgentService(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                                     CourseProjectAgentProperties properties,
                                     CourseProjectProperties courseProjectProperties,
                                     CourseAgentToolRegistry toolRegistry,
                                     ArtifactGenerationService artifactGenerationService) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.properties = properties;
        this.courseProjectProperties = courseProjectProperties;
        this.toolRegistry = toolRegistry;
        this.artifactGenerationService = artifactGenerationService;
    }

    public CourseProjectResponse generateCourseProjectPlan(CourseProjectRequest request) {
        validateRequest(request);
        String requestId = resolveRequestId(request.getRequestId());
        String topic = resolveTopic(request.getTopic());
        try {
            String supervisorResult = runStage(SUPERVISOR_PROMPT, buildSupervisorUserPrompt(requestId, topic, request), false);
            String plannerResult = runStage(PLANNER_PROMPT, buildPlannerUserPrompt(requestId, topic, request, supervisorResult), shouldUseTools(request));
            String executorResult = runStage(EXECUTOR_PROMPT, buildExecutorUserPrompt(requestId, topic, request, supervisorResult, plannerResult), shouldUseTools(request));
            GeneratedArtifacts generatedArtifacts = generateArtifacts(requestId, topic, request, plannerResult, executorResult);
            return new CourseProjectResponse(
                    requestId,
                    topic,
                    supervisorResult,
                    plannerResult,
                    executorResult,
                    generatedArtifacts.databaseDesign,
                    generatedArtifacts.apiDesign,
                    generatedArtifacts.projectStructure,
                    generatedArtifacts.reportOutline,
                    generatedArtifacts.testCases,
                    generatedArtifacts.defenseQa,
                    buildSummary(topic, generatedArtifacts),
                    generatedArtifacts.failedArtifacts,
                    shouldUseTools(request)
            );
        } catch (Exception e) {
            throw new RuntimeException("生成课设任务方案失败：" + e.getMessage(), e);
        }
    }

    public Flux<String> streamCourseProjectPlan(CourseProjectRequest request) {
        validateRequest(request);
        String requestId = resolveRequestId(request.getRequestId());
        String topic = resolveTopic(request.getTopic());
        try {
            String supervisorResult = runStage(SUPERVISOR_PROMPT, buildSupervisorUserPrompt(requestId, topic, request), false);
            String plannerResult = runStage(PLANNER_PROMPT, buildPlannerUserPrompt(requestId, topic, request, supervisorResult), shouldUseTools(request));
            Flux<String> supervisorFlux = Flux.just(
                    "## Supervisor 阶段输出\n\n",
                    supervisorResult,
                    "\n\n## Planner 阶段输出\n\n",
                    plannerResult,
                    "\n\n## Executor 阶段输出\n\n"
            );
            Flux<String> executorFlux = buildRequestSpec(EXECUTOR_PROMPT,
                    buildExecutorUserPrompt(requestId, topic, request, supervisorResult, plannerResult),
                    shouldUseTools(request))
                    .stream()
                    .content();
            return Flux.concat(supervisorFlux, executorFlux);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("生成课设任务流式方案失败：" + e.getMessage(), e));
        }
    }

    public String resolveRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return properties.getDefaultRequestPrefix() + "-" + UUID.randomUUID();
        }
        return requestId.trim();
    }

    private String runStage(String systemPrompt, String userPrompt, boolean useTools) {
        return buildRequestSpec(systemPrompt, userPrompt, useTools).call().content();
    }

    private ChatClient.ChatClientRequestSpec buildRequestSpec(String systemPrompt, String userPrompt, boolean useTools) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("ChatClient.Builder 不可用，请检查 Spring AI DashScope 配置");
        }
        ChatClient.ChatClientRequestSpec requestSpec = builder.build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt);
        if (useTools) {
            requestSpec = requestSpec.tools(toolRegistry.allTools());
        }
        return requestSpec;
    }

    private boolean shouldUseTools(CourseProjectRequest request) {
        if (!properties.isEnableTools()) {
            return false;
        }
        return request.getUseTools() == null || request.getUseTools();
    }

    private String buildSupervisorUserPrompt(String requestId, String topic, CourseProjectRequest request) {
        return """
                请先对以下课设任务做总控拆解：
                
                请求编号：%s
                课设题目：%s
                方向偏好：%s
                附加要求：%s
                指定技术栈：%s
                
                请你以 Supervisor 视角判断：
                1. 这次课设最核心的交付目标是什么
                2. 应该先产出哪些关键成果
                3. 适合分成哪几个执行阶段
                4. 哪些部分最容易在课程设计中出问题
                """.formatted(
                requestId,
                topic,
                safeValue(request.getDirection()),
                safeValue(request.getRequirements()),
                safeValue(request.getTechStack())
        );
    }

    private String buildPlannerUserPrompt(String requestId, String topic, CourseProjectRequest request, String supervisorResult) {
        return """
                请基于以下课程设计任务生成规划方案，并按需调用工具：
                
                请求编号：%s
                课设题目：%s
                方向偏好：%s
                附加要求：%s
                指定技术栈：%s
                
                以下是 Supervisor 结果：
                %s
                
                你需要输出一份“规划稿”，重点是：
                - 功能模块怎么分
                - 技术栈怎么选
                - 数据库和接口从哪里切入
                - 项目结构怎么搭
                - 开发顺序怎么排
                """.formatted(
                requestId,
                topic,
                safeValue(request.getDirection()),
                safeValue(request.getRequirements()),
                safeValue(request.getTechStack()),
                supervisorResult
        );
    }

    private String buildExecutorUserPrompt(String requestId,
                                           String topic,
                                           CourseProjectRequest request,
                                           String supervisorResult,
                                           String plannerResult) {
        return """
                请根据以下信息生成一份完整的课程设计方案初稿：
                
                请求编号：%s
                课设题目：%s
                方向偏好：%s
                附加要求：%s
                指定技术栈：%s
                
                Supervisor 结果：
                %s
                
                Planner 结果：
                %s
                
                请输出一份适合大学生直接落地执行的完整课设方案，内容要详细、结构清晰、能直接用于后续实现和写报告。
                """.formatted(
                requestId,
                topic,
                safeValue(request.getDirection()),
                safeValue(request.getRequirements()),
                safeValue(request.getTechStack()),
                supervisorResult,
                plannerResult
        );
    }

    private String resolveTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return courseProjectProperties.getDefaultTopic();
        }
        return topic.trim();
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "未指定" : value.trim();
    }

    private GeneratedArtifacts generateArtifacts(String requestId,
                                                 String topic,
                                                 CourseProjectRequest request,
                                                 String plannerResult,
                                                 String executorResult) {
        GeneratedArtifacts generatedArtifacts = new GeneratedArtifacts();
        String existingPlan = plannerResult + "\n\n" + executorResult;
        generatedArtifacts.databaseDesign = tryGenerateArtifact(requestId, topic, request, existingPlan, "database_design", generatedArtifacts.failedArtifacts);
        generatedArtifacts.apiDesign = tryGenerateArtifact(requestId, topic, request, existingPlan, "api_design", generatedArtifacts.failedArtifacts);
        generatedArtifacts.projectStructure = tryGenerateArtifact(requestId, topic, request, existingPlan, "project_structure", generatedArtifacts.failedArtifacts);
        generatedArtifacts.reportOutline = tryGenerateArtifact(requestId, topic, request, existingPlan, "report_outline", generatedArtifacts.failedArtifacts);
        generatedArtifacts.testCases = tryGenerateArtifact(requestId, topic, request, existingPlan, "test_cases", generatedArtifacts.failedArtifacts);
        generatedArtifacts.defenseQa = tryGenerateArtifact(requestId, topic, request, existingPlan, "defense_qa", generatedArtifacts.failedArtifacts);
        return generatedArtifacts;
    }

    private String tryGenerateArtifact(String requestId,
                                       String topic,
                                       CourseProjectRequest request,
                                       String existingPlan,
                                       String artifactType,
                                       Map<String, String> failedArtifacts) {
        try {
            ArtifactGenerationRequest artifactRequest = new ArtifactGenerationRequest();
            artifactRequest.setRequestId(requestId + "-" + artifactType);
            artifactRequest.setTopic(topic);
            artifactRequest.setArtifactType(artifactType);
            artifactRequest.setDirection(request.getDirection());
            artifactRequest.setRequirements(request.getRequirements());
            artifactRequest.setTechStack(request.getTechStack());
            artifactRequest.setExistingPlan(existingPlan);
            ArtifactGenerationResponse response = artifactGenerationService.generateArtifact(artifactRequest);
            return response.getContent();
        } catch (Exception e) {
            failedArtifacts.put(artifactType, e.getMessage());
            return "该产物生成失败：" + e.getMessage();
        }
    }

    private String buildSummary(String topic, GeneratedArtifacts generatedArtifacts) {
        StringBuilder builder = new StringBuilder();
        builder.append("已为课设题目《").append(topic).append("》生成主方案与配套产物。");
        if (generatedArtifacts.failedArtifacts.isEmpty()) {
            builder.append(" 当前数据库设计、接口设计、项目结构、报告大纲、测试用例和答辩问答均已生成。");
        } else {
            builder.append(" 其中有部分配套产物生成失败，需要后续补齐：");
            generatedArtifacts.failedArtifacts.forEach((artifactType, error) ->
                    builder.append("[").append(artifactType).append("：").append(error).append("]"));
        }
        return builder.toString();
    }

    private void validateRequest(CourseProjectRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getTopic() == null || request.getTopic().isBlank()) {
            throw new IllegalArgumentException("课设题目不能为空");
        }
    }

    private static class GeneratedArtifacts {
        private String databaseDesign;
        private String apiDesign;
        private String projectStructure;
        private String reportOutline;
        private String testCases;
        private String defenseQa;
        private final Map<String, String> failedArtifacts = new LinkedHashMap<>();
    }
}
