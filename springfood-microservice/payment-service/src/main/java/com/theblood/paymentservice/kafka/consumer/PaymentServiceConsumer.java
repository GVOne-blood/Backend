package com.theblood.paymentservice.kafka.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.common.dto.kafka.Event;
import com.theblood.springfood.common.dto.kafka.OrderCreationEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceConsumer {

    ObjectMapper objectMapper;

    /**
     *
     * @param payload: String (DB) -> Event (common)
     */
    // pay online with vnpay
    @KafkaListener(topics = "order-creation-saga")
    public void handleOnlineOrderPayment(String payload) {

        Event event = objectMapper.convertValue(payload, Event.class);
        OrderCreationEvent orderCreationEvent = (OrderCreationEvent) event.getPayload();

    }

    @KafkaListener(topics = "cod-transaction-event")
    public void handleCodOrderPayment(String payload) {

    }
}
