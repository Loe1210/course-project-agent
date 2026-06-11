package com.keshetong.service;

import com.keshetong.config.DocumentChunkConfig;
import com.keshetong.dto.DocumentChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentChunkServiceTests {

    @Test
    void chunkDocumentSplitsMarkdownIntoSemanticChunks() {
        DocumentChunkConfig config = new DocumentChunkConfig();
        config.setMaxSize(80);
        config.setOverlap(20);

        DocumentChunkService service = new DocumentChunkService(config);

        String content = """
                # 项目背景
                校园二手交易平台用于帮助学生发布和购买闲置商品。

                ## 功能需求
                用户需要注册登录、发布商品、浏览商品、留言和下单。

                ## 数据设计
                系统至少需要用户表、商品表、订单表和留言表。
                """;

        List<DocumentChunk> chunks = service.chunkDocument(content, "demo.md");

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.size() >= 2);
        assertNotNull(chunks.get(0).getContent());
        assertTrue(chunks.stream().anyMatch(chunk -> chunk.getTitle() != null && !chunk.getTitle().isBlank()));
    }
}
