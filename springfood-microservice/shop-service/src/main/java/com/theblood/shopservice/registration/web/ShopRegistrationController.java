package com.theblood.shopservice.registration.web;

import com.theblood.shopservice.dto.request.ShopRequest;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep1Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep2Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep3Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep4Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStepResponse;
import com.theblood.shopservice.registration.service.ShopRegistrationService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import javax.naming.AuthenticationException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/shop-registration")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopRegistrationController {

    ShopRegistrationService shopRegistrationService;

    @PostMapping("")
    public ResponseEntity<?> submitRegistration(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody ShopRequest request
    ) throws AuthenticationException {
        UUID userId = resolveUserId(principal);
        UUID requestId = shopRegistrationService.submitRegistration(request, userId);
        return ResponseEntity.ok(new ResponseData<>(200, "Shop registration submitted", Map.of("requestId", requestId)));
    }

    @PostMapping("/step-1")
    public ResponseEntity<?> submitStep1(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody ShopRegistrationStep1Request request
    ) throws AuthenticationException {
        UUID userId = resolveUserId(principal);
        UUID requestId = shopRegistrationService.submitStep1(request, userId);
        return ResponseEntity.ok(new ResponseData<>(200, "Step 1 saved",
            ShopRegistrationStepResponse.builder().requestId(requestId).status("DRAFT").build()));
    }

    @PostMapping("/step-2")
    public ResponseEntity<?> submitStep2(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody ShopRegistrationStep2Request request
    ) throws AuthenticationException {
        UUID userId = resolveUserId(principal);
        UUID requestId = shopRegistrationService.submitStep2(request, userId);
        return ResponseEntity.ok(new ResponseData<>(200, "Step 2 saved",
            ShopRegistrationStepResponse.builder().requestId(requestId).status("DRAFT").build()));
    }

    @PostMapping("/step-3")
    public ResponseEntity<?> submitStep3(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody ShopRegistrationStep3Request request
    ) throws AuthenticationException {
        UUID userId = resolveUserId(principal);
        UUID requestId = shopRegistrationService.submitStep3(request, userId);
        return ResponseEntity.ok(new ResponseData<>(200, "Step 3 saved",
            ShopRegistrationStepResponse.builder().requestId(requestId).status("DRAFT").build()));
    }

    @PostMapping("/step-4")
    public ResponseEntity<?> submitStep4(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody ShopRegistrationStep4Request request
    ) throws AuthenticationException {
        UUID userId = resolveUserId(principal);
        UUID requestId = shopRegistrationService.submitStep4(request, userId);
        return ResponseEntity.ok(new ResponseData<>(200, "Step 4 submitted",
            ShopRegistrationStepResponse.builder().requestId(requestId).status("PENDING").build()));
    }

    /**
     * Resolves the authenticated user from {@link CustomUserPrincipal} populated
     * by the internal authentication filter. Throws when the request doesn't
     * carry a verified identity.
     */
    private static UUID resolveUserId(CustomUserPrincipal principal) throws AuthenticationException {
        if (principal == null || principal.getUserId() == null) {
            throw new AuthenticationException("Authentication required");
        }
        return principal.getUserId();
    }
}
