package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.MessageReaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the MessageReaction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {

    /**
     * Find a reaction by message ID, user ID, and emoji
     */
    Optional<MessageReaction> findByMessage_MessageIdAndUserIdAndEmoji(String messageId, String userId, String emoji);

    /**
     * Check if a reaction exists for a message, user, and emoji
     */
    boolean existsByMessage_MessageIdAndUserIdAndEmoji(String messageId, String userId, String emoji);

    /**
     * Find all reactions by message ID
     */
    java.util.List<MessageReaction> findByMessageMessageId(String messageId);

    /**
     * Find reaction by message ID, user ID and emoji (alternative method name for tests)
     */
    Optional<MessageReaction> findByMessageMessageIdAndUserIdAndEmoji(String messageId, String userId, String emoji);
}
