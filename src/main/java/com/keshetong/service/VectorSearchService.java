package com.keshetong.service;

import com.keshetong.config.RagProperties;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class VectorSearchService {

    private static final Logger logger = LoggerFactory.getLogger(VectorSearchService.class);

    private final MilvusServiceClient milvusClient;
    private final VectorEmbeddingService embeddingService;
    private final RagProperties ragProperties;

    public VectorSearchService(@Lazy MilvusServiceClient milvusClient,
                               VectorEmbeddingService embeddingService,
                               RagProperties ragProperties) {
        this.milvusClient = milvusClient;
        this.embeddingService = embeddingService;
        this.ragProperties = ragProperties;
    }

    public List<SearchResult> searchSimilarDocuments(String query, int topK) {
        try {
            List<Float> queryVector = embeddingService.generateQueryVector(query);
            milvusClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(ragProperties.getCollectionName())
                            .build()
            );

            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(ragProperties.getCollectionName())
                    .withVectorFieldName("vector")
                    .withVectors(Collections.singletonList(queryVector))
                    .withTopK(topK)
                    .withMetricType(io.milvus.param.MetricType.L2)
                    .withOutFields(List.of("id", "content", "metadata"))
                    .withParams("{\"nprobe\":10}")
                    .build();

            R<SearchResults> response = milvusClient.search(searchParam);
            if (response.getStatus() != 0) {
                throw new RuntimeException("Milvus 检索失败：" + response.getMessage());
            }

            SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
            List<SearchResult> results = new ArrayList<>();
            for (int i = 0; i < wrapper.getRowRecords(0).size(); i++) {
                SearchResult result = new SearchResult();
                result.setId((String) wrapper.getIDScore(0).get(i).get("id"));
                result.setContent((String) wrapper.getFieldData("content", 0).get(i));
                result.setScore(wrapper.getIDScore(0).get(i).getScore());
                Object metadataObj = wrapper.getFieldData("metadata", 0).get(i);
                if (metadataObj != null) {
                    result.setMetadata(metadataObj.toString());
                }
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            logger.error("检索向量知识库失败", e);
            throw new RuntimeException("检索向量知识库失败：" + e.getMessage(), e);
        }
    }

    @Getter
    @Setter
    public static class SearchResult {
        private String id;
        private String content;
        private float score;
        private String metadata;
    }
}
