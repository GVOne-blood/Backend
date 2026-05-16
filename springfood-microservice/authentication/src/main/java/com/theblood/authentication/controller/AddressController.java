package com.theblood.authentication.controller;


import com.theblood.authentication.dto.request.AddressRequest;
import com.theblood.authentication.dto.response.AddressDetail;
import com.theblood.authentication.service.AddressService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("profile/addr")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AddressController {
    AddressService addressService;

    /** GET /api/v1/profile/addr/ */
    @GetMapping("/")
    public ResponseData<List<AddressDetail>> getUserAddresses(
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        return new ResponseData<>(
                200,
                "Get user addresses successfully",
                addressService.findAllUserAddresses(user.getUserId())
        );
    }

    /** POST /api/v1/profile/addr — tạo địa chỉ mới. */
    @PostMapping
    public ResponseEntity<ResponseData<AddressDetail>> createAddress(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressDetail created = addressService.createAddress(user.getUserId(), request);
        return new ResponseEntity<>(
                new ResponseData<>(201, "Address created successfully", created),
                HttpStatus.CREATED
        );
    }

    /** PUT /api/v1/profile/addr/{addressId} — cập nhật địa chỉ. */
    @PutMapping("/{addressId}")
    public ResponseEntity<ResponseData<AddressDetail>> updateAddress(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressDetail updated = addressService.updateAddress(
                user.getUserId(), addressId, request
        );
        return ResponseEntity.ok(
                new ResponseData<>(200, "Address updated successfully", updated)
        );
    }

    /** PATCH /api/v1/profile/addr/{addressId}/default — set làm default. */
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ResponseData<AddressDetail>> setDefault(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable UUID addressId
    ) {
        AddressDetail updated = addressService.setDefaultAddress(user.getUserId(), addressId);
        return ResponseEntity.ok(
                new ResponseData<>(200, "Default address updated", updated)
        );
    }

    /** DELETE /api/v1/profile/addr — xoá nhiều địa chỉ theo list UUID. */
    @DeleteMapping
    public ResponseData<?> deleteUserAddress(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @RequestBody List<UUID> addressIds
    ) {
        try {
            addressService.deleteAddresses(user.getUserId(), addressIds);
            return new ResponseData<>(204, "Addresses deleted successfully", null);
        } catch (Exception ex) {
            return new ResponseData<>(400, "Delete addresses failed: " + ex.getMessage(), null);
        }
    }
}
