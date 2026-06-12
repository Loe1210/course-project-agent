package com.keshetong.agent.tool;

import com.keshetong.service.CourseToolSupportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiDesignTools {

    private final CourseToolSupportService supportService;

    public ApiDesignTools(CourseToolSupportService supportService) {
        this.supportService = supportService;
    }

    @Tool(description = "生成课程设计项目的接口设计草案，适合写入接口文档或开发计划。")
    public String designCourseProjectApis(
            @ToolParam(description = "课设题目名称") String topic,
            @ToolParam(description = "系统模块，多个模块可用逗号分隔") String modules) {
        try {
            String resolvedTopic = supportService.resolveTopic(topic);
            List<String> resolvedModules = supportService.resolveModules(modules, resolvedTopic);
            StringBuilder builder = new StringBuilder();
            builder.append("# 接口设计草案\n\n");
            builder.append("**课设题目：** ").append(resolvedTopic).append("\n\n");
            builder.append("## 通用接口规范\n");
            builder.append("- 建议统一使用 RESTful 风格。\n");
            builder.append("- 统一返回结构：`code`、`message`、`data`。\n");
            builder.append("- 涉及分页的接口建议增加 `pageNum`、`pageSize`、`total` 字段。\n\n");
            builder.append("## 模块接口建议\n");
            for (String module : resolvedModules) {
                builder.append("### ").append(module).append("\n");
                builder.append("- `GET /api/").append(toPath(module)).append("/list`：查询").append(module).append("列表\n");
                builder.append("- `GET /api/").append(toPath(module)).append("/{id}`：查询").append(module).append("详情\n");
                builder.append("- `POST /api/").append(toPath(module)).append("`：新增").append(module).append("\n");
                builder.append("- `PUT /api/").append(toPath(module)).append("/{id}`：修改").append(module).append("\n");
                builder.append("- `DELETE /api/").append(toPath(module)).append("/{id}`：删除").append(module).append("\n\n");
            }
            builder.append("## 文档建议\n");
            builder.append("- 对每个接口补充请求参数、响应示例、异常场景和权限要求。\n");
            builder.append("- 若课设答辩强调前后端联调，建议补充接口调用流程图。\n");
            return builder.toString();
        } catch (Exception e) {
            return "生成接口设计草案失败：" + e.getMessage();
        }
    }

    private String toPath(String module) {
        return module.replace("与", "")
                .replace("管理", "")
                .replace("信息", "")
                .replace("流程", "")
                .replace(" ", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "");
    }
}
