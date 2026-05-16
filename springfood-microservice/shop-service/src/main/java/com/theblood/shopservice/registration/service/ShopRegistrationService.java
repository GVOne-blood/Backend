package com.theblood.shopservice.registration.service;

import com.theblood.shopservice.dto.request.ShopRequest;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep1Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep2Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep3Request;
import com.theblood.shopservice.registration.dto.ShopRegistrationStep4Request;
import java.util.UUID;

public interface ShopRegistrationService {
    UUID submitRegistration(ShopRequest request, UUID userId);

    UUID submitStep1(ShopRegistrationStep1Request request, UUID userId);

    UUID submitStep2(ShopRegistrationStep2Request request, UUID userId);

    UUID submitStep3(ShopRegistrationStep3Request request, UUID userId);

    UUID submitStep4(ShopRegistrationStep4Request request, UUID userId);
}
