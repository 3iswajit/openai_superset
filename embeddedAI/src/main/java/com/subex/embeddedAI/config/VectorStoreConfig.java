//package com.subex.embeddedAI.config;
//
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.PgVectorStore;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import static org.springframework.ai.vectorstore.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
//import static org.springframework.ai.vectorstore.PgVectorStore.PgIndexType.HNSW;
//
//@Configuration
//public class VectorStoreConfig {
//
//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, @Qualifier("ollamaEmbeddingModel")EmbeddingModel embeddingModel) {
//        return new PgVectorStore.Builder(jdbcTemplate, embeddingModel)
//                .withDimensions(1536)                    // Optional: defaults to model dimensions or 1536
//                .withDistanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
//                .withIndexType(HNSW)                     // Optional: defaults to HNSW
//                .withInitializeSchema(true)              // Optional: defaults to false
//                .withSchemaName("public")                // Optional: defaults to "public"
//                .withVectorTableName("vector_store")     // Optional: defaults to "vector_store"
//                .withMaxDocumentBatchSize(10000)         // Optional: defaults to 10000
//                .build();
//    }
//}

//docker run --name redis-server -d -p 6379:6379 redis