package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for KnowledgeDocument entity.
 * Tracks documents ingested into vector store.
 */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {

    /**
     * Find document by source type and source ID.
     */
    Optional<KnowledgeDocument> findBySourceTypeAndSourceId(
        KnowledgeDocument.DocumentSourceType sourceType,
        String sourceId
    );

    /**
     * Find all documents by source type.
     */
    List<KnowledgeDocument> findBySourceType(KnowledgeDocument.DocumentSourceType sourceType);

    /**
     * Find all active documents.
     */
    List<KnowledgeDocument> findByIsActiveTrue();

    /**
     * Check if document exists by source.
     */
    boolean existsBySourceTypeAndSourceId(
        KnowledgeDocument.DocumentSourceType sourceType,
        String sourceId
    );
}
