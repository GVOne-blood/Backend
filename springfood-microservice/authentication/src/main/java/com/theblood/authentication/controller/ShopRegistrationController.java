package com.theblood.authentication.controller;

import com.theblood.authentication.service.ShopRegistrationRequestService;
import com.theblood.springfood.client.api.ShopClient;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/shop-regis")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopRegistrationController {

    ShopRegistrationRequestService shopRegistrationRequestService;


    @GetMapping("")
    public ResponseEntity<?> getShopRegistrationRequestHis(@PageableDefault Pageable pageable) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        return ResponseEntity.ok().body(shopRegistrationRequestService.getListShopRegistrationRequest(pageable, userContext.getUserIdString()));
    }

    @PostMapping("")
    public ResponseEntity<?> approveShopRegistrationRequest() {
        return ResponseEntity.ok().build();
    }

    @PutMapping("")
    public ResponseEntity<?> updateShopRegistrationRequest(
            @RequestParam String requestId
    ) {
        ShopClient.ShopApproveResponse res = shopRegistrationRequestService.approveShop(requestId);

        return ResponseEntity.ok().body(res);
    }
}
