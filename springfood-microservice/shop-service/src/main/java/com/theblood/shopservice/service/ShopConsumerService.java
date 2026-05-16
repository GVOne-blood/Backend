package com.theblood.shopservice.service;

import com.theblood.shopservice.repository.ShopRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// import com.theblood.springfood.common.dto.kafka.AvatarEvent;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopConsumerService {
    KafkaTemplate<String, Object> kafkaTemplate;
    ShopRepository shopRepository;

    // TODO: Uncomment when AvatarEvent is implemented
    /*
    @KafkaListener(topics = "media-shop-avatar", groupId = "shop-service")
    public void getShopAvatarFromMedia(@Payload AvatarEvent avatarEvent) {

        shopRepository.findById(UUID.fromString(avatarEvent.getObejctId())).ifPresent(shop -> {
            shop.setLogo(avatarEvent.getAvatarUrl());
            shopRepository.save(shop);
        });

    }
    */
}
