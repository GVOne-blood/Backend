package com.theblood.cartservice.service;

import com.theblood.cartservice.service.dto.response.CartDetailResponse;

import java.util.UUID;


public interface CartService {

    CartDetailResponse getCartDetails(UUID cartId);

    CartDetailResponse addToCart(UUID cartId, UUID productId, String sku, Integer quantity);

}
