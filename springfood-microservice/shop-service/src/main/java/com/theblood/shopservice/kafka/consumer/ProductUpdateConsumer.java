package com.theblood.shopservice.kafka.consumer;

import com.theblood.shopservice.repository.ShopRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)


public class ProductUpdateConsumer {

    ShopRepository shopRepository;
    KafkaTemplate<String, Object> kafkaTemplate;

}
