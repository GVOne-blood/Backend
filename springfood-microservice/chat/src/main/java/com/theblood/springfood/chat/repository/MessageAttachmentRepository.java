package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.MessageAttachment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MessageAttachment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {}
