package com.theblood.shopservice.service;


import com.theblood.shopservice.dto.request.ShopRequest;
import com.theblood.shopservice.dto.response.ShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShopService {

    Page<ShopResponse> getAllShops(Pageable pageable);

    ShopResponse getShopById(String shopId);

    ShopResponse getShop(String userId);

    ShopResponse shopRegister(ShopRequest shopRequest);

    boolean isUserOwnShop(UUID userId, UUID shopId);
}
