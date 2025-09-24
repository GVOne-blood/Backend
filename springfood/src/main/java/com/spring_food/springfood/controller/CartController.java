package com.spring_food.springfood.controller;

import com.spring_food.springfood.dto.response.CartDetailResponse;
import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<?>> viewCart(
            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        if (user.getId().isEmpty()) return ResponseEntity.notFound().build();
        Page<CartDetailResponse> res = cartService.getCartDetail(pageable, user.getId());

        return ResponseEntity.ok(new ResponseData<>(200, "get cart detail successfully", res));
    }

    @PostMapping("/")
    public ResponseEntity<ResponseData<?>> addProductToCart(
            @AuthenticationPrincipal String userId,
            @RequestBody String product_id,
            @RequestBody int quantity
    ) {

        cartService.addToCart(userId, product_id, quantity);

        return ResponseEntity.ok(new ResponseData<>(200, "add product to cart successfully", null));
    }

    @DeleteMapping
    public ResponseEntity<ResponseData<?>> clearCart(@AuthenticationPrincipal String userId) {
        try {
            cartService.clearCartByUserId(userId);
            return ResponseEntity.ok(new ResponseData<>(200, "clear cart successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseData<>(500, "clear cart failed", null));
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<ResponseData<?>> deleteProductInCart(@AuthenticationPrincipal User user,
                                                               @RequestBody List<String> productIds) {
        try {
            cartService.deleteProductInCart(productIds, user.getId());
            return ResponseEntity.ok(new ResponseData<>(200, "clear cart successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResponseData<>(500, "clear cart failed : " + e.getMessage(), null));
        }
    }

}
