package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.BlockedUser;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BlockedUser entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, String> {}
