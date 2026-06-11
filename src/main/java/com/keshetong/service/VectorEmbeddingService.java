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
            throw new IllegalArgumentException("Content must not be empty");
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
            throw new RuntimeException("DashScope API key is invalid or missing", e);
        } catch (Exception e) {
            logger.error("Failed to generate embedding", e);
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    public List<Float> generateQueryVector(String query) {
        return generateEmbedding(query);
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank() || "your-api-key-here".equals(apiKey)) {
            throw new IllegalStateException("Please configure DASHSCOPE_API_KEY before using RAG features");
        }
    }

    private List<Float> extractVector(TextEmbeddingResult result) {
        if (result == null || result.getOutput() == null || result.getOutput().getEmbeddings() == null) {
            throw new RuntimeException("DashScope returned an empty embedding result");
        }
        List<TextEmbeddingResultItem> items = result.getOutput().getEmbeddings();
        if (items.isEmpty()) {
            throw new RuntimeException("DashScope returned no embedding items");
        }
        List<Float> vector = new ArrayList<>(items.get(0).getEmbedding().size());
        for (Double value : items.get(0).getEmbedding()) {
            vector.add(value.floatValue());
        }
        return vector;
    }
}
