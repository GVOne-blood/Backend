package com.theblood.springfood.chat.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration for Spring AI Vector Store using pgvector.
 * 
 * Spring AI automatically creates the vector_store table with schema:
 * - id UUID PRIMARY KEY
 * - content TEXT
 * - metadata JSON
 * - embedding vector(768) -- dimension from config
 * 
 * No need to manually create entities or tables!
 */
@Configuration
public class VectorStoreConfig {

    /**
     * Creates PgVectorStore bean for RAG operations.
     * 
     * Spring AI handles:
     * - Table creation (if initialize-schema=true)
     * - HNSW index creation
     * - Vector similarity search
     * - Metadata filtering
     * 
     * @param jdbcTemplate JDBC template for database operations
     * @param embeddingModel Gemini embedding model (auto-configured)
     * @return Configured vector store
     */
    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
    }
}
