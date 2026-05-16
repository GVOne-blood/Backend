package com.theblood.shopservice.registration.web;

import com.theblood.shopservice.registration.dto.AdminRejectRegistrationRequest;
import com.theblood.shopservice.registration.dto.AdminShopRegistrationView;
import com.theblood.shopservice.registration.service.AdminShopRegistrationService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin endpoints quản lý đơn đăng ký mở shop.
 *
 * <p>Tất cả đều yêu cầu role ADMIN — gateway đã filter, đây là defence-in-depth.</p>
 *
 * <pre>
 * GET    /shop-registration/admin              List + filter (status, search)
 * GET    /shop-registration/admin/{requestId}  Get detail
 * POST   /shop-registration/admin/{id}/approve Approve → tạo Shop entity ACTIVE
 * POST   /shop-registration/admin/{id}/reject  Reject với reason bắt buộc
 * </pre>
 */
@RestController
@RequestMapping("/shop-registration/admin")
@RequiredArgsConstructor
public class AdminShopRegistrationController {

    private final AdminShopRegistrationService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ResponseData<Page<AdminShopRegistrationView>>> list(
        @RequestParam(required = false, defaultValue = "all") String status,
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminShopRegistrationView> page = adminService.list(status, search, pageable);
        return ResponseEntity.ok(new ResponseData<>(200, "OK", page));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{requestId}")
    public ResponseEntity<ResponseData<AdminShopRegistrationView>> get(@PathVariable UUID requestId) {
        return ResponseEntity.ok(new ResponseData<>(200, "OK", adminService.get(requestId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ResponseData<AdminShopRegistrationView>> approve(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable UUID requestId
    ) {
        String username = principal != null ? principal.getUsername() : "system";
        AdminShopRegistrationView view = adminService.approve(requestId, username);
        return ResponseEntity.ok(new ResponseData<>(200, "Approved", view));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ResponseData<AdminShopRegistrationView>> reject(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable UUID requestId,
        @Valid @RequestBody AdminRejectRegistrationRequest body
    ) {
        String username = principal != null ? principal.getUsername() : "system";
        AdminShopRegistrationView view = adminService.reject(requestId, body.getReason(), username);
        return ResponseEntity.ok(new ResponseData<>(200, "Rejected", view));
    }
}
