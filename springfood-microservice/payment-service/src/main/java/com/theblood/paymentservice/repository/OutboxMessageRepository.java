package com.theblood.paymentservice.repository;

import com.theblood.common.enums.MessageStatus;
import com.theblood.paymentservice.kafka.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(MessageStatus status);
    
}
