package com.theblood.shopservice.controller;

import com.theblood.shopservice.common.enums.ShopStatus;
import com.theblood.shopservice.dto.request.AdminBanShopRequest;
import com.theblood.shopservice.dto.request.AdminShopUpdateRequest;
import com.theblood.shopservice.dto.response.AdminShopRowResponse;
import com.theblood.shopservice.dto.response.AdminShopStatsResponse;
import com.theblood.shopservice.service.AdminShopService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin endpoints quản lý shop. Tất cả yêu cầu role ADMIN — gateway đã filter
 * nhưng giữ {@code @PreAuthorize} cho defence-in-depth.
 *
 * <pre>
 * GET    /shop/admin/stats                  Stats cards (total/active/banned…).
 * GET    /shop/admin                        List với filter status + search, paginate.
 * GET    /shop/admin/{shopId}               Detail (kèm stats).
 * PUT    /shop/admin/{shopId}               Cập nhật thông tin shop.
 * PATCH  /shop/admin/{shopId}/status        Đổi status (ACTIVE/INACTIVE/CLOSED).
 * POST   /shop/admin/{shopId}/ban           Ban với reason bắt buộc.
 * POST   /shop/admin/{shopId}/unban         Unban (status → ACTIVE, giữ history).
 * DELETE /shop/admin/{shopId}               Soft-delete (status → CLOSED).
 * </pre>
 */
@RestController
@RequestMapping("/shop/admin")
@RequiredArgsConstructor
public class AdminShopController {

    private final AdminShopService adminShopService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<ResponseData<AdminShopStatsResponse>> getStats() {
        return ResponseEntity.ok(new ResponseData<>(200, "OK", adminShopService.getStats()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ResponseData<Page<AdminShopRowResponse>>> list(
        @RequestParam(required = false, defaultValue = "all") String status,
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(new ResponseData<>(200, "OK",
            adminShopService.listShops(status, search, pageable)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{shopId}")
    public ResponseEntity<ResponseData<AdminShopRowResponse>> get(@PathVariable UUID shopId) {
        return ResponseEntity.ok(new ResponseData<>(200, "OK", adminShopService.getShop(shopId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{shopId}")
    public ResponseEntity<ResponseData<AdminShopRowResponse>> update(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable UUID shopId,
        @Valid @RequestBody AdminShopUpdateRequest body
    ) {
        String username = principal != null ? principal.getUsername() : "system";
        return ResponseEntity.ok(new ResponseData<>(200, "Updated",
            adminShopService.updateShop(shopId, body, username)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{shopId}/status")
    public ResponseEntity<ResponseData<AdminShopRowResponse>> changeStatus(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable UUID shopId,
        @RequestParam("value") ShopStatus value
    ) {
        String username = principal != null ? principal.getUsername() : "system";
        return ResponseEntity.ok(new ResponseData<>(200, "Status updated",
            adminShopService.changeStatus(shopId, value, username)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{shopId}/ban")
    public ResponseEntity<ResponseData<AdminShopRowResponse>> ban(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable UUID shopId,
        @Valid @RequestBody AdminBanShopRequest body
    ) {
        String username = principal != null ? principal.getUsername() : "system";
        return ResponseEntity.ok(new ResponseData<>(200, "Banned",
            adminShopService.banShop(shopId, body.getReason(), username)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{shopId}/unban")
    public ResponseEntity<ResponseData<AdminShopRowResponse>> unban(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable UUID shopId
    ) {
        String username = principal != null ? principal.getUsername() : "system";
        return ResponseEntity.ok(new ResponseData<>(200, "Unbanned",
            adminShopService.unbanShop(shopId, username)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{shopId}")
    public ResponseEntity<ResponseData<Void>> close(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable UUID shopId
    ) {
        String username = principal != null ? principal.getUsername() : "system";
        adminShopService.closeShop(shopId, username);
        return new ResponseEntity<>(new ResponseData<>(204, "Closed", null), HttpStatus.NO_CONTENT);
    }
}
