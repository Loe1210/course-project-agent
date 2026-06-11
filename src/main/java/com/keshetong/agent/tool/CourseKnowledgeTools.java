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

    @Tool(description = "Use this tool to search the software course project knowledge base. " +
            "It retrieves scoring standards, report templates, design rules, example topics, defense preparation materials, " +
            "and other relevant course project documents.")
    public String queryCourseProjectKnowledge(
            @ToolParam(description = "Search query describing the course project knowledge you need") String query) {
        try {
            List<VectorSearchService.SearchResult> results =
                    vectorSearchService.searchSimilarDocuments(query, ragProperties.getTopK());
            if (results.isEmpty()) {
                return "{\"status\":\"no_results\",\"message\":\"No relevant course project knowledge found.\"}";
            }
            return objectMapper.writeValueAsString(results);
        } catch (Exception e) {
            logger.error("queryCourseProjectKnowledge failed", e);
            return String.format(
                    "{\"status\":\"error\",\"message\":\"Failed to query course project knowledge: %s\"}",
                    e.getMessage().replace("\"", "'")
            );
        }
    }
}
