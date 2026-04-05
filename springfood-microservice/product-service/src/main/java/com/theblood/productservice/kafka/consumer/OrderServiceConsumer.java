package com.theblood.productservice.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.productservice.common.enums.ProductStatus;
import com.theblood.productservice.domain.Product;
import com.theblood.productservice.kafka.model.OutboxMessage;
import com.theblood.productservice.kafka.repository.OutboxMessageRepository;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.service.dto.request.ItemRequest;
import com.theblood.springfood.common.dto.kafka.Event;
import com.theblood.springfood.common.dto.kafka.OrderCreationEvent;
import com.theblood.springfood.common.enums.MessageStatus;
import com.theblood.springfood.common.enums.kafka.SagaOrderEventType;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceConsumer {

    ProductRepository productRepository;
    OutboxMessageRepository outboxMessageRepository;
    ObjectMapper objectMapper;
    KafkaTemplate kafkaTemplate;


    // product process
    @KafkaListener(topics = "order-creation-saga", groupId = "product-service-order-group")
    @Transactional
    public void handleOrderCreate(String payload) throws JsonProcessingException {
        Event event = objectMapper.convertValue(payload, Event.class);

        if (!event.getEventType().equals(SagaOrderEventType.ORDER_CREATED.name()))
            throw new InvalidDataException("Invalid event type : Only accept order-created event");
        List<ItemRequest> products = (List<ItemRequest>) objectMapper.convertValue(event.getPayload(), OrderCreationEvent.class);

        BigDecimal totalPrice = new BigDecimal(0);

        try {
            for (ItemRequest product : products) {
                Optional<Product> optionalProduct = productRepository.findById(product.getProductId());
                if (optionalProduct.isEmpty())
                    throw new InvalidDataException("Product not found");
                if (!optionalProduct.get().getProductStatus().equals(ProductStatus.AVAILABLE))
                    throw new InvalidDataException("Product status not available");
                if (product.getQuantity() < optionalProduct.get().getQuantity())
                    throw new InvalidDataException("Product quantity less than quantity");

                optionalProduct.get().setQuantity(optionalProduct.get().getQuantity() - product.getQuantity());

                BigDecimal itemPrice = optionalProduct.get().getPrice().multiply(BigDecimal.valueOf(product.getQuantity()));

                totalPrice.add(itemPrice);

            }
            OrderCreationEvent response = (OrderCreationEvent) event.getPayload();
            response.builder()
                    .totalPrice(totalPrice)
                    .build();

            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setMessageId(event.getTransactionId());
            outboxMessage.setPayload(objectMapper.writeValueAsString(Event.builder()
                    .payload(response)
                    .transactionId(event.getTransactionId())
                    .eventType(SagaOrderEventType.PRODUCT_PROCESSED.name())
                    .build()));
            outboxMessage.setTopic("order-creation-saga");
            outboxMessageRepository.save(outboxMessage);

        } catch (Exception e) {
            log.error("Error while processing order create -product, will rollback to order service: " + e.getMessage());
            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setStatus(MessageStatus.PENDING);
            outboxMessage.setTopic("order-creation-saga");
            outboxMessage.setMessageId(event.getTransactionId());
            outboxMessage.setPayload(objectMapper.writeValueAsString(Event.builder()
                    .eventType(SagaOrderEventType.PRODUCT_PROCESSED_FAILED.name())
                    .transactionId(event.getTransactionId())
                    .payload("")
                    .reason("product transaction rollback. Stop create order because : " + e.getMessage())
                    .build()));
            outboxMessageRepository.save(outboxMessage);

//            kafkaTemplate.send("order-creation-saga", new Event(SagaOrderEventType.PRODUCT_PROCESSED_FAIL.name(), event.getTransactionId(), "Product validate fail", ""));
        }


    }

    // rollback
    @KafkaListener(topics = "order-creation-saga", groupId = "product-service-rollback-group")
    @Transactional
    public void handleOrderProductRollback(String payload) {
        Event event = objectMapper.convertValue(payload, Event.class);
        if (!event.getEventType().equals(SagaOrderEventType.PRODUCT_PROCESSED_FAILED.name()))
            throw new InvalidDataException("Invalid event type : Only accept product-process-fail event");
        OrderCreationEvent orderCreationEvent = (OrderCreationEvent) event.getPayload();
        List<ItemRequest> products = (List<ItemRequest>) objectMapper.convertValue(event.getPayload(), OrderCreationEvent.class);
        try {
            for (ItemRequest product : products) {
                Optional<Product> optionalProduct = productRepository.findById(product.getProductId());
                //rollback
                optionalProduct.get().setQuantity(optionalProduct.get().getQuantity() + product.getQuantity());
            }
            // String transactionId = UUID.randomUUID().toString();
            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setMessageId(event.getTransactionId());
            outboxMessage.setPayload(objectMapper.writeValueAsString(Event.builder()
                    .payload("rollback completed")
                    .transactionId(event.getTransactionId())
                    .eventType(SagaOrderEventType.PRODUCT_PROCESSED.name())
                    .build()));
            outboxMessage.setTopic("order-creation-saga");
            outboxMessageRepository.save(outboxMessage);

        } catch (Exception e) {

            log.error("Error while processing order create -product, will rollback to order service: " + e.getMessage());
//            kafkaTemplate.send("order-creation-saga", new Event(SagaOrderEventType.PRODUCT_PROCESSED_FAIL.name(), event.getTransactionId(), "Product validate fail", ""));
        }


    }
}
