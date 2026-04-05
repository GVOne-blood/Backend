package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.ConversationParticipant;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the ConversationParticipant entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, String> {

    /**
     * Check if a user is an ACTIVE participant of a conversation
     */
    boolean existsByConversation_ConversationIdAndUserIdAndStatus(String conversationId, String userId, String status);

    /**
     * Find a participant by conversation and user ID
     */
    Optional<ConversationParticipant> findByConversation_ConversationIdAndUserId(String conversationId, String userId);

    /**
     * Find all participants of a conversation with a specific status.
     * Used for broadcasting messages to ACTIVE participants.
     *
     * @param conversationId The conversation ID
     * @param status The participant status (e.g., "ACTIVE")
     * @return List of participants matching the criteria
     */
    List<ConversationParticipant> findByConversation_ConversationIdAndStatus(String conversationId, String status);

    /**
     * Find all ACTIVE participants of a conversation except the sender.
     * Used for incrementing unread counts when a new message is persisted.
     *
     * @param conversationId The conversation ID
     * @param excludeUserId The user ID to exclude (typically the sender)
     * @param status The participant status (e.g., "ACTIVE")
     * @return List of participants matching the criteria
     */
    List<ConversationParticipant> findByConversation_ConversationIdAndStatusAndUserIdNot(
        String conversationId,
        String status,
        String excludeUserId
    );

    /**
     * Find participant by conversation ID and user ID (alternative method name for tests)
     */
    Optional<ConversationParticipant> findByConversationConversationIdAndUserId(String conversationId, String userId);
}
