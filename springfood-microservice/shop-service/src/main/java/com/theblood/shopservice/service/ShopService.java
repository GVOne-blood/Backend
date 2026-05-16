package com.theblood.shopservice.service;


import com.theblood.shopservice.dto.request.ShopRequest;
import com.theblood.shopservice.dto.response.ShopDetailResponse;
import com.theblood.shopservice.dto.response.ShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShopService {

    Page<ShopResponse> getAllShops(Pageable pageable);

    ShopResponse getShopById(String shopId);

    ShopResponse getShop(String userId);

    /**
     * Find the shop owned by the supplied user, returning {@link Optional#empty()}
     * when the user has no shop. This variant exists so HTTP layers can map
     * "no shop yet" to a 204 instead of a 404.
     */
    java.util.Optional<ShopDetailResponse> findShopOfUser(String userId);

    ShopResponse shopRegister(ShopRequest shopRequest);

    boolean isUserOwnShop(UUID userId, UUID shopId);

    Page<ShopResponse> getFeaturedShops(Pageable pageable);
    
    /**
     * Get shop detail information by shop ID
     * @param shopId Shop ID
     * @return Shop detail information
     */
    ShopDetailResponse getShopDetail(String shopId);
}
