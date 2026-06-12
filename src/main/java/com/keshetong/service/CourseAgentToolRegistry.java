package com.keshetong.service;

import com.keshetong.agent.tool.ApiDesignTools;
import com.keshetong.agent.tool.CourseKnowledgeTools;
import com.keshetong.agent.tool.DatabaseDesignTools;
import com.keshetong.agent.tool.DateTimeTools;
import com.keshetong.agent.tool.DefenseTools;
import com.keshetong.agent.tool.ProjectTemplateTools;
import com.keshetong.agent.tool.ReportTools;
import com.keshetong.agent.tool.TopicRecommendTools;
import org.springframework.stereotype.Service;

@Service
public class CourseAgentToolRegistry {

    private final Object[] allTools;

    public CourseAgentToolRegistry(TopicRecommendTools topicRecommendTools,
                                   DatabaseDesignTools databaseDesignTools,
                                   ApiDesignTools apiDesignTools,
                                   ProjectTemplateTools projectTemplateTools,
                                   ReportTools reportTools,
                                   DefenseTools defenseTools,
                                   DateTimeTools dateTimeTools,
                                   CourseKnowledgeTools courseKnowledgeTools) {
        this.allTools = new Object[]{
                topicRecommendTools,
                databaseDesignTools,
                apiDesignTools,
                projectTemplateTools,
                reportTools,
                defenseTools,
                dateTimeTools,
                courseKnowledgeTools
        };
    }

    public Object[] allTools() {
        return allTools.clone();
    }
}
