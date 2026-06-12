package com.keshetong.controller;

import com.keshetong.config.FileUploadConfig;
import com.keshetong.service.VectorIndexService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileUploadController.class)
class FileUploadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadConfig fileUploadConfig;

    @MockBean
    private VectorIndexService vectorIndexService;

    @Test
    void shouldReturnUploadAndIndexingStatus() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task.txt",
                "text/plain",
                "课程设计任务书".getBytes()
        );

        Mockito.when(fileUploadConfig.getAllowedExtensions()).thenReturn("txt,md,docx,pdf");
        Mockito.when(fileUploadConfig.getPath()).thenReturn("target/test-uploads");
        Mockito.when(vectorIndexService.indexSingleFile(Mockito.anyString()))
                .thenReturn(new VectorIndexService.SingleFileIndexingResult(true, "target/test-uploads/task.txt", 3, "知识库入库成功"));

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileSaved").value(true))
                .andExpect(jsonPath("$.data.vectorIndexed").value(true))
                .andExpect(jsonPath("$.data.chunkCount").value(3));
    }

    @Test
    void shouldReturnPartialSuccessWhenVectorIndexingFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task.txt",
                "text/plain",
                "课程设计任务书".getBytes()
        );

        Mockito.when(fileUploadConfig.getAllowedExtensions()).thenReturn("txt,md,docx,pdf");
        Mockito.when(fileUploadConfig.getPath()).thenReturn("target/test-uploads");
        Mockito.when(vectorIndexService.indexSingleFile(Mockito.anyString()))
                .thenThrow(new RuntimeException("Milvus 未启动"));

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileSaved").value(true))
                .andExpect(jsonPath("$.data.vectorIndexed").value(false))
                .andExpect(jsonPath("$.data.errorMessage").value("Milvus 未启动"));
    }
}
