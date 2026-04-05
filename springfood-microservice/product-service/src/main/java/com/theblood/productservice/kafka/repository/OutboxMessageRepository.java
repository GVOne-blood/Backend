package com.theblood.productservice.kafka.repository;

import com.theblood.springfood.common.enums.MessageStatus;
import com.theblood.productservice.kafka.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(MessageStatus status);
}
