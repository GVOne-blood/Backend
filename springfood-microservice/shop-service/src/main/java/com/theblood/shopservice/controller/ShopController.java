package com.theblood.shopservice.controller;

import com.theblood.shopservice.dto.response.ShopDetailResponse;
import com.theblood.shopservice.dto.response.ShopResponse;
import com.theblood.shopservice.service.ShopService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class ShopController {

    ShopService shopService;

    @GetMapping("/")
    public ResponseData<Page<ShopResponse>> getAllShops(@PageableDefault(size = 10, page = 0, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return new ResponseData<>(200, "Success", shopService.getAllShops(pageable));
    }

    @GetMapping("")
    ResponseEntity<?> getShopInfo(@RequestParam String shopId) {
        ShopDetailResponse shopDetail = shopService.getShopDetail(shopId);
        return ResponseEntity.ok().body(shopDetail);
    }

    @GetMapping("/featured")
    public ResponseData<Page<ShopResponse>> getFeaturedShops(
            @PageableDefault(size = 10, page = 0, sort = "totalSold", direction = Sort.Direction.DESC) Pageable pageable) {
        return new ResponseData<>(200, "Success", shopService.getFeaturedShops(pageable));
    }

    /**
     * Returns the shop owned by the currently authenticated user.
     *
     * <p>The caller's identity is resolved from the {@link CustomUserPrincipal}
     * populated by {@code InternalAuthenticationFilter} based on the headers
     * the API gateway injects from the validated JWT. The endpoint
     * intentionally returns {@code 204 No Content} when the user has no shop
     * yet so the front-end can distinguish "no shop" from "auth error" without
     * relying on error payloads.</p>
     */
    @GetMapping("/me")
    public ResponseEntity<ShopDetailResponse> getMyShop(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Optional<ShopDetailResponse> shop = shopService.findShopOfUser(principal.getUserId().toString());
        return shop.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Get shop detail information by shop ID
     * Public endpoint - no authentication required
     *
     * @param shopId Shop ID
     * @return Shop detail information
     */
    @GetMapping("/{shopId}")
    public ResponseData<ShopDetailResponse> getShopDetail(@PathVariable String shopId) {
        ShopDetailResponse shopDetail = shopService.getShopDetail(shopId);
        return new ResponseData<>(200, "Success", shopDetail);
    }


}
