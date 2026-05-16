package com.theblood.productservice.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.productservice.domain.Product;
import com.theblood.productservice.domain.UserWishlist;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.service.UserWishlistService;
import com.theblood.productservice.service.dto.request.WishlistRequest;
import com.theblood.productservice.service.dto.response.WishlistItemResponse;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Wishlist (Sản phẩm yêu thích) REST API.
 *
 * <p>Base path: <code>/wishlist</code>. Qua API Gateway: <code>/api/v1/products/wishlist/**</code>.
 * Tất cả endpoint yêu cầu user đã đăng nhập — UserContextHolder phải có principal.</p>
 */
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WishlistResource {

    UserWishlistService wishlistService;
    ProductRepository productRepository;
    ObjectMapper objectMapper;

    /** GET /api/v1/products/wishlist?page=0&size=10 */
    @GetMapping
    public ResponseEntity<ResponseData<Page<WishlistItemResponse>>> getWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID userId = currentUserId();
        Page<UserWishlist> raw = wishlistService.getUserWishlist(userId, page, size);
        Page<WishlistItemResponse> enriched = raw.map(this::enrich);
        return ResponseEntity.ok(
                new ResponseData<>(200, "Get wishlist successfully", enriched)
        );
    }

    /** GET /api/v1/products/wishlist/count */
    @GetMapping("/count")
    public ResponseEntity<ResponseData<Long>> getCount() {
        UUID userId = currentUserId();
        return ResponseEntity.ok(
                new ResponseData<>(200, "Get wishlist count", wishlistService.getUserWishlistCount(userId))
        );
    }

    /** GET /api/v1/products/wishlist/check/{productId} — quick check trạng thái yêu thích. */
    @GetMapping("/check/{productId}")
    public ResponseEntity<ResponseData<Boolean>> isInWishlist(@PathVariable UUID productId) {
        UUID userId = currentUserId();
        return ResponseEntity.ok(
                new ResponseData<>(200, "Check status", wishlistService.isInWishlist(userId, productId))
        );
    }

    /** POST /api/v1/products/wishlist — thêm vào wishlist. */
    @PostMapping
    public ResponseEntity<ResponseData<WishlistItemResponse>> addToWishlist(
            @Valid @RequestBody WishlistRequest request
    ) {
        UUID userId = currentUserId();
        UUID productId = UUID.fromString(request.getProductId());
        UUID variantId = request.getVariantId() != null && !request.getVariantId().isBlank()
                ? UUID.fromString(request.getVariantId())
                : null;

        UserWishlist saved = wishlistService.addToWishlist(
                userId, productId, variantId, request.getNote()
        );
        return new ResponseEntity<>(
                new ResponseData<>(201, "Added to wishlist", enrich(saved)),
                HttpStatus.CREATED
        );
    }

    /** DELETE /api/v1/products/wishlist/{productId} — xoá theo product. */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ResponseData<Void>> removeByProduct(@PathVariable UUID productId) {
        UUID userId = currentUserId();
        boolean removed = wishlistService.removeFromWishlist(userId, productId);
        if (!removed) {
            return new ResponseEntity<>(
                    new ResponseData<>(404, "Wishlist item not found", null),
                    HttpStatus.NOT_FOUND
            );
        }
        return ResponseEntity.ok(new ResponseData<>(204, "Removed from wishlist", null));
    }

    /** DELETE /api/v1/products/wishlist — clear toàn bộ. */
    @DeleteMapping
    public ResponseEntity<ResponseData<Void>> clearWishlist() {
        UUID userId = currentUserId();
        wishlistService.clearUserWishlist(userId);
        return ResponseEntity.ok(new ResponseData<>(204, "Wishlist cleared", null));
    }

    // ----------------------- helpers -----------------------

    private UUID currentUserId() {
        CustomUserPrincipal principal = UserContextHolder.getContext();
        if (principal == null || principal.getUserId() == null) {
            throw new IllegalStateException("Authentication required");
        }
        return principal.getUserId();
    }

    /**
     * Enrich UserWishlist với snapshot product info để FE hiển thị card mà
     * không cần gọi thêm endpoint product detail từng item.
     */
    private WishlistItemResponse enrich(UserWishlist w) {
        WishlistItemResponse.WishlistItemResponseBuilder builder = WishlistItemResponse.builder()
                .wishlistId(w.getWishlistId())
                .productId(w.getProductId())
                .variantId(w.getVariantId())
                .note(w.getNote())
                .createdAt(w.getCreatedAt());

        Optional<Product> productOpt = productRepository.findById(w.getProductId());
        if (productOpt.isPresent()) {
            Product p = productOpt.get();
            builder.productName(p.getName())
                    .productPrice(p.getPrice())
                    .productImage(extractFirstImage(p.getImages()))
                    .isAvailable(p.getQuantity() != null && p.getQuantity() > 0);
        } else {
            // Product đã bị xoá nhưng wishlist còn — đánh dấu unavailable
            builder.isAvailable(false);
        }
        return builder.build();
    }

    /**
     * Product.images là JSON string (jsonb). Parse và lấy element đầu tiên.
     * Format giả định: ["url1", "url2"] hoặc [{"url":"...","alt":"..."}].
     */
    private String extractFirstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) return null;
        try {
            // Thử parse list of strings
            List<String> urls = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            return urls.isEmpty() ? null : urls.get(0);
        } catch (Exception ignored) {
            try {
                // Fallback: list of objects với key "url"
                List<Map<String, Object>> items = objectMapper.readValue(
                        imagesJson, new TypeReference<List<Map<String, Object>>>() {}
                );
                if (items.isEmpty()) return null;
                Object url = items.get(0).get("url");
                return url != null ? url.toString() : null;
            } catch (Exception e) {
                log.debug("Cannot parse product.images JSON: {}", imagesJson);
                return null;
            }
        }
    }
}
