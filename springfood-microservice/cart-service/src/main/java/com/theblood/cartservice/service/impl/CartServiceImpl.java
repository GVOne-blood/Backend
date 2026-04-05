package com.theblood.cartservice.service.impl;

import com.theblood.cartservice.service.CartService;
import com.theblood.cartservice.service.dto.response.CartDetailResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {

    @Override
    public CartDetailResponse getCartDetails(UUID cartId) {
        return null;
    }

    @Override
    public CartDetailResponse addToCart(UUID cartId, UUID productId, String sku, Integer quantity) {
        return null;
    }
}
