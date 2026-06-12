package com.keshetong.agent.tool;

import com.keshetong.service.CourseToolSupportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseDesignTools {

    private final CourseToolSupportService supportService;

    public DatabaseDesignTools(CourseToolSupportService supportService) {
        this.supportService = supportService;
    }

    @Tool(description = "生成课程设计项目的数据库设计建议，包括核心数据表、关键字段和表关系说明。")
    public String designCourseProjectDatabase(
            @ToolParam(description = "课设题目名称") String topic,
            @ToolParam(description = "可选：业务范围或补充要求") String businessScope) {
        try {
            String resolvedTopic = supportService.resolveTopic(topic);
            List<String> tables = supportService.inferDatabaseTables(resolvedTopic);
            StringBuilder builder = new StringBuilder();
            builder.append("# 数据库设计建议\n\n");
            builder.append("**课设题目：** ").append(resolvedTopic).append("\n");
            builder.append("**业务范围：** ").append(
                    businessScope == null || businessScope.isBlank() ? "按常规课程设计范围生成" : businessScope.trim()
            ).append("\n\n");
            builder.append("## 核心数据表\n");
            for (String table : tables) {
                builder.append("- `").append(table).append("`：")
                        .append(describeTable(table))
                        .append("\n");
            }
            builder.append("\n## 关键关系设计\n");
            builder.append("- 用户表通常与业务主表是一对多关系，用于记录创建人、发布人或操作人。\n");
            builder.append("- 业务主表与明细表通常是一对多关系，例如订单与订单项、课程与成绩项。\n");
            builder.append("- 若存在分类、角色、状态枚举，建议拆分为独立字典表或使用稳定枚举字段。\n");
            builder.append("\n## 设计建议\n");
            builder.append("- 所有核心表建议包含 `id`、`create_time`、`update_time`、`deleted` 等基础字段。\n");
            builder.append("- 对检索频繁的字段建立索引，如用户名、订单编号、商品名称、课程编号。\n");
            builder.append("- 在报告中重点说明主键策略、外键关系、索引设计和字段约束。\n");
            return builder.toString();
        } catch (Exception e) {
            return "生成数据库设计建议失败：" + e.getMessage();
        }
    }

    private String describeTable(String tableName) {
        return switch (tableName) {
            case "user" -> "存储系统用户账号、角色、状态等信息";
            case "student" -> "存储学生基础资料，如学号、姓名、班级、联系方式";
            case "teacher" -> "存储教师基础信息及授课关系";
            case "course" -> "存储课程名称、学分、授课教师等信息";
            case "score" -> "存储成绩记录，关联学生与课程";
            case "class_info" -> "存储班级、专业、年级等组织信息";
            case "book" -> "存储图书基础信息，如编号、名称、作者、库存";
            case "category" -> "存储分类信息";
            case "borrow_record" -> "存储图书借阅与归还记录";
            case "fine_record" -> "存储逾期罚款等记录";
            case "merchant" -> "存储商家或店铺资料";
            case "product" -> "存储商品信息";
            case "cart_item" -> "存储购物车条目";
            case "orders" -> "存储订单主表信息";
            case "order_item" -> "存储订单明细";
            case "delivery" -> "存储配送进度和骑手信息";
            case "product_image" -> "存储商品图片资源";
            case "favorite" -> "存储收藏关系";
            case "comment" -> "存储评价内容和评分";
            case "complaint" -> "存储举报或申诉记录";
            case "role" -> "存储角色定义和权限范围";
            case "business_entity" -> "存储核心业务对象";
            case "business_record" -> "存储业务流转记录";
            case "notice" -> "存储公告通知";
            case "operation_log" -> "存储系统操作日志";
            default -> "存储业务相关数据";
        };
    }
}
