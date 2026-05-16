package com.theblood.cartservice.service;

import com.theblood.cartservice.service.dto.request.AddToCartRequest;
import com.theblood.cartservice.service.dto.request.SelectionUpdateRequest;
import com.theblood.cartservice.service.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addItem(String userId, AddToCartRequest request);

    CartResponse updateItemQuantity(String userId, String sku, Integer quantity);

    CartResponse removeItem(String userId, String sku);

    void clearCart(String userId);

    /**
     * Toggle selection cho item / shop / cart.
     * Logic ưu tiên: selectAll > shopId > items[].
     */
    CartResponse toggleSelection(String userId, SelectionUpdateRequest request);
}
