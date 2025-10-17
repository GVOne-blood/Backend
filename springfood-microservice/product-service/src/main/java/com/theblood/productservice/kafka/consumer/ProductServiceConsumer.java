package com.theblood.productservice.kafka.consumer;

import com.theblood.common.enums.kafka.ProductCreationMessage;
import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.common.exception.custom.KafkaMessageIsFailException;
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
public class ProductServiceConsumer {

    @KafkaListener(topics = "product-creation-validated")
    public boolean handleProductCreationValidated(String message) {
        log.info("Received product creation validated message: {}", message);
        if (message != null || message.isEmpty())
            throw new InvalidDataException("Invalid product creation request : Response message after validated product is null or empty");
        if (message.equals(ProductCreationMessage.PRODUCT_VALIDATION_SUCCESS.name())) {
            return true;
        }
        // xu ly gi do ...
        throw new KafkaMessageIsFailException(ProductCreationMessage.PRODUCT_VALIDATION_FAILED.name());
    }
}
