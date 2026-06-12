package com.keshetong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keshetong.dto.CourseProjectRequest;
import com.keshetong.dto.CourseProjectResponse;
import com.keshetong.service.CourseProjectAgentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseProjectController.class)
class CourseProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseProjectAgentService courseProjectAgentService;

    @Test
    void shouldReturnCourseProjectResponse() throws Exception {
        CourseProjectRequest request = new CourseProjectRequest();
        request.setRequestId("project-001");
        request.setTopic("校园二手交易平台");
        request.setRequirements("要求包含用户、商品、订单、评价模块");
        request.setUseTools(true);

        Mockito.when(courseProjectAgentService.generateCourseProjectPlan(Mockito.any(CourseProjectRequest.class)))
                .thenReturn(buildResponse());

        mockMvc.perform(post("/api/course_project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.requestId").value("project-001"))
                .andExpect(jsonPath("$.data.topic").value("校园二手交易平台"))
                .andExpect(jsonPath("$.data.databaseDesign").value("数据库设计"))
                .andExpect(jsonPath("$.data.usedTools").value(true));
    }

    @Test
    void shouldHandleProjectBadRequest() throws Exception {
        CourseProjectRequest request = new CourseProjectRequest();
        request.setTopic("");

        Mockito.when(courseProjectAgentService.generateCourseProjectPlan(Mockito.any(CourseProjectRequest.class)))
                .thenThrow(new IllegalArgumentException("课设题目不能为空"));

        mockMvc.perform(post("/api/course_project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("课设题目不能为空"));
    }

    private CourseProjectResponse buildResponse() {
        CourseProjectResponse response = new CourseProjectResponse();
        response.setRequestId("project-001");
        response.setTopic("校园二手交易平台");
        response.setSupervisorResult("supervisor");
        response.setPlannerResult("planner");
        response.setExecutorResult("executor");
        response.setDatabaseDesign("数据库设计");
        response.setApiDesign("接口设计");
        response.setProjectStructure("项目结构");
        response.setReportOutline("报告大纲");
        response.setTestCases("测试用例");
        response.setDefenseQa("答辩问答");
        response.setSummary("聚合结果摘要");
        response.setUsedTools(true);
        return response;
    }
}
