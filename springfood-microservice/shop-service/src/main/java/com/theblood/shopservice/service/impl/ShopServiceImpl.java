package com.theblood.shopservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.shopservice.domain.Shop;
import com.theblood.shopservice.dto.request.ShopRequest;
import com.theblood.shopservice.dto.response.ShopResponse;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.repository.ShopRepository;
import com.theblood.shopservice.service.ShopConsumerService;
import com.theblood.shopservice.service.ShopService;
import com.theblood.springfood.client.service.LoggingService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import com.theblood.springfood.common.enums.Role;
import com.theblood.springfood.common.exception.custom.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopServiceImpl implements ShopService {

    ShopRepository shopRepository;
    ShopConsumerService shopConsumerService;
    ShopMemberRepository shopMemberRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    LoggingService loggingService;
    ObjectMapper objectMapper;

    @Override
    public Page<ShopResponse> getAllShops(Pageable pageable) {
        return null;
    }

    @Override
    public ShopResponse getShopById(String shopId) {
        return null;
    }

    @Override
    public ShopResponse getShop(String userId) {
        Optional<Shop> shop = shopRepository.findShopByOwnerId(UUID.fromString(userId));
        if (shop.isEmpty()) throw new NotFoundException("Shop not found for user id: " + userId);

        ShopResponse res = objectMapper.convertValue(shop.get(), ShopResponse.class);
        return res;
    }

    @Override
    @Transactional
    public ShopResponse shopRegister(ShopRequest shopRequest) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();

        Shop shop = new Shop();
        shop.setShopName(shopRequest.getShopName());
        shop.setShopAddress(shopRequest.getShopAddress());
        shop.setAvgStar(BigDecimal.ZERO);
        shop.setShopAddress(shopRequest.getShopAddress());
        shop.setIsActive(0);
        shop.setShopType(shopRequest.getShopType());
//        shop.setTotalSold(0);
//        shop.setTotalTraffic(0);
//        shop.setTotalOrders(0);

        //send to admin to get approvement
        ShopResponse req = ShopResponse.builder().build();
        return req;
    }

    @Override
    public boolean isUserOwnShop(UUID userId, UUID shopId) {
        return shopMemberRepository.existsByIdAndUserIdAndRoleName(shopId.toString(), userId.toString(), Role.SHOP_OWNER.name());
    }


}
