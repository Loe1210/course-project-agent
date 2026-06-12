package com.keshetong.agent.tool;

import com.keshetong.service.CourseToolSupportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ReportTools {

    private final CourseToolSupportService supportService;

    public ReportTools(CourseToolSupportService supportService) {
        this.supportService = supportService;
    }

    @Tool(description = "生成课程设计报告大纲，适合直接作为论文或说明书的章节草稿。")
    public String generateCourseProjectReportOutline(
            @ToolParam(description = "课设题目名称") String topic) {
        try {
            String resolvedTopic = supportService.resolveTopic(topic);
            StringBuilder builder = new StringBuilder();
            builder.append("# 课程设计报告大纲\n\n");
            builder.append("**课设题目：** ").append(resolvedTopic).append("\n\n");
            builder.append("## 1. 项目概述\n");
            builder.append("- 项目背景\n- 设计目标\n- 开发意义\n\n");
            builder.append("## 2. 需求分析\n");
            builder.append("- 角色分析\n- 功能需求\n- 非功能需求\n\n");
            builder.append("## 3. 系统总体设计\n");
            builder.append("- 系统架构\n- 功能模块划分\n- 技术选型说明\n\n");
            builder.append("## 4. 数据库设计\n");
            builder.append("- E-R 图说明\n- 数据表设计\n- 字段与约束说明\n\n");
            builder.append("## 5. 详细设计与实现\n");
            builder.append("- 核心页面设计\n- 核心接口设计\n- 关键代码说明\n\n");
            builder.append("## 6. 系统测试\n");
            builder.append("- 测试环境\n- 测试用例\n- 测试结果分析\n\n");
            builder.append("## 7. 总结与展望\n");
            builder.append("- 项目成果总结\n- 问题与不足\n- 后续优化方向\n\n");
            builder.append("## 写作建议\n");
            builder.append("- 每一章都尽量结合截图、表格、流程图说明。\n");
            builder.append("- 重点突出你自己完成的设计决策和实现思路。\n");
            return builder.toString();
        } catch (Exception e) {
            return "生成课程设计报告大纲失败：" + e.getMessage();
        }
    }
}
