package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.ConversationSettings;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ConversationSettings entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConversationSettingsRepository extends JpaRepository<ConversationSettings, String> {}
