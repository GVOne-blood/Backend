package com.theblood.cartservice.resources;

import com.theblood.cartservice.service.CartService;
import com.theblood.cartservice.service.dto.request.AddToCartRequest;
import com.theblood.cartservice.service.dto.request.SelectionUpdateRequest;
import com.theblood.cartservice.service.dto.response.CartResponse;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class CartResources {

    CartService cartService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ResponseData<CartResponse>> getCart(@AuthenticationPrincipal CustomUserPrincipal user) {
        CartResponse cart = cartService.getCart(user.getUserId().toString());
        return ResponseEntity.ok(new ResponseData<>(200, "Get cart successfully", cart));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/items")
    public ResponseEntity<ResponseData<CartResponse>> addItem(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        CartResponse cart = cartService.addItem(user.getUserId().toString(), request);
        return ResponseEntity.ok(new ResponseData<>(200, "Item added to cart", cart));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/items/{sku}/quantity")
    public ResponseEntity<ResponseData<CartResponse>> updateQuantity(
            @PathVariable String sku,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        CartResponse cart = cartService.updateItemQuantity(user.getUserId().toString(), sku, quantity);
        return ResponseEntity.ok(new ResponseData<>(200, "Quantity updated", cart));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/items/{sku}")
    public ResponseEntity<ResponseData<CartResponse>> removeItem(
            @PathVariable String sku,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        CartResponse cart = cartService.removeItem(user.getUserId().toString(), sku);
        return ResponseEntity.ok(new ResponseData<>(200, "Item removed from cart", cart));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<ResponseData<Void>> clearCart(@AuthenticationPrincipal CustomUserPrincipal user) {
        cartService.clearCart(user.getUserId().toString());
        return ResponseEntity.ok(new ResponseData<>(204, "Cart cleared", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/items/select")
    public ResponseEntity<ResponseData<CartResponse>> toggleSelection(
            @Valid @RequestBody SelectionUpdateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        CartResponse cart = cartService.toggleSelection(user.getUserId().toString(), request);
        return ResponseEntity.ok(new ResponseData<>(200, "Selection updated", cart));
    }
}
