package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.TypingIndicator;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TypingIndicator entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypingIndicatorRepository extends JpaRepository<TypingIndicator, String> {}
