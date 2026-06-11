package com.keshetong.agent.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keshetong.config.RagProperties;
import com.keshetong.service.VectorSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseKnowledgeTools {

    public static final String TOOL_QUERY_COURSE_PROJECT_KNOWLEDGE = "queryCourseProjectKnowledge";

    private static final Logger logger = LoggerFactory.getLogger(CourseKnowledgeTools.class);

    private final VectorSearchService vectorSearchService;
    private final RagProperties ragProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CourseKnowledgeTools(VectorSearchService vectorSearchService, RagProperties ragProperties) {
        this.vectorSearchService = vectorSearchService;
        this.ragProperties = ragProperties;
    }

    @Tool(description = "使用该工具检索软件课程设计知识库。" +
            "可获取评分标准、报告模板、设计规范、选题示例、答辩准备资料等相关课程设计文档。")
    public String queryCourseProjectKnowledge(
            @ToolParam(description = "用于描述你需要查询的课程设计知识内容") String query) {
        try {
            List<VectorSearchService.SearchResult> results =
                    vectorSearchService.searchSimilarDocuments(query, ragProperties.getTopK());
            if (results.isEmpty()) {
                return "{\"status\":\"no_results\",\"message\":\"未检索到相关课程设计知识。\"}";
            }
            return objectMapper.writeValueAsString(results);
        } catch (Exception e) {
            logger.error("查询课程设计知识库失败", e);
            return String.format(
                    "{\"status\":\"error\",\"message\":\"查询课程设计知识库失败：%s\"}",
                    e.getMessage().replace("\"", "'")
            );
        }
    }
}
