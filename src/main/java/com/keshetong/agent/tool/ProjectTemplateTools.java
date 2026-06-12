package com.keshetong.agent.tool;

import com.keshetong.service.CourseToolSupportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectTemplateTools {

    private final CourseToolSupportService supportService;

    public ProjectTemplateTools(CourseToolSupportService supportService) {
        this.supportService = supportService;
    }

    @Tool(description = "生成课程设计项目的目录结构建议，适合用作代码骨架和工程初始化参考。")
    public String generateProjectStructure(
            @ToolParam(description = "课设题目名称") String topic,
            @ToolParam(description = "技术栈，多个技术名可用逗号分隔，例如 Spring Boot, Vue, MySQL") String techStack) {
        try {
            String resolvedTopic = supportService.resolveTopic(topic);
            List<String> stacks = supportService.resolveTechStack(techStack);
            StringBuilder builder = new StringBuilder();
            builder.append("# 项目目录结构建议\n\n");
            builder.append("**课设题目：** ").append(resolvedTopic).append("\n");
            builder.append("**推荐技术栈：** ").append(String.join(" / ", stacks)).append("\n\n");
            builder.append("```text\n");
            builder.append("course-project/\n");
            builder.append("├─ docs/\n");
            builder.append("│  ├─ 开题材料/\n");
            builder.append("│  ├─ 数据库设计/\n");
            builder.append("│  ├─ 接口文档/\n");
            builder.append("│  └─ 课设报告/\n");
            builder.append("├─ backend/\n");
            builder.append("│  └─ src/main/java/com/example/\n");
            builder.append("│     ├─ controller/\n");
            builder.append("│     ├─ service/\n");
            builder.append("│     ├─ mapper/\n");
            builder.append("│     ├─ entity/\n");
            builder.append("│     ├─ dto/\n");
            builder.append("│     └─ config/\n");
            builder.append("├─ frontend/\n");
            builder.append("│  ├─ src/api/\n");
            builder.append("│  ├─ src/views/\n");
            builder.append("│  ├─ src/components/\n");
            builder.append("│  └─ src/router/\n");
            builder.append("├─ sql/\n");
            builder.append("│  ├─ schema.sql\n");
            builder.append("│  └─ init_data.sql\n");
            builder.append("└─ README.md\n");
            builder.append("```\n\n");
            builder.append("## 使用建议\n");
            builder.append("- `docs/` 目录用于沉淀课设提交材料，后续写报告会更顺手。\n");
            builder.append("- `sql/` 目录建议单独维护建表语句和测试数据。\n");
            builder.append("- 若课设只做后端，可合并 `frontend/` 为静态页面目录。\n");
            return builder.toString();
        } catch (Exception e) {
            return "生成项目目录结构建议失败：" + e.getMessage();
        }
    }
}
