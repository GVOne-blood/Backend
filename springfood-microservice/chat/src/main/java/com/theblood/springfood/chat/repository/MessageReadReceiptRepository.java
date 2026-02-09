package com.theblood.springfood.chat.repository;

import com.theblood.springfood.chat.domain.MessageReadReceipt;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MessageReadReceipt entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageReadReceiptRepository extends JpaRepository<MessageReadReceipt, String> {}
