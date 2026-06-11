package com.keshetong.service;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.embeddings.TextEmbeddingResultItem;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class VectorEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(VectorEmbeddingService.class);

    @Value("${dashscope.api.key}")
    private String apiKey;

    @Value("${dashscope.embedding.model}")
    private String model;

    private final TextEmbedding textEmbedding = new TextEmbedding();

    public List<Float> generateEmbedding(String content) {
        validateApiKey();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("待向量化的内容不能为空");
        }
        try {
            Constants.apiKey = apiKey;
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(model)
                    .texts(Collections.singletonList(content))
                    .build();
            TextEmbeddingResult result = textEmbedding.call(param);
            return extractVector(result);
        } catch (NoApiKeyException e) {
            throw new RuntimeException("DashScope API Key 无效或未配置", e);
        } catch (Exception e) {
            logger.error("生成向量失败", e);
            throw new RuntimeException("生成向量失败：" + e.getMessage(), e);
        }
    }

    public List<Float> generateQueryVector(String query) {
        return generateEmbedding(query);
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank() || "your-api-key-here".equals(apiKey)) {
            throw new IllegalStateException("在使用 RAG 功能前，请先配置 DASHSCOPE_API_KEY");
        }
    }

    private List<Float> extractVector(TextEmbeddingResult result) {
        if (result == null || result.getOutput() == null || result.getOutput().getEmbeddings() == null) {
            throw new RuntimeException("DashScope 返回的向量结果为空");
        }
        List<TextEmbeddingResultItem> items = result.getOutput().getEmbeddings();
        if (items.isEmpty()) {
            throw new RuntimeException("DashScope 未返回任何向量数据");
        }
        List<Float> vector = new ArrayList<>(items.get(0).getEmbedding().size());
        for (Double value : items.get(0).getEmbedding()) {
            vector.add(value.floatValue());
        }
        return vector;
    }
}
