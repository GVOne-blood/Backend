package com.theblood.orderservice.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.common.dto.kafka.Event;
import com.theblood.common.dto.kafka.OrderCreationEvent;
import com.theblood.common.dto.request.ItemRequest;
import com.theblood.common.enums.MessageStatus;
import com.theblood.common.enums.kafka.SagaOrderEventType;
import com.theblood.orderservice.kafka.model.OutboxMessage;
import com.theblood.orderservice.model.OrderItem;
import com.theblood.orderservice.repository.OrderItemRepository;
import com.theblood.orderservice.repository.OutboxMessageRepository;
import com.theblood.orderservice.service.OrderItemService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemServiceImpl implements OrderItemService {

    ObjectMapper objectMapper;
    OrderItemRepository orderItemRepository;
    OutboxMessageRepository outboxMessageRepository;

    @KafkaListener(topics = "order-creation-saga")
    @Transactional
    public void addOrderItems(String payload) throws JsonProcessingException {

        Event event = objectMapper.convertValue(payload, Event.class);
        OrderCreationEvent orderCreationEvent = (OrderCreationEvent) event.getPayload();

        try {
            List<ItemRequest> products = orderCreationEvent.getProducts();
            for (ItemRequest product : products) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(orderCreationEvent.getOrderId());
                orderItem.setProductId(UUID.fromString(product.getProductId()));
                orderItem.setProductName(product.getProductName());
                orderItem.setQuantity(product.getQuantity());
                orderItem.setPriceAtBooking(product.getPrice());
                orderItemRepository.save(orderItem);
            }


            outboxMessageRepository.save(OutboxMessage.builder()
                    .topic("order-creation-saga")
                    .messageId(event.getTransactionId())
                    .payload(objectMapper.writeValueAsString(Event.builder()
                            .eventType(SagaOrderEventType.ORDER_ITEM_PROCESSED.name())
                            .transactionId(event.getTransactionId())
                            .payload(orderCreationEvent)
                            .build()))
                    .status(MessageStatus.PENDING)
                    .build());
        } catch (Exception e) {
            outboxMessageRepository.save(OutboxMessage.builder()
                    .topic("order-creation-saga")
                    .messageId(event.getTransactionId())
                    .payload(objectMapper.writeValueAsString(Event.builder()
                            .eventType(SagaOrderEventType.ORDER_ITEM_PROCESS_FAILED.name())
                            .transactionId(event.getTransactionId())
                            .reason("Fail to save orderItem. Rolling back because : " + e.getMessage())
                            .payload(orderCreationEvent)
                            .build()))
                    .status(MessageStatus.PENDING)
                    .build());
        }
    }

    @KafkaListener(topics = "order-creation-saga")
    @Transactional
    public void handleOrderItemRollback(String payload) throws JsonProcessingException {

        Event event = objectMapper.convertValue(payload, Event.class);
        OrderCreationEvent orderCreationEvent = (OrderCreationEvent) event.getPayload();
        //rollback
        orderItemRepository.deleteAllByOrderId(orderCreationEvent.getOrderId());

        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setMessageId(event.getTransactionId());
        outboxMessage.setPayload(objectMapper.writeValueAsString(Event.builder()
                .payload(orderCreationEvent)
                .transactionId(event.getTransactionId())
                .eventType(SagaOrderEventType.PRODUCT_PROCESSED_FAILED.name())
                .build()));
        outboxMessage.setTopic("order-creation-saga");
        outboxMessageRepository.save(outboxMessage);
    }

    @Override
    public void addOrderItem() {

        OrderItem orderItem = new OrderItem();

    }

    @Override
    public OrderItem updateOrderItem() {
        return null;
    }
}
