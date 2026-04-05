package com.theblood.orderservice.kafka.consumer;

import com.theblood.springfood.common.exception.custom.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceConsumer {

    @KafkaListener(topics = "order-address-response")
    public void handleOrderAddress(Object address) {
        if (address == null) throw new InvalidDataException("Invalid Shipping Address");
    }

    @KafkaListener(topics = "order-creation-saga")
    public void handleProductRollback() {

    }

}
