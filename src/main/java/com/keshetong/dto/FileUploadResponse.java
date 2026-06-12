package com.keshetong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileUploadResponse {

    private String fileName;
    private String filePath;
    private Long fileSize;
    private boolean fileSaved;
    private boolean vectorIndexed;
    private Integer chunkCount;
    private String knowledgeBaseMessage;
    private String errorMessage;
}
