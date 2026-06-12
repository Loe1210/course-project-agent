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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class ArtifactGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactGenerationService.class);

    private static final String REPORT_DRAFT_PROMPT = """
            你是“课设通 Agent 助手”的产物生成 Agent。
            你的职责是根据用户提供的课设题目、要求和已有规划，生成一份适合大学生软件课程设计提交的报告正文初稿。

            请遵守以下规则：
            1. 全程使用中文。
            2. 输出应贴合软件课程设计场景。
            3. 若已有规划内容，请尽量吸收其中的模块、技术栈、数据库和接口设计建议。
            4. 报告初稿至少包含：项目背景、需求分析、总体设计、数据库设计、核心实现、测试方案、总结展望。
            5. 不要只列目录，要给出可直接继续修改的正文草稿。
            """;

    private static final String REVIEW_PROMPT = """
            你是“课设通 Agent 助手”的课设评审 Agent。
            你的职责是审查一份课程设计方案或单个产物是否完整、是否适合答辩展示，并给出修改建议。

            请遵守以下规则：
            1. 全程使用中文。
            2. 输出必须包含：总体评价、已具备内容、缺失项、风险点、修改建议。
            3. 重点从课程设计常见要求出发：需求、功能、数据库、接口、测试、报告、答辩准备。
            4. 若用户内容明显不足，要明确指出缺失部分，不要模糊带过。
            """;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ArtifactGenerationProperties properties;
    private final CourseProjectProperties courseProjectProperties;
    private final DatabaseDesignTools databaseDesignTools;
    private final ApiDesignTools apiDesignTools;
    private final ProjectTemplateTools projectTemplateTools;
    private final ReportTools reportTools;
    private final DefenseTools defenseTools;
    private final DocumentGateway documentGateway;

    public ArtifactGenerationService(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                                     ArtifactGenerationProperties properties,
                                     CourseProjectProperties courseProjectProperties,
                                     DatabaseDesignTools databaseDesignTools,
                                     ApiDesignTools apiDesignTools,
                                     ProjectTemplateTools projectTemplateTools,
                                     ReportTools reportTools,
                                     DefenseTools defenseTools,
                                     DocumentGateway documentGateway) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.properties = properties;
        this.courseProjectProperties = courseProjectProperties;
        this.databaseDesignTools = databaseDesignTools;
        this.apiDesignTools = apiDesignTools;
        this.projectTemplateTools = projectTemplateTools;
        this.reportTools = reportTools;
        this.defenseTools = defenseTools;
        this.documentGateway = documentGateway;
    }

    public ArtifactGenerationResponse generateArtifact(ArtifactGenerationRequest request) {
        validateArtifactRequest(request);
        String requestId = resolveRequestId(request.getRequestId());
        String topic = resolveTopic(request.getTopic());
        String artifactType = normalizeArtifactType(request.getArtifactType());

        String content = switch (artifactType) {
            case "database_design" -> databaseDesignTools.designCourseProjectDatabase(topic, request.getRequirements());
            case "api_design" -> apiDesignTools.designCourseProjectApis(topic, null);
            case "project_structure" -> projectTemplateTools.generateProjectStructure(topic, request.getTechStack());
            case "report_outline" -> reportTools.generateCourseProjectReportOutline(topic);
            case "report_draft" -> generateReportDraft(topic, request);
            case "test_cases" -> generateTestCases(topic, request);
            case "defense_qa" -> defenseTools.generateDefensePreparation(topic, "答辩准备");
            default -> throw new IllegalArgumentException("不支持的产物类型：" + request.getArtifactType());
        };
        DocumentGateway.ExportedDocumentResult exportedDocument = null;
        if (artifactType.startsWith("report_")) {
            exportedDocument = tryExportReport(topic, artifactType, content);
        }
        if (exportedDocument == null) {
            return new ArtifactGenerationResponse(requestId, topic, artifactType, content);
        }
        return new ArtifactGenerationResponse(
                requestId,
                topic,
                artifactType,
                content,
                exportedDocument.fileName(),
                exportedDocument.filePath(),
                exportedDocument.downloadUrl()
        );
    }

    public ArtifactReviewResponse reviewArtifact(ArtifactReviewRequest request) {
        validateReviewRequest(request);
        String requestId = resolveRequestId(request.getRequestId());
        String topic = resolveTopic(request.getTopic());
        String reviewType = normalizeReviewType(request.getReviewType());
        String reviewResult = switch (reviewType) {
            case "completeness" -> reviewWithHeuristics(topic, request.getContent());
            case "llm_review" -> reviewWithLlm(topic, request.getContent());
            default -> throw new IllegalArgumentException("不支持的审查类型：" + request.getReviewType());
        };
        return new ArtifactReviewResponse(requestId, topic, reviewType, reviewResult);
    }

    public String resolveRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return "artifact-" + UUID.randomUUID();
        }
        return requestId.trim();
    }

    private String generateReportDraft(String topic, ArtifactGenerationRequest request) {
        if (!properties.isEnableLlmDraft()) {
            return """
                    # 课程设计报告正文初稿

                    当前已关闭大模型报告初稿生成能力，请先开启 `artifact-generation.enable-llm-draft` 后重试。
                    """;
        }
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("ChatClient.Builder 不可用，请检查 Spring AI DashScope 配置");
        }
        String userPrompt = """
                请为以下课程设计生成报告正文初稿：

                课设题目：%s
                方向偏好：%s
                附加要求：%s
                指定技术栈：%s
                已有规划：%s
                """.formatted(
                topic,
                safeValue(request.getDirection()),
                safeValue(request.getRequirements()),
                safeValue(request.getTechStack()),
                safeValue(request.getExistingPlan())
        );
        return builder.build()
                .prompt()
                .system(REPORT_DRAFT_PROMPT)
                .user(userPrompt)
                .call()
                .content();
    }

    private String generateTestCases(String topic, ArtifactGenerationRequest request) {
        return """
                # 测试用例设计

                **课设题目：** %s

                ## 一、测试目标
                - 验证核心业务流程是否可用
                - 验证输入校验、异常处理和权限控制是否正确
                - 验证页面与接口联调结果是否符合预期

                ## 二、建议测试范围
                - 用户登录与身份校验
                - 核心业务新增、查询、修改、删除
                - 列表分页与条件筛选
                - 异常输入与边界值场景
                - 数据一致性与状态流转

                ## 三、测试用例示例
                | 编号 | 测试场景 | 前置条件 | 操作步骤 | 预期结果 |
                |---|---|---|---|---|
                | TC-01 | 正常登录 | 已存在有效账号 | 输入正确账号密码并登录 | 登录成功，跳转系统首页 |
                | TC-02 | 空参数提交 | 打开新增页面 | 必填项为空直接提交 | 页面提示必填项不能为空 |
                | TC-03 | 新增业务数据 | 已登录系统 | 填写表单并提交 | 新增成功，列表可查询 |
                | TC-04 | 修改业务数据 | 已存在一条测试数据 | 修改关键字段并保存 | 修改成功，详情页信息更新 |
                | TC-05 | 删除业务数据 | 已存在一条测试数据 | 点击删除并确认 | 删除成功，列表中不再显示 |
                | TC-06 | 条件查询 | 已存在多条测试数据 | 输入关键词和筛选条件 | 返回符合条件的数据 |

                ## 四、测试建议
                - 结合你的课设模块，再补充至少 8 到 12 条业务测试用例。
                - 报告中建议附上测试截图和测试结果分析。
                - 若题目涉及状态流转，请单独增加流程型测试用例。
                """.formatted(topic);
    }

    private String reviewWithHeuristics(String topic, String content) {
        String normalized = content == null ? "" : content;
        boolean hasDemand = containsAny(normalized, "需求", "功能需求", "需求分析");
        boolean hasDatabase = containsAny(normalized, "数据库", "数据表", "E-R", "ER图");
        boolean hasApi = containsAny(normalized, "接口", "API", "/api/");
        boolean hasTest = containsAny(normalized, "测试", "测试用例", "验收");
        boolean hasReport = containsAny(normalized, "报告", "总结", "说明书");
        boolean hasDefense = containsAny(normalized, "答辩", "演示", "问题准备");

        StringBuilder builder = new StringBuilder();
        builder.append("# 课设完整性审查\n\n");
        builder.append("**课设题目：** ").append(topic).append("\n\n");
        builder.append("## 总体评价\n");
        int score = countTrue(hasDemand, hasDatabase, hasApi, hasTest, hasReport, hasDefense);
        if (score >= 5) {
            builder.append("- 当前方案完整度较高，已经覆盖大部分课设答辩所需材料。\n");
        } else if (score >= 3) {
            builder.append("- 当前方案具备基础框架，但仍有若干关键内容需要补全。\n");
        } else {
            builder.append("- 当前方案还比较粗略，距离可提交或可答辩状态还有明显差距。\n");
        }
        builder.append("\n## 已具备内容\n");
        appendItem(builder, hasDemand, "已包含需求分析或功能需求说明");
        appendItem(builder, hasDatabase, "已包含数据库设计相关内容");
        appendItem(builder, hasApi, "已包含接口设计或接口示例");
        appendItem(builder, hasTest, "已包含测试或验收内容");
        appendItem(builder, hasReport, "已包含报告写作相关内容");
        appendItem(builder, hasDefense, "已包含答辩准备相关内容");

        builder.append("\n## 缺失项\n");
        appendMissing(builder, !hasDemand, "补充需求分析和功能模块划分");
        appendMissing(builder, !hasDatabase, "补充数据库设计、数据表说明或 E-R 图");
        appendMissing(builder, !hasApi, "补充接口设计、请求响应说明和联调示例");
        appendMissing(builder, !hasTest, "补充测试用例、测试结果与验收标准");
        appendMissing(builder, !hasReport, "补充报告目录、正文初稿或总结部分");
        appendMissing(builder, !hasDefense, "补充答辩问题准备和演示讲解顺序");

        builder.append("\n## 风险点\n");
        builder.append("- 若数据库和接口设计不完整，后续实现和答辩都容易卡住。\n");
        builder.append("- 若测试内容不足，老师通常会质疑系统是否真正跑通过。\n");
        builder.append("- 若报告与答辩准备缺失，即使功能完成也会影响最终展示效果。\n");

        builder.append("\n## 修改建议\n");
        builder.append("- 先补数据库设计和接口设计，这两部分最能支撑后续实现。\n");
        builder.append("- 再补测试用例和报告大纲，形成可提交材料闭环。\n");
        builder.append("- 最后准备答辩问题和演示流程，确保展示节奏完整。\n");
        return builder.toString();
    }

    private String reviewWithLlm(String topic, String content) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("ChatClient.Builder 不可用，请检查 Spring AI DashScope 配置");
        }
        String userPrompt = """
                请审查以下课程设计内容：

                课设题目：%s
                内容正文：
                %s
                """.formatted(topic, safeValue(content));
        return builder.build()
                .prompt()
                .system(REVIEW_PROMPT)
                .user(userPrompt)
                .call()
                .content();
    }

    private DocumentGateway.ExportedDocumentResult tryExportReport(String topic, String artifactType, String content) {
        try {
            String title = "课设报告导出 - " + topic;
            return documentGateway.exportReportDocx(topic, artifactType, title, content);
        } catch (Exception e) {
            logger.warn("导出 Word 报告失败：{}", e.getMessage());
            return null;
        }
    }

    private String resolveTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return courseProjectProperties.getDefaultTopic();
        }
        return topic.trim();
    }

    private String normalizeArtifactType(String artifactType) {
        if (artifactType == null || artifactType.isBlank()) {
            throw new IllegalArgumentException("产物类型不能为空");
        }
        return artifactType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeReviewType(String reviewType) {
        if (reviewType == null || reviewType.isBlank()) {
            return "completeness";
        }
        return reviewType.trim().toLowerCase(Locale.ROOT);
    }

    private void validateArtifactRequest(ArtifactGenerationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getTopic() == null || request.getTopic().isBlank()) {
            throw new IllegalArgumentException("课设题目不能为空");
        }
        if (request.getArtifactType() == null || request.getArtifactType().isBlank()) {
            throw new IllegalArgumentException("产物类型不能为空");
        }
    }

    private void validateReviewRequest(ArtifactReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getTopic() == null || request.getTopic().isBlank()) {
            throw new IllegalArgumentException("课设题目不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("待审查内容不能为空");
        }
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "未提供" : value.trim();
    }

    private boolean containsAny(String content, String... keywords) {
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int countTrue(boolean... values) {
        int count = 0;
        for (boolean value : values) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    private void appendItem(StringBuilder builder, boolean condition, String text) {
        if (condition) {
            builder.append("- ").append(text).append("\n");
        }
    }

    private void appendMissing(StringBuilder builder, boolean condition, String text) {
        if (condition) {
            builder.append("- ").append(text).append("\n");
        }
    }
}
