package com.theblood.shopservice.service.impl;

import com.theblood.common.enums.Role;
import com.theblood.shopservice.dto.response.ShopResponse;
import com.theblood.shopservice.mapper.ShopMapper;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.repository.ShopRepository;
import com.theblood.shopservice.service.ShopService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopServiceImpl implements ShopService {

    ShopRepository shopRepository;
    ShopMemberRepository shopMemberRepository;
    ShopMapper shopMapper;


    @Override
    public Page<ShopResponse> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable).map(shopMapper::toShopResponse);
    }

    @Override
    public boolean isUserOwnShop(UUID userId, UUID shopId) {
        return shopMemberRepository.existsByIdAndUserIdAndRoleName(shopId, userId, Role.SHOP_OWNER.name());
    }


}
