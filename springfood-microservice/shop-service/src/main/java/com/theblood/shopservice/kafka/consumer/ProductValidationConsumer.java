package com.theblood.shopservice.kafka.consumer;

import com.theblood.common.dto.kafka.ProductValidationRequest;
import com.theblood.common.enums.kafka.ProductCreationMessage;
import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.shopservice.common.enums.ShopStatus;
import com.theblood.shopservice.model.Shop;
import com.theblood.shopservice.repository.ShopRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductValidationConsumer {

    ShopRepository shopRepository;
    KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "product-validation-request")
    @Transactional
    public void validateProductCreation(ProductValidationRequest message) {
        Optional<Shop> shop = shopRepository.findById(message.getShopId());
        if (shop.isEmpty()) throw new InvalidDataException("Shop not found, create product failed");
        if (shop.get().getShopStatus() != ShopStatus.ACTIVE)
            throw new IllegalArgumentException("Shop status not active");

        kafkaTemplate.send("product-creation-validated", ProductCreationMessage.PRODUCT_VALIDATION_SUCCESS.name());
        log.info("Validated product creation for shop id: {}", message.getShopId());
    }

}
