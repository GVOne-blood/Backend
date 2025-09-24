package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.response.CartDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CartService {

    Page<CartDetailResponse> getCartDetail(Pageable pageable, String userId);

    void addToCart(String userId, String productId, int quantity);

    void deleteProductInCart(List<String> listProductId, String userId);

    void clearCartByUserId(String userId);
}
