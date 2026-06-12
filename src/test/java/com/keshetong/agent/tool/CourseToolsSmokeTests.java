package com.keshetong.agent.tool;

import com.keshetong.config.CourseProjectProperties;
import com.keshetong.service.CourseToolSupportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseToolsSmokeTests {

    private TopicRecommendTools topicRecommendTools;
    private DatabaseDesignTools databaseDesignTools;
    private ApiDesignTools apiDesignTools;
    private ProjectTemplateTools projectTemplateTools;
    private ReportTools reportTools;
    private DefenseTools defenseTools;
    private DateTimeTools dateTimeTools;

    @BeforeEach
    void setUp() {
        CourseProjectProperties properties = new CourseProjectProperties();
        properties.setDefaultTopic("校园二手交易平台");
        properties.setDefaultTechStack(List.of("Spring Boot", "Vue", "MySQL"));
        CourseToolSupportService supportService = new CourseToolSupportService(properties);
        topicRecommendTools = new TopicRecommendTools(supportService);
        databaseDesignTools = new DatabaseDesignTools(supportService);
        apiDesignTools = new ApiDesignTools(supportService);
        projectTemplateTools = new ProjectTemplateTools(supportService);
        reportTools = new ReportTools(supportService);
        defenseTools = new DefenseTools(supportService);
        dateTimeTools = new DateTimeTools();
    }

    @Test
    void shouldGenerateTopicRecommendation() {
        String result = topicRecommendTools.recommendCourseProjectTopics("Java Web", 3);
        assertTrue(result.contains("课设选题推荐"));
        assertTrue(result.contains("校园二手交易平台"));
    }

    @Test
    void shouldGenerateDatabaseAndApiDesign() {
        String databaseResult = databaseDesignTools.designCourseProjectDatabase("学生成绩管理系统", "");
        String apiResult = apiDesignTools.designCourseProjectApis("学生成绩管理系统", "学生信息管理,成绩信息管理");
        assertTrue(databaseResult.contains("数据库设计建议"));
        assertTrue(databaseResult.contains("student"));
        assertTrue(apiResult.contains("接口设计草案"));
        assertTrue(apiResult.contains("/api/学生/list"));
    }

    @Test
    void shouldGenerateTemplateReportAndDefenseContent() {
        String structureResult = projectTemplateTools.generateProjectStructure("校园二手交易平台", "Spring Boot,Vue,MySQL");
        String reportResult = reportTools.generateCourseProjectReportOutline("校园二手交易平台");
        String defenseResult = defenseTools.generateDefensePreparation("校园二手交易平台", "数据库设计");
        assertTrue(structureResult.contains("项目目录结构建议"));
        assertTrue(structureResult.contains("backend/"));
        assertTrue(reportResult.contains("课程设计报告大纲"));
        assertTrue(defenseResult.contains("课设答辩准备"));
    }

    @Test
    void shouldReturnCurrentDateTime() {
        String result = dateTimeTools.getCurrentDateTime();
        assertTrue(result.contains("年"));
        assertTrue(result.contains(":"));
    }
}
