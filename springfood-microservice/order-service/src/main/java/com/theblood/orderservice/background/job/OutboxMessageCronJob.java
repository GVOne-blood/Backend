package com.theblood.orderservice.background.job;


import com.theblood.common.enums.MessageStatus;
import com.theblood.orderservice.kafka.model.OutboxMessage;
import com.theblood.orderservice.repository.OutboxMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageCronJob {

    private final OutboxMessageRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 500) // Chạy mỗi 5 ms
    @Transactional
    public void processOutboxMessages() {
        List<OutboxMessage> pendingMessages = repository
                .findByStatusOrderByCreatedAtAsc(MessageStatus.PENDING);

        for (OutboxMessage message : pendingMessages) {
            try {
                kafkaTemplate.send(message.getTopic(), message.getKey(), message.getPayload())
                        .get(5, TimeUnit.SECONDS);

                message.setStatus(MessageStatus.SENT);
                message.setUpdatedAt(LocalDateTime.now());
                repository.save(message);

            } catch (Exception e) {
                message.setRetryCount(message.getRetryCount() + 1);
                message.setLastRetryAt(LocalDateTime.now());
                message.setErrorMessage(e.getMessage());
                message.setUpdatedAt(LocalDateTime.now());

                if (message.getRetryCount() >= 3) {
                    message.setStatus(MessageStatus.FAILED);
                }
                repository.save(message);
            }
        }
    }
}
