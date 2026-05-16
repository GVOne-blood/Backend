package com.theblood.shopservice.controller;

import com.theblood.shopservice.dto.request.StaffRequest;
import com.theblood.shopservice.dto.response.StaffResponse;
import com.theblood.shopservice.service.StaffService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop/staff")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class StaffController {

    StaffService staffService;

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping
    public ResponseEntity<ResponseData<Page<StaffResponse>>> getStaffList(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        Page<StaffResponse> staff = staffService.getStaffByShopId(user.getShopId(), pageable);
        return ResponseEntity.ok(new ResponseData<>(200, "Get staff list successfully", staff));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<StaffResponse>> getStaffDetail(
            @PathVariable("id") String staffId,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        StaffResponse staff = staffService.getStaffDetail(user.getShopId(), staffId);
        return ResponseEntity.ok(new ResponseData<>(200, "Get staff detail successfully", staff));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping
    public ResponseEntity<ResponseData<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        StaffResponse staff = staffService.createStaff(user.getShopId(), request);
        return new ResponseEntity<>(new ResponseData<>(201, "Create staff successfully", staff), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<StaffResponse>> updateStaff(
            @PathVariable("id") String staffId,
            @Valid @RequestBody StaffRequest request,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        StaffResponse staff = staffService.updateStaff(user.getShopId(), staffId, request);
        return ResponseEntity.ok(new ResponseData<>(200, "Update staff successfully", staff));
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteStaff(
            @PathVariable("id") String staffId,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        staffService.deleteStaff(user.getShopId(), staffId);
        return ResponseEntity.ok(new ResponseData<>(204, "Delete staff successfully", null));
    }
}
