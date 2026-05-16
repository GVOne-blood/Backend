package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.service.ai.RagImportService;
import com.theblood.springfood.chat.service.dto.*;
import com.theblood.springfood.chat.service.rag.DocumentIngestionService;
import com.theblood.springfood.chat.service.rag.RAGService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for Knowledge Base / RAG operations.
 * Handles document upload, search, and management.
 */
@RestController
@RequestMapping("/api/knowledge-base")
@Tag(name = "Knowledge Base", description = "RAG document management and semantic search")
public class KnowledgeBaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private final DocumentIngestionService ingestionService;
    private final RAGService ragService;
    private final RagImportService ragImportService;

    public KnowledgeBaseController(
        DocumentIngestionService ingestionService,
        RAGService ragService,
        RagImportService ragImportService
    ) {
        this.ingestionService = ingestionService;
        this.ragService = ragService;
        this.ragImportService = ragImportService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync/products")
    @Operation(summary = "Sync products to knowledge base", description = "Import all products from product-service into vector store for AI RAG")
    public ResponseEntity<DocumentUploadResponse> syncProducts() {
        log.info("Manually triggered product sync to knowledge base...");
        try {
            int totalChunks = ragImportService.importAllProducts();
            return ResponseEntity.ok(DocumentUploadResponse.builder()
                .success(true)
                .message("Products synced successfully")
                .chunkCount(totalChunks)
                .build());
        } catch (Exception e) {
            log.error("Failed to sync products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to sync products: " + e.getMessage())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync/shops")
    @Operation(summary = "Sync shops to knowledge base", description = "Import all shops from shop-service into vector store for AI RAG")
    public ResponseEntity<DocumentUploadResponse> syncShops() {
        log.info("Manually triggered shop sync to knowledge base...");
        try {
            int totalChunks = ragImportService.importAllShops();
            return ResponseEntity.ok(DocumentUploadResponse.builder()
                .success(true)
                .message("Shops synced successfully")
                .chunkCount(totalChunks)
                .build());
        } catch (Exception e) {
            log.error("Failed to sync shops", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to sync shops: " + e.getMessage())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync/all")
    @Operation(summary = "Sync all data to knowledge base", description = "Import all products and shops into vector store")
    public ResponseEntity<DocumentUploadResponse> syncAll() {
        log.info("Full sync triggered...");
        try {
            int productChunks = ragImportService.importAllProducts();
            int shopChunks = ragImportService.importAllShops();
            return ResponseEntity.ok(DocumentUploadResponse.builder()
                .success(true)
                .message("Full sync completed")
                .chunkCount(productChunks + shopChunks)
                .build());
        } catch (Exception e) {
            log.error("Full sync failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Full sync failed: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Upload text document to knowledge base
     * 
     * Example request:
     * POST /api/knowledge-base/documents/text
     * {
     *   "title": "Product Guide - iPhone 15",
     *   "content": "iPhone 15 features A16 Bionic chip...",
     *   "sourceType": "PRODUCT",
     *   "sourceId": "product_12345",
     *   "additionalMetadata": {
     *     "category": "electronics",
     *     "language": "vi",
     *     "tags": ["featured", "new"]
     *   }
     * }
     */
    @PostMapping("/documents/text")
    @Operation(summary = "Upload text document", description = "Upload plain text content to knowledge base")
    public ResponseEntity<DocumentUploadResponse> uploadTextDocument(
        @Valid @RequestBody DocumentUploadRequest request
    ) {
        log.info("Uploading text document: title='{}', sourceType={}, sourceId={}", 
            request.getTitle(), request.getSourceType(), request.getSourceId());

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest()
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Content is required for text documents")
                    .build());
        }

        try {
            int chunkCount = ingestionService.ingestTextDocument(
                request.getContent(),
                request.buildMetadata()
            );

            return ResponseEntity.ok(DocumentUploadResponse.builder()
                .success(true)
                .message("Document uploaded successfully")
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .chunkCount(chunkCount)
                .build());
        } catch (Exception e) {
            log.error("Failed to upload text document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to upload document: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Upload PDF file to knowledge base
     * 
     * Example using curl:
     * curl -X POST http://localhost:8080/api/knowledge-base/documents/pdf \
     *   -F "file=@document.pdf" \
     *   -F "title=Product Manual" \
     *   -F "sourceType=PRODUCT" \
     *   -F "sourceId=product_123"
     */
    @PostMapping(value = "/documents/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload PDF document", description = "Upload PDF file to knowledge base")
    public ResponseEntity<DocumentUploadResponse> uploadPdfDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("sourceType") String sourceType,
        @RequestParam("sourceId") String sourceId,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "language", required = false) String language
    ) {
        log.info("Uploading PDF document: title='{}', filename='{}', size={} bytes", 
            title, file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("File is empty")
                    .build());
        }

        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest()
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Only PDF files are supported")
                    .build());
        }

        try {
            // Convert MultipartFile to Resource
            Resource pdfResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // Build metadata
            var metadata = new java.util.HashMap<String, Object>();
            metadata.put("title", title);
            metadata.put("source_type", sourceType);
            metadata.put("source_id", sourceId);
            metadata.put("filename", file.getOriginalFilename());
            if (category != null) metadata.put("category", category);
            if (language != null) metadata.put("language", language);

            int chunkCount = ingestionService.ingestPdfDocument(pdfResource, metadata);

            return ResponseEntity.ok(DocumentUploadResponse.builder()
                .success(true)
                .message("PDF uploaded successfully")
                .sourceType(sourceType)
                .sourceId(sourceId)
                .chunkCount(chunkCount)
                .build());
        } catch (IOException e) {
            log.error("Failed to read PDF file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to read PDF file: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Failed to upload PDF document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to upload PDF: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Upload Word file to knowledge base
     * 
     * Example using curl:
     * curl -X POST http://localhost:8080/api/knowledge-base/documents/word \
     *   -F "file=@document.docx" \
     *   -F "title=Product Manual" \
     *   -F "sourceType=PRODUCT" \
     *   -F "sourceId=product_123"
     */
    @PostMapping(value = "/documents/word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Word document", description = "Upload Word (.docx) file to knowledge base")
    public ResponseEntity<DocumentUploadResponse> uploadWordDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("sourceType") String sourceType,
        @RequestParam("sourceId") String sourceId,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "language", required = false) String language
    ) {
        log.info("Uploading Word document: title='{}', filename='{}', size={} bytes", 
            title, file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("File is empty")
                    .build());
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.contains("wordprocessingml")) {
            return ResponseEntity.badRequest()
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Only Word (.docx) files are supported")
                    .build());
        }

        try {
            // Convert MultipartFile to Resource
            Resource wordResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // Build metadata
            var metadata = new java.util.HashMap<String, Object>();
            metadata.put("title", title);
            metadata.put("source_type", sourceType);
            metadata.put("source_id", sourceId);
            metadata.put("filename", file.getOriginalFilename());
            if (category != null) metadata.put("category", category);
            if (language != null) metadata.put("language", language);

            int chunkCount = ingestionService.ingestWordDocument(wordResource, metadata);

            return ResponseEntity.ok(DocumentUploadResponse.builder()
                .success(true)
                .message("Word document uploaded successfully")
                .sourceType(sourceType)
                .sourceId(sourceId)
                .chunkCount(chunkCount)
                .build());
        } catch (IOException e) {
            log.error("Failed to read Word file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to read Word file: " + e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Failed to upload Word document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to upload Word: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Search knowledge base using semantic search
     * 
     * Example request:
     * POST /api/knowledge-base/search
     * {
     *   "query": "How to use iPhone camera?",
     *   "topK": 5,
     *   "similarityThreshold": 0.7,
     *   "sourceType": "PRODUCT"
     * }
     */
    @PostMapping("/search")
    @Operation(summary = "Semantic search", description = "Search knowledge base using semantic similarity")
    public ResponseEntity<RAGSearchResponse> search(
        @Valid @RequestBody RAGSearchRequest request
    ) {
        log.info("Searching knowledge base: query='{}', topK={}", request.getQuery(), request.getTopK());

        try {
            RAGSearchResponse response = ragService.search(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to search knowledge base", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RAGSearchResponse.builder()
                    .query(request.getQuery())
                    .totalResults(0)
                    .results(java.util.Collections.emptyList())
                    .context("Error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Delete document by source
     * 
     * Example: DELETE /api/knowledge-base/documents/PRODUCT/product_123
     */
    @DeleteMapping("/documents/{sourceType}/{sourceId}")
    @Operation(summary = "Delete document", description = "Delete document by source type and ID")
    public ResponseEntity<DocumentUploadResponse> deleteDocument(
        @PathVariable String sourceType,
        @PathVariable String sourceId
    ) {
        log.info("Deleting document: sourceType={}, sourceId={}", sourceType, sourceId);

        try {
            boolean deleted = ingestionService.deleteDocumentBySource(sourceType, sourceId);
            
            if (deleted) {
                return ResponseEntity.ok(DocumentUploadResponse.builder()
                    .success(true)
                    .message("Document deleted successfully")
                    .sourceType(sourceType)
                    .sourceId(sourceId)
                    .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(DocumentUploadResponse.builder()
                        .success(false)
                        .message("Document not found")
                        .sourceType(sourceType)
                        .sourceId(sourceId)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to delete document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to delete document: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Delete all documents of a specific type
     * 
     * Example: DELETE /api/knowledge-base/documents/type/PRODUCT
     */
    @DeleteMapping("/documents/type/{sourceType}")
    @Operation(summary = "Delete documents by type", description = "Delete all documents of a specific source type")
    public ResponseEntity<DocumentUploadResponse> deleteDocumentsByType(
        @PathVariable String sourceType
    ) {
        log.info("Deleting all documents of type: {}", sourceType);

        try {
            int count = ingestionService.deleteDocumentsByType(sourceType);
            
            return ResponseEntity.ok(DocumentUploadResponse.builder()
                .success(true)
                .message(String.format("Deleted %d documents", count))
                .sourceType(sourceType)
                .chunkCount(count)
                .build());
        } catch (Exception e) {
            log.error("Failed to delete documents by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DocumentUploadResponse.builder()
                    .success(false)
                    .message("Failed to delete documents: " + e.getMessage())
                    .build());
        }
    }
}
