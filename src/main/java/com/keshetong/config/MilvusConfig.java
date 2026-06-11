package com.keshetong.config;

import io.milvus.client.MilvusServiceClient;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class MilvusConfig {

    private static final Logger logger = LoggerFactory.getLogger(MilvusConfig.class);

    private MilvusServiceClient milvusClient;

    @Bean
    @Lazy
    public MilvusServiceClient milvusServiceClient(ObjectProvider<com.keshetong.client.MilvusClientFactory> factoryProvider) {
        var factory = factoryProvider.getIfAvailable();
        if (factory == null) {
            throw new IllegalStateException("MilvusClientFactory is not available");
        }
        logger.info("Initializing Milvus client");
        milvusClient = factory.createClient();
        return milvusClient;
    }

    @PreDestroy
    public void cleanup() {
        if (milvusClient != null) {
            logger.info("Closing Milvus client");
            milvusClient.close();
        }
    }
}
