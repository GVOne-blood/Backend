package com.theblood.productservice.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.common.enums.MessageStatus;
import com.theblood.productservice.kafka.model.OutboxMessage;
import com.theblood.productservice.kafka.repository.OutboxMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxMessageRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveMessage(String topic, String key, Object payload) throws JsonProcessingException {
        OutboxMessage message = new OutboxMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTopic(topic);
        message.setKey(key);
        message.setPayload(objectMapper.writeValueAsString(payload));
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(0);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        message.setSourceService("product-service");

        repository.save(message);
    }
}
