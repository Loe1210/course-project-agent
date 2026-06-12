package com.keshetong.service;

import com.keshetong.config.CourseProjectProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CourseToolSupportService {

    private final CourseProjectProperties courseProjectProperties;

    public CourseToolSupportService(CourseProjectProperties courseProjectProperties) {
        this.courseProjectProperties = courseProjectProperties;
    }

    public String resolveTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return courseProjectProperties.getDefaultTopic();
        }
        return topic.trim();
    }

    public List<String> resolveTechStack(String techStackText) {
        if (techStackText == null || techStackText.isBlank()) {
            return new ArrayList<>(courseProjectProperties.getDefaultTechStack());
        }
        List<String> stackList = splitInput(techStackText);
        return stackList.isEmpty() ? new ArrayList<>(courseProjectProperties.getDefaultTechStack()) : stackList;
    }

    public List<String> resolveModules(String modulesText, String topic) {
        List<String> modules = splitInput(modulesText);
        if (!modules.isEmpty()) {
            return modules;
        }
        return inferModulesFromTopic(topic);
    }

    public List<String> inferModulesFromTopic(String topic) {
        String lowerTopic = resolveTopic(topic).toLowerCase(Locale.ROOT);
        if (lowerTopic.contains("学生")) {
            return List.of("用户登录与权限", "学生信息管理", "课程信息管理", "成绩信息管理", "统计分析");
        }
        if (lowerTopic.contains("图书")) {
            return List.of("用户登录与权限", "图书检索", "借阅管理", "归还与续借", "库存统计");
        }
        if (lowerTopic.contains("外卖") || lowerTopic.contains("点餐")) {
            return List.of("用户登录与权限", "商品展示", "购物车与下单", "订单管理", "配送状态跟踪");
        }
        if (lowerTopic.contains("二手")) {
            return List.of("用户登录与权限", "商品发布", "商品检索", "订单与交易", "评价与举报");
        }
        return List.of("用户登录与权限", "基础信息管理", "业务流程管理", "数据统计分析", "系统维护");
    }

    public List<String> inferDatabaseTables(String topic) {
        String lowerTopic = resolveTopic(topic).toLowerCase(Locale.ROOT);
        if (lowerTopic.contains("学生")) {
            return List.of("user", "student", "teacher", "course", "score", "class_info");
        }
        if (lowerTopic.contains("图书")) {
            return List.of("user", "book", "category", "borrow_record", "fine_record");
        }
        if (lowerTopic.contains("外卖") || lowerTopic.contains("点餐")) {
            return List.of("user", "merchant", "product", "cart_item", "orders", "order_item", "delivery");
        }
        if (lowerTopic.contains("二手")) {
            return List.of("user", "product", "product_image", "orders", "favorite", "comment", "complaint");
        }
        return List.of("user", "role", "business_entity", "business_record", "notice", "operation_log");
    }

    public String markdownList(List<String> items) {
        return items.stream().map(item -> "- " + item).collect(Collectors.joining("\n"));
    }

    public String normalizedDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return "Java Web";
        }
        return direction.trim();
    }

    private List<String> splitInput(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split("[,，、\\n]"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .toList();
    }
}
