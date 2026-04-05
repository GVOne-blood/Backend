package com.theblood.orderservice.repository;

import com.theblood.springfood.common.enums.MessageStatus;
import com.theblood.orderservice.kafka.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(MessageStatus status);
}
