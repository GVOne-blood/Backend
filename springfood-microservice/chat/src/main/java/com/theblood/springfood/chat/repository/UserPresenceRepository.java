package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.UserPresence;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the UserPresence entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, String> {}
