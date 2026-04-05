package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Message entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * Find messages by conversation ID, excluding deleted messages, ordered by created date descending
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId AND m.isDeleted = 0 ORDER BY m.createdDate DESC")
    Page<Message> findMessageHistory(@Param("conversationId") String conversationId, Pageable pageable);

    /**
     * Check if a message with the given clientMessageId already exists.
     * Used for deduplication during message persistence.
     */
    boolean existsByClientMessageId(String clientMessageId);

    /**
     * Find all messages by conversation ID (including deleted)
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId")
    java.util.List<Message> findByConversationConversationId(@Param("conversationId") String conversationId);

    /**
     * Find messages by conversation ID ordered by created date descending
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId ORDER BY m.createdDate DESC")
    java.util.List<Message> findByConversationConversationIdOrderByCreatedAtDesc(@Param("conversationId") String conversationId);
}
