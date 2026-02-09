package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.MessageReaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MessageReaction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {}
