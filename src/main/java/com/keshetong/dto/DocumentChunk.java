package com.keshetong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentChunk {

    private String content;
    private int startIndex;
    private int endIndex;
    private int chunkIndex;
    private String title;

    public DocumentChunk() {
    }

    public DocumentChunk(String content, int startIndex, int endIndex, int chunkIndex) {
        this.content = content;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.chunkIndex = chunkIndex;
    }
}
