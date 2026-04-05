package com.theblood.springfood.chat.service.rag;

import com.theblood.springfood.chat.domain.RagSearchLog;
import com.theblood.springfood.chat.repository.RagSearchLogRepository;
import com.theblood.springfood.chat.service.dto.RAGSearchRequest;
import com.theblood.springfood.chat.service.dto.RAGSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for RAG (Retrieval-Augmented Generation) operations.
 * Handles semantic search and context building for AI.
 */
@Service
@Transactional
public class RAGService {

    private static final Logger log = LoggerFactory.getLogger(RAGService.class);

    private final VectorStore vectorStore;
    private final RagSearchLogRepository searchLogRepository;

    public RAGService(VectorStore vectorStore, RagSearchLogRepository searchLogRepository) {
        this.vectorStore = vectorStore;
        this.searchLogRepository = searchLogRepository;
    }

    /**
     * Perform semantic search on vector store.
     *
     * @param request Search request with query and filters
     * @return Search results with similar documents
     */
    public RAGSearchResponse search(RAGSearchRequest request) {
        log.info("Performing RAG search: query='{}', topK={}", request.getQuery(), request.getTopK());

        // Build search request with filters (Spring AI 1.0.0 GA API)
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
            .query(request.getQuery())
            .topK(request.getTopK())
            .similarityThreshold(request.getSimilarityThreshold());

        // Add metadata filters if provided
        if (request.getSourceType() != null || !request.getMetadataFilters().isEmpty()) {
            Filter.Expression filterExpression = buildFilterExpression(request);
            if (filterExpression != null) {
                searchBuilder.filterExpression(filterExpression);
            }
        }

        // Execute search
        List<Document> documents = vectorStore.similaritySearch(searchBuilder.build());
        log.info("Found {} similar documents", documents.size());

        // Convert to response
        List<RAGSearchResponse.SearchResult> results = documents.stream()
            .map(this::convertToSearchResult)
            .collect(Collectors.toList());

        // Build combined context for AI
        String context = buildContext(documents);

        // Log search for analytics
        logSearch(request.getQuery(), results.size(), request.getTopK());

        return RAGSearchResponse.builder()
            .query(request.getQuery())
            .results(results)
            .totalResults(results.size())
            .context(context)
            .build();
    }

    /**
     * Build filter expression from request
     */
    private Filter.Expression buildFilterExpression(RAGSearchRequest request) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();

        // Filter by source type
        if (request.getSourceType() != null) {
            return builder.eq("source_type", request.getSourceType()).build();
        }

        // Add custom metadata filters
        // Note: Spring AI filter syntax may vary, adjust as needed
        return null;
    }

    /**
     * Convert Document to SearchResult
     */
    private RAGSearchResponse.SearchResult convertToSearchResult(Document doc) {
        return RAGSearchResponse.SearchResult.builder()
            .content(doc.getText())
            .similarity(doc.getMetadata().get("distance") != null ?
                1.0 - ((Number) doc.getMetadata().get("distance")).doubleValue() : null)
            .metadata(doc.getMetadata())
            .sourceType((String) doc.getMetadata().get("source_type"))
            .sourceId((String) doc.getMetadata().get("source_id"))
            .title((String) doc.getMetadata().get("title"))
            .build();
    }

    /**
     * Build combined context from documents for AI
     */
    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "No relevant information found.";
        }

        StringBuilder context = new StringBuilder();
        context.append("Relevant information:\n\n");

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append(String.format("[Document %d]\n", i + 1));
            context.append(String.format("Title: %s\n", doc.getMetadata().get("title")));
            context.append(String.format("Content: %s\n\n", doc.getText()));
        }

        return context.toString();
    }

    /**
     * Log search for analytics
     */
    private void logSearch(String query, int resultsCount, int topK) {
        try {
            RagSearchLog searchLog = RagSearchLog.builder()
                .query(query)
                .resultsCount(resultsCount)
                .topK(topK)
                .build();
            searchLogRepository.save(searchLog);
        } catch (Exception e) {
            log.warn("Failed to log search: {}", e.getMessage());
        }
    }
}
