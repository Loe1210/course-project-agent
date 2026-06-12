package com.keshetong.agent.tool;

import com.keshetong.service.CourseToolSupportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefenseTools {

    private final CourseToolSupportService supportService;

    public DefenseTools(CourseToolSupportService supportService) {
        this.supportService = supportService;
    }

    @Tool(description = "生成课程设计答辩准备内容，包括常见问题、参考回答方向和展示建议。")
    public String generateDefensePreparation(
            @ToolParam(description = "课设题目名称") String topic,
            @ToolParam(description = "希望强调的答辩重点，例如数据库设计、接口实现、前端页面") String focus) {
        try {
            String resolvedTopic = supportService.resolveTopic(topic);
            String finalFocus = focus == null || focus.isBlank() ? "系统设计与实现" : focus.trim();
            List<String> modules = supportService.inferModulesFromTopic(resolvedTopic);
            StringBuilder builder = new StringBuilder();
            builder.append("# 课设答辩准备\n\n");
            builder.append("**课设题目：** ").append(resolvedTopic).append("\n");
            builder.append("**重点方向：** ").append(finalFocus).append("\n\n");
            builder.append("## 常见答辩问题\n");
            builder.append("1. 你为什么选择这个题目？\n");
            builder.append("2. 系统解决了什么实际问题？\n");
            builder.append("3. 你的功能模块是如何划分的？\n");
            builder.append("4. 数据库为什么这样设计？\n");
            builder.append("5. 系统中最有难度的部分是什么？你如何解决？\n");
            builder.append("6. 如果继续优化，你还会补充哪些功能？\n\n");
            builder.append("## 回答思路\n");
            builder.append("- 围绕 `需求来源 -> 模块划分 -> 技术实现 -> 效果验证` 的顺序组织表达。\n");
            builder.append("- 对模块划分可以重点讲：").append(String.join("、", modules)).append("。\n");
            builder.append("- 对难点回答建议落到具体实现，例如权限控制、分页检索、表关系设计、接口联调。\n\n");
            builder.append("## 展示建议\n");
            builder.append("- 演示时优先展示完整主流程，不要一开始就讲代码。\n");
            builder.append("- 准备 2 到 3 张关键设计图，如系统架构图、E-R 图、功能结构图。\n");
            builder.append("- 回答老师问题时尽量结合你自己的实现细节，不要只讲概念。\n");
            return builder.toString();
        } catch (Exception e) {
            return "生成答辩准备内容失败：" + e.getMessage();
        }
    }
}
