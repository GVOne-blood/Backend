package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.AIMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for AI Message entity
 */
@Repository
public interface AIMessageRepository extends JpaRepository<AIMessage, String> {

    /**
     * Tìm messages của conversation, sắp xếp theo thời gian tạo
     */
    Page<AIMessage> findByConversationIdAndIsDeletedOrderByCreatedDateAsc(
        String conversationId,
        Integer isDeleted,
        Pageable pageable
    );

    /**
     * Tìm messages của user
     */
    Page<AIMessage> findByUserIdAndIsDeletedOrderByCreatedDateDesc(
        String userId,
        Integer isDeleted,
        Pageable pageable
    );

    /**
     * Đếm số messages trong conversation
     */
    long countByConversationIdAndIsDeleted(String conversationId, Integer isDeleted);

    /**
     * Tìm message cuối cùng của conversation
     */
    AIMessage findFirstByConversationIdAndIsDeletedOrderByCreatedDateDesc(
        String conversationId,
        Integer isDeleted
    );

    /**
     * Search messages by content
     */
    @Query("SELECT m FROM AIMessage m WHERE m.userId = :userId " +
           "AND m.isDeleted = 0 " +
           "AND (LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.contentPreview) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY m.createdDate DESC")
    Page<AIMessage> searchByContent(
        @Param("userId") String userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * Tìm conversations cũ để cleanup
     */
    @Query("SELECT DISTINCT m.conversationId FROM AIMessage m " +
           "WHERE m.createdDate < :cutoffDate " +
           "AND m.isDeleted = 0")
    List<String> findOldConversationIds(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Soft delete tất cả messages của conversation
     */
    @Query("UPDATE AIMessage m SET m.isDeleted = 1, m.deletedAt = :deletedAt " +
           "WHERE m.conversationId = :conversationId")
    void softDeleteByConversationId(
        @Param("conversationId") String conversationId,
        @Param("deletedAt") Instant deletedAt
    );
}
