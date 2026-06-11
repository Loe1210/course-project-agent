package com.keshetong.client;

import com.keshetong.config.MilvusProperties;
import com.keshetong.config.RagProperties;
import com.keshetong.constant.MilvusConstants;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CollectionSchemaParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MilvusClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(MilvusClientFactory.class);

    private final MilvusProperties milvusProperties;
    private final RagProperties ragProperties;

    public MilvusClientFactory(MilvusProperties milvusProperties, RagProperties ragProperties) {
        this.milvusProperties = milvusProperties;
        this.ragProperties = ragProperties;
    }

    public MilvusServiceClient createClient() {
        if (!milvusProperties.isEnabled()) {
            throw new IllegalStateException("Milvus is disabled by configuration");
        }

        MilvusServiceClient client = null;
        try {
            client = connectToMilvus();
            if (!collectionExists(client, ragProperties.getCollectionName())) {
                logger.info("Creating Milvus collection {}", ragProperties.getCollectionName());
                createKnowledgeCollection(client);
                createIndexes(client);
            }
            return client;
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            throw new RuntimeException("Failed to initialize Milvus client: " + e.getMessage(), e);
        }
    }

    private MilvusServiceClient connectToMilvus() {
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withHost(milvusProperties.getHost())
                .withPort(milvusProperties.getPort())
                .withConnectTimeout(milvusProperties.getTimeout(), TimeUnit.MILLISECONDS);
        if (milvusProperties.getUsername() != null && !milvusProperties.getUsername().isEmpty()) {
            builder.withAuthorization(milvusProperties.getUsername(), milvusProperties.getPassword());
        }
        return new MilvusServiceClient(builder.build());
    }

    private boolean collectionExists(MilvusServiceClient client, String collectionName) {
        R<Boolean> response = client.hasCollection(
                HasCollectionParam.newBuilder().withCollectionName(collectionName).build()
        );
        if (response.getStatus() != 0) {
            throw new RuntimeException("Failed to check collection: " + response.getMessage());
        }
        return response.getData();
    }

    private void createKnowledgeCollection(MilvusServiceClient client) {
        FieldType idField = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.VarChar)
                .withMaxLength(MilvusConstants.ID_MAX_LENGTH)
                .withPrimaryKey(true)
                .build();

        FieldType vectorField = FieldType.newBuilder()
                .withName("vector")
                .withDataType(DataType.FloatVector)
                .withDimension(MilvusConstants.VECTOR_DIM)
                .build();

        FieldType contentField = FieldType.newBuilder()
                .withName("content")
                .withDataType(DataType.VarChar)
                .withMaxLength(MilvusConstants.CONTENT_MAX_LENGTH)
                .build();

        FieldType metadataField = FieldType.newBuilder()
                .withName("metadata")
                .withDataType(DataType.JSON)
                .build();

        CollectionSchemaParam schema = CollectionSchemaParam.newBuilder()
                .withEnableDynamicField(false)
                .addFieldType(idField)
                .addFieldType(vectorField)
                .addFieldType(contentField)
                .addFieldType(metadataField)
                .build();

        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(ragProperties.getCollectionName())
                .withDescription("Course project knowledge collection")
                .withSchema(schema)
                .withShardsNum(MilvusConstants.DEFAULT_SHARD_NUMBER)
                .build();

        R<RpcStatus> response = client.createCollection(createParam);
        if (response.getStatus() != 0) {
            throw new RuntimeException("Failed to create collection: " + response.getMessage());
        }
    }

    private void createIndexes(MilvusServiceClient client) {
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(ragProperties.getCollectionName())
                .withFieldName("vector")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.L2)
                .withExtraParam("{\"nlist\":128}")
                .withSyncMode(false)
                .build();

        R<RpcStatus> response = client.createIndex(indexParam);
        if (response.getStatus() != 0) {
            throw new RuntimeException("Failed to create vector index: " + response.getMessage());
        }
    }
}
