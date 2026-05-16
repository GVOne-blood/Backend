package com.theblood.cartservice.service.impl;

import com.theblood.cartservice.domain.Cart;
import com.theblood.cartservice.domain.CartItemUpdated;
import com.theblood.cartservice.service.CartService;
import com.theblood.cartservice.service.dto.request.AddToCartRequest;
import com.theblood.cartservice.service.dto.request.SelectionUpdateRequest;
import com.theblood.cartservice.service.dto.response.CartItemResponse;
import com.theblood.cartservice.service.dto.response.CartResponse;
import com.theblood.cartservice.service.dto.response.ShopCartGroup;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_HOURS = 72;

    private final RedisServiceWrapper redisServiceWrapper;

    @Override
    public CartResponse getCart(String userId) {
        Cart cart = getCartFromRedis(userId);
        return toCartResponse(cart);
    }

    @Override
    public CartResponse addItem(String userId, AddToCartRequest request) {
        Cart cart = getCartFromRedis(userId);

        // Validate required identifiers up-front so a bad UUID surfaces as a
        // clear 400 instead of a generic 500 from `UUID.fromString`. Earlier
        // FE callers (store-detail's mock products, demo data) sent integer
        // `productId`s like "1" / "2" which made `UUID.fromString` throw an
        // `IllegalArgumentException` — the global handler then mapped it to
        // `500 Internal Server Error`. By validating here we keep the BE
        // response semantics correct while still failing the bad caller.
        UUID productUuid;
        try {
            productUuid = UUID.fromString(request.getProductId());
        } catch (IllegalArgumentException ex) {
            throw new InvalidDataException(
                "productId phải là UUID hợp lệ. Nhận: '" + request.getProductId() + "'"
            );
        }

        UUID shopUuid = null;
        if (request.getShopId() != null && !request.getShopId().isBlank()) {
            try {
                shopUuid = UUID.fromString(request.getShopId());
            } catch (IllegalArgumentException ex) {
                throw new InvalidDataException(
                    "shopId phải là UUID hợp lệ. Nhận: '" + request.getShopId() + "'"
                );
            }
        }

        Optional<CartItemUpdated> existing = cart.getItems().stream()
            .filter(i -> request.getSku() != null ? request.getSku().equals(i.getSku())
                : request.getProductId().equals(i.getProductId().toString()))
            .findFirst();

        if (existing.isPresent()) {
            CartItemUpdated item = existing.get();
            item.setQuantity(item.getQuantity() + (request.getQuantity() != null ? request.getQuantity() : 1));
            item.setUpdatedAt(LocalDateTime.now());
            // Note: KHÔNG đè variantName/attributes của item cũ khi merge
        } else {
            CartItemUpdated newItem = CartItemUpdated.builder()
                .sku(request.getSku())
                .productId(productUuid)
                .productName(request.getProductName())
                .productImage(request.getProductImage())
                .shopId(shopUuid)
                .shopName(request.getShopName())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .discountAmount(request.getDiscountAmount())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .variantName(request.getVariantName())
                .attributes(request.getAttributes())
                .isAvailable(true)
                .selected(true)
                .addedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            newItem.recalculateFinalPrice();
            cart.getItems().add(newItem);
        }

        recalculateCart(cart);
        saveCartToRedis(userId, cart);
        return toCartResponse(cart);
    }

    @Override
    public CartResponse updateItemQuantity(String userId, String sku, Integer quantity) {
        if (quantity < 0) throw new InvalidDataException("Quantity must be >= 0");

        Cart cart = getCartFromRedis(userId);

        CartItemUpdated item = cart.getItems().stream()
            .filter(i -> sku.equals(i.getSku()))
            .findFirst()
            .orElseThrow(() -> new InvalidDataException("Item not found in cart"));

        if (quantity == 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
            item.recalculateFinalPrice();
        }

        recalculateCart(cart);
        saveCartToRedis(userId, cart);
        return toCartResponse(cart);
    }

    @Override
    public CartResponse removeItem(String userId, String sku) {
        Cart cart = getCartFromRedis(userId);
        cart.getItems().removeIf(i -> sku.equals(i.getSku()));
        recalculateCart(cart);
        saveCartToRedis(userId, cart);
        return toCartResponse(cart);
    }

    @Override
    public void clearCart(String userId) {
        redisServiceWrapper.deleteKey(CART_KEY_PREFIX + userId);
    }

    @Override
    public CartResponse toggleSelection(String userId, SelectionUpdateRequest request) {
        if (request == null
            || (request.getSelectAll() == null
                && (request.getShopId() == null || request.getShopId().isBlank())
                && (request.getItems() == null || request.getItems().isEmpty()))) {
            throw new InvalidDataException(
                "SelectionUpdateRequest must contain at least one of: selectAll, shopId, items[]"
            );
        }

        Cart cart = getCartFromRedis(userId);
        List<CartItemUpdated> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Priority 1: selectAll
        if (request.getSelectAll() != null && request.getSelectAll()) {
            boolean target = Boolean.TRUE.equals(request.getSelected());
            for (CartItemUpdated item : items) {
                item.setSelected(target);
                item.setUpdatedAt(now);
            }
        }
        // Priority 2: shopId
        else if (request.getShopId() != null && !request.getShopId().isBlank()) {
            boolean target = Boolean.TRUE.equals(request.getSelected());
            UUID shopUuid;
            try {
                shopUuid = UUID.fromString(request.getShopId());
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException("Invalid shopId format: " + request.getShopId());
            }
            for (CartItemUpdated item : items) {
                if (shopUuid.equals(item.getShopId())) {
                    item.setSelected(target);
                    item.setUpdatedAt(now);
                }
            }
        }
        // Priority 3: items[]
        else {
            Map<String, Boolean> bySku = request.getItems().stream()
                .filter(s -> s != null && s.getSku() != null)
                .collect(Collectors.toMap(
                    SelectionUpdateRequest.SkuSelection::getSku,
                    s -> Boolean.TRUE.equals(s.getSelected()),
                    (a, b) -> b
                ));
            for (CartItemUpdated item : items) {
                Boolean target = bySku.get(item.getSku());
                if (target != null) {
                    item.setSelected(target);
                    item.setUpdatedAt(now);
                }
            }
        }

        recalculateCart(cart);
        saveCartToRedis(userId, cart);
        return toCartResponse(cart);
    }

    private Cart getCartFromRedis(String userId) {
        String key = CART_KEY_PREFIX + userId;
        Object raw = redisServiceWrapper.getValue(key);
        if (raw instanceof Cart) return (Cart) raw;
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setTotalItems(0);
        return cart;
    }

    private void saveCartToRedis(String userId, Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        redisServiceWrapper.setValueWithTimeout(
            CART_KEY_PREFIX + userId,
            cart,
            CART_TTL_HOURS,
            TimeUnit.HOURS
        );
    }

    private void recalculateCart(Cart cart) {
        List<CartItemUpdated> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;
        int totalQty = 0;
        for (CartItemUpdated item : items) {
            if (item.getSelected() != null && item.getSelected()) {
                total = total.add(
                    item.getFinalPrice() != null ? item.getFinalPrice() :
                        item.getPrice() != null ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO
                );
            }
            totalQty += item.getQuantity() != null ? item.getQuantity() : 0;
        }

        cart.setTotalPrice(total);
        cart.setTotalItems(totalQty);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemUpdated> items = cart.getItems() != null ? cart.getItems() : new ArrayList<>();

        Map<String, List<CartItemUpdated>> grouped = items.stream()
            .collect(Collectors.groupingBy(
                i -> i.getShopId() != null ? i.getShopId().toString() : "unknown",
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<ShopCartGroup> shopGroups = new ArrayList<>();
        for (Map.Entry<String, List<CartItemUpdated>> entry : grouped.entrySet()) {
            List<CartItemUpdated> shopItems = entry.getValue();
            CartItemUpdated first = shopItems.get(0);

            List<CartItemResponse> itemResponses = shopItems.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

            BigDecimal shopTotal = itemResponses.stream()
                .filter(CartItemResponse::getSelected)
                .map(r -> r.getFinalPrice() != null ? r.getFinalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            long selectedCount = itemResponses.stream().filter(CartItemResponse::getSelected).count();

            shopGroups.add(ShopCartGroup.builder()
                .shopId(first.getShopId())
                .shopName(first.getShopName())
                .items(itemResponses)
                .shopTotal(shopTotal)
                .selectedTotal(shopTotal)
                .itemCount(shopItems.size())
                .selectedCount((int) selectedCount)
                .allSelected(selectedCount == shopItems.size())
                .hasUnavailableItems(shopItems.stream().anyMatch(i -> !Boolean.TRUE.equals(i.getIsAvailable())))
                .build());
        }

        BigDecimal selectedTotal = items.stream()
            .filter(i -> Boolean.TRUE.equals(i.getSelected()))
            .map(i -> i.getFinalPrice() != null ? i.getFinalPrice() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
            .userId(cart.getUserId())
            .totalPrice(cart.getTotalPrice())
            .selectedTotal(selectedTotal)
            .totalItems(cart.getTotalItems())
            .selectedItems((int) items.stream().filter(i -> Boolean.TRUE.equals(i.getSelected())).count())
            .shopGroups(shopGroups)
            .canCheckout(items.stream().anyMatch(i -> Boolean.TRUE.equals(i.getSelected())))
            .hasUnavailableItems(items.stream().anyMatch(i -> !Boolean.TRUE.equals(i.getIsAvailable())))
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    private CartItemResponse toItemResponse(CartItemUpdated item) {
        return CartItemResponse.builder()
            .sku(item.getSku())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .productImage(item.getProductImage())
            .shopId(item.getShopId())
            .shopName(item.getShopName())
            .originalPrice(item.getOriginalPrice())
            .price(item.getPrice())
            .discountAmount(item.getDiscountAmount())
            .finalPrice(item.getFinalPrice())
            .quantity(item.getQuantity())
            .availableStock(item.getAvailableStock())
            .hasEnoughStock(item.hasEnoughStock())
            .isAvailable(item.getIsAvailable())
            .unavailableReason(item.getUnavailableReason())
            .variantName(item.getVariantName())
            .attributes(item.getAttributes())
            .promotionName(item.getPromotionName())
            .selected(item.getSelected() != null ? item.getSelected() : true)
            .canCheckout(Boolean.TRUE.equals(item.getIsAvailable()))
            .addedAt(item.getAddedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}
