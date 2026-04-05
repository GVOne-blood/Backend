package com.theblood.springfood.chat.service.rag;

import com.theblood.springfood.chat.domain.KnowledgeDocument;
import com.theblood.springfood.chat.repository.KnowledgeDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for ingesting documents into the vector store.
 * Handles document loading, chunking, embedding, storage, and deletion.
 */
@Service
@Transactional
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;
    private final KnowledgeDocumentRepository documentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DocumentTextExtractor textExtractor;

    public DocumentIngestionService(
        VectorStore vectorStore,
        KnowledgeDocumentRepository documentRepository,
        JdbcTemplate jdbcTemplate,
        DocumentTextExtractor textExtractor
    ) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.textExtractor = textExtractor;
        // Split documents into 512-token chunks with 50-token overlap
        this.textSplitter = new TokenTextSplitter(512, 50, 5, 10000, true);
    }

    /**
     * Ingest or update a text document into the vector store.
     * If document with same source exists, it will be deleted first.
     *
     * @param content Document content
     * @param metadata Document metadata (must include 'source_type' and 'source_id')
     * @return Number of chunks created
     */
    public int ingestTextDocument(String content, Map<String, Object> metadata) {
        log.info("Ingesting text document with metadata: {}", metadata);

        String sourceType = (String) metadata.get("source_type");
        String sourceId = (String) metadata.get("source_id");
        String title = (String) metadata.getOrDefault("title", "Untitled");

        // Delete existing document if present
        if (sourceType != null && sourceId != null) {
            deleteDocumentBySource(sourceType, sourceId);
        }

        // Create document
        Document document = new Document(content, metadata);

        // Split into chunks
        List<Document> chunks = textSplitter.split(document);
        log.info("Split document into {} chunks", chunks.size());

        // Add to vector store (automatically generates embeddings)
        vectorStore.add(chunks);

        // Extract vector store IDs from chunks
        List<String> vectorIds = chunks.stream()
            .map(Document::getId)
            .collect(Collectors.toList());

        // Save tracking record
        KnowledgeDocument knowledgeDoc = KnowledgeDocument.builder()
            .title(title)
            .content(content)
            .sourceType(sourceType != null ? 
                KnowledgeDocument.DocumentSourceType.valueOf(sourceType) : 
                KnowledgeDocument.DocumentSourceType.GENERAL)
            .sourceId(sourceId)
            .metadata(metadata)
            .vectorStoreIds(vectorIds)
            .chunkCount(chunks.size())
            .isActive(true)
            .build();

        documentRepository.save(knowledgeDoc);
        log.info("Successfully ingested {} chunks and saved tracking record", chunks.size());

        return chunks.size();
    }

    /**
     * Ingest a PDF document into the vector store.
     *
     * @param pdfResource PDF file resource
     * @param metadata Document metadata
     * @return Number of chunks created
     */
    public int ingestPdfDocument(Resource pdfResource, Map<String, Object> metadata) {
        log.info("Ingesting PDF document: {}", pdfResource.getFilename());

        String sourceType = (String) metadata.get("source_type");
        String sourceId = (String) metadata.get("source_id");

        // Delete existing if present
        if (sourceType != null && sourceId != null) {
            deleteDocumentBySource(sourceType, sourceId);
        }

        // Read PDF
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
        List<Document> documents = pdfReader.get();

        // Combine all pages into one content
        String fullContent = documents.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n"));

        // Add metadata to all documents
        documents.forEach(doc -> doc.getMetadata().putAll(metadata));

        // Split into chunks
        List<Document> chunks = textSplitter.split(documents);
        log.info("Split PDF into {} chunks", chunks.size());

        // Add to vector store
        vectorStore.add(chunks);

        // Extract vector IDs
        List<String> vectorIds = chunks.stream()
            .map(Document::getId)
            .collect(Collectors.toList());

        // Save tracking record
        KnowledgeDocument knowledgeDoc = KnowledgeDocument.builder()
            .title((String) metadata.getOrDefault("title", pdfResource.getFilename()))
            .content(fullContent)
            .sourceType(sourceType != null ? 
                KnowledgeDocument.DocumentSourceType.valueOf(sourceType) : 
                KnowledgeDocument.DocumentSourceType.GENERAL)
            .sourceId(sourceId)
            .metadata(metadata)
            .vectorStoreIds(vectorIds)
            .chunkCount(chunks.size())
            .isActive(true)
            .build();

        documentRepository.save(knowledgeDoc);
        log.info("Successfully ingested PDF with {} chunks", chunks.size());

        return chunks.size();
    }

    /**
     * Delete document by source type and source ID.
     * This enables updating outdated documents.
     *
     * @param sourceType Source type (PRODUCT, ORDER, etc.)
     * @param sourceId Source identifier
     * @return true if document was deleted
     */
    public boolean deleteDocumentBySource(String sourceType, String sourceId) {
        log.info("Deleting document: sourceType={}, sourceId={}", sourceType, sourceId);

        Optional<KnowledgeDocument> docOpt = documentRepository.findBySourceTypeAndSourceId(
            KnowledgeDocument.DocumentSourceType.valueOf(sourceType),
            sourceId
        );

        if (docOpt.isEmpty()) {
            log.info("No document found to delete");
            return false;
        }

        KnowledgeDocument doc = docOpt.get();
        List<String> vectorIds = doc.getVectorStoreIds();

        if (!vectorIds.isEmpty()) {
            // Delete from vector store
            vectorStore.delete(vectorIds);
            log.info("Deleted {} vectors from vector store", vectorIds.size());
        }

        // Delete tracking record
        documentRepository.delete(doc);
        log.info("Deleted tracking record for document");

        return true;
    }

    /**
     * Delete all documents of a specific type.
     *
     * @param sourceType Source type to delete
     * @return Number of documents deleted
     */
    public int deleteDocumentsByType(String sourceType) {
        log.info("Deleting all documents of type: {}", sourceType);

        List<KnowledgeDocument> documents = documentRepository.findBySourceType(
            KnowledgeDocument.DocumentSourceType.valueOf(sourceType)
        );

        int count = 0;
        for (KnowledgeDocument doc : documents) {
            List<String> vectorIds = doc.getVectorStoreIds();
            if (!vectorIds.isEmpty()) {
                vectorStore.delete(vectorIds);
            }
            documentRepository.delete(doc);
            count++;
        }

        log.info("Deleted {} documents of type {}", count, sourceType);
        return count;
    }

    /**
     * Alternative: Delete directly from PostgreSQL using SQL.
     * Use this if Spring AI VectorStore.delete() has issues.
     *
     * @param sourceType Source type
     * @param sourceId Source ID
     */
    public void deleteFromVectorStoreDirectly(String sourceType, String sourceId) {
        log.info("Direct SQL delete: sourceType={}, sourceId={}", sourceType, sourceId);

        String sql = """
            DELETE FROM chat.vector_store
            WHERE metadata->>'source_type' = ?
            AND metadata->>'source_id' = ?
            """;

        int deleted = jdbcTemplate.update(sql, sourceType, sourceId);
        log.info("Deleted {} rows directly from vector_store table", deleted);

        // Also delete tracking record
        documentRepository.findBySourceTypeAndSourceId(
            KnowledgeDocument.DocumentSourceType.valueOf(sourceType),
            sourceId
        ).ifPresent(documentRepository::delete);
    }

    /**
     * Ingest Word document (.docx) into the vector store.
     *
     * @param wordResource Word file resource
     * @param metadata Document metadata
     * @return Number of chunks created
     */
    public int ingestWordDocument(Resource wordResource, Map<String, Object> metadata) {
        log.info("Ingesting Word document: {}", wordResource.getFilename());

        String sourceType = (String) metadata.get("source_type");
        String sourceId = (String) metadata.get("source_id");

        // Delete existing if present
        if (sourceType != null && sourceId != null) {
            deleteDocumentBySource(sourceType, sourceId);
        }

        try {
            // Extract text from Word document
            String extractedText = textExtractor.extractFromWord(wordResource.getInputStream());
            
            if (extractedText.isBlank()) {
                log.warn("No text extracted from Word document: {}", wordResource.getFilename());
                return 0;
            }

            // Create document with extracted text
            Document document = new Document(extractedText, metadata);

            // Split into chunks
            List<Document> chunks = textSplitter.split(document);
            log.info("Split Word document into {} chunks", chunks.size());

            // Add to vector store
            vectorStore.add(chunks);

            // Extract vector IDs
            List<String> vectorIds = chunks.stream()
                .map(Document::getId)
                .collect(Collectors.toList());

            // Save tracking record
            KnowledgeDocument knowledgeDoc = KnowledgeDocument.builder()
                .title((String) metadata.getOrDefault("title", wordResource.getFilename()))
                .content(extractedText)
                .sourceType(sourceType != null ? 
                    KnowledgeDocument.DocumentSourceType.valueOf(sourceType) : 
                    KnowledgeDocument.DocumentSourceType.GENERAL)
                .sourceId(sourceId)
                .metadata(metadata)
                .vectorStoreIds(vectorIds)
                .chunkCount(chunks.size())
                .isActive(true)
                .build();

            documentRepository.save(knowledgeDoc);
            log.info("Successfully ingested Word document with {} chunks", chunks.size());

            return chunks.size();
        } catch (IOException e) {
            log.error("Failed to read Word document", e);
            throw new RuntimeException("Failed to process Word document: " + e.getMessage(), e);
        }
    }
}

