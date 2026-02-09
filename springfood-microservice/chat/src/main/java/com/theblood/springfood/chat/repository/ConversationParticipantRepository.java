package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.ConversationParticipant;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ConversationParticipant entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, String> {}
