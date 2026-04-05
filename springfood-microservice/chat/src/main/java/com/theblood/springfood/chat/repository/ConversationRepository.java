package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Conversation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    /**
     * Find all conversations where the user is an ACTIVE participant, ordered by last_message_at DESC
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.userId = :userId AND p.status = 'ACTIVE'
        ORDER BY c.lastMessageAt DESC NULLS LAST
        """)
    Page<Conversation> findUserConversations(@Param("userId") String userId, Pageable pageable);
    
    /**
     * Search conversations by keyword in name or last_message_preview
     * Only returns conversations where the user is an ACTIVE participant
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.userId = :userId 
        AND p.status = 'ACTIVE'
        AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(c.lastMessagePreview) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY c.lastMessageAt DESC NULLS LAST
        """)
    Page<Conversation> searchUserConversations(
        @Param("userId") String userId, 
        @Param("keyword") String keyword, 
        Pageable pageable
    );
}
