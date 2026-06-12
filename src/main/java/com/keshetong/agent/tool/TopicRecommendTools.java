package com.keshetong.agent.tool;

import com.keshetong.service.CourseToolSupportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TopicRecommendTools {

    private final CourseToolSupportService supportService;

    public TopicRecommendTools(CourseToolSupportService supportService) {
        this.supportService = supportService;
    }

    @Tool(description = "推荐适合大学生软件课程设计的选题，并给出功能范围、技术建议和实现难度。")
    public String recommendCourseProjectTopics(
            @ToolParam(description = "希望聚焦的技术方向，例如 Java Web、前后端分离、微信小程序") String direction,
            @ToolParam(description = "推荐数量，建议取 3 到 5") Integer count) {
        try {
            String targetDirection = supportService.normalizedDirection(direction);
            int finalCount = count == null || count <= 0 ? 3 : Math.min(count, 5);
            List<String[]> candidates = buildCandidates(targetDirection);
            StringBuilder builder = new StringBuilder();
            builder.append("# 课设选题推荐\n\n");
            builder.append("**技术方向：** ").append(targetDirection).append("\n\n");
            for (int i = 0; i < Math.min(finalCount, candidates.size()); i++) {
                String[] item = candidates.get(i);
                builder.append("## ").append(i + 1).append(". ").append(item[0]).append("\n");
                builder.append("- 适合原因：").append(item[1]).append("\n");
                builder.append("- 核心模块：").append(item[2]).append("\n");
                builder.append("- 推荐技术：").append(item[3]).append("\n");
                builder.append("- 难度评估：").append(item[4]).append("\n\n");
            }
            builder.append("## 选题建议\n");
            builder.append("- 优先选择业务边界清晰、数据结构稳定的题目。\n");
            builder.append("- 课设答辩时要能讲清楚需求、数据库、接口和测试流程。\n");
            builder.append("- 若时间有限，建议从校园服务类和信息管理类题目中选择。\n");
            return builder.toString();
        } catch (Exception e) {
            return "生成课设选题推荐失败：" + e.getMessage();
        }
    }

    private List<String[]> buildCandidates(String direction) {
        List<String[]> candidates = new ArrayList<>();
        candidates.add(new String[]{
                "校园二手交易平台",
                "业务场景贴近校园生活，功能完整但复杂度可控。",
                "用户登录、商品发布、商品检索、订单交易、评价举报",
                direction + "、MySQL、Redis（可选）",
                "中等"
        });
        candidates.add(new String[]{
                "学生成绩管理系统",
                "实体关系明确，适合展示数据库设计和 CRUD 能力。",
                "学生管理、课程管理、成绩录入、成绩查询、统计分析",
                direction + "、MySQL、ECharts（可选）",
                "较低"
        });
        candidates.add(new String[]{
                "校园图书借阅系统",
                "借阅流程清晰，适合体现状态流转和权限控制。",
                "图书检索、借阅登记、归还续借、库存管理、公告通知",
                direction + "、MySQL、Element Plus（可选）",
                "中等"
        });
        candidates.add(new String[]{
                "宿舍报修管理系统",
                "流程型业务明显，适合展示工单流转和角色权限。",
                "报修提交、工单分配、维修处理、进度跟踪、评价反馈",
                direction + "、MySQL、消息通知（可选）",
                "中等"
        });
        candidates.add(new String[]{
                "校园活动报名系统",
                "前后端交互丰富，适合体现表单校验和并发控制。",
                "活动发布、活动报名、名额控制、签到记录、数据统计",
                direction + "、MySQL、Vue",
                "中等"
        });
        return candidates;
    }
}
