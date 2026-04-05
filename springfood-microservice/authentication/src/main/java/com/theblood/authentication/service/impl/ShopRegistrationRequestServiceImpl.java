package com.theblood.authentication.service.impl;

import com.theblood.authentication.common.RegistrationStatus;
import com.theblood.authentication.constant.ShopRegistrationMessage;
import com.theblood.authentication.model.ShopRegistrationRequest;
import com.theblood.authentication.repository.ShopRegistrationRequestRepository;
import com.theblood.authentication.service.ShopRegistrationRequestService;
import com.theblood.authentication.service.mapper.ShopRegistrationMapper;
import com.theblood.springfood.client.api.ShopClient;
import com.theblood.springfood.client.service.LoggingService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShopRegistrationRequestServiceImpl implements ShopRegistrationRequestService {

    ShopRegistrationRequestRepository shopRegistrationRequestRepository;
    LoggingService loggingService;
    ShopRegistrationMapper shopRegistrationMapper;

    @Override
    public Page<ShopClient.ShopApproveResponse> getListShopRegistrationRequest(Pageable pageable, String adminId) {

        Page<ShopRegistrationRequest> requests = shopRegistrationRequestRepository.findAll();
        Page<ShopClient.ShopApproveResponse> res = requests.map(shopRegistrationMapper::toShopApproveResponse);
        return res;
    }

    @Transactional
    @Override
    public ShopClient.ShopApproveResponse approveShop(ShopClient.ShopApproveDTO shopApproveDTO) {

        ShopRegistrationRequest srr = new ShopRegistrationRequest();
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        String username = userContext.getUsername();


        String rejectReason = "Shop chưa cung cấp đầy đủ giấy tờ kinh doanh hoặc thông tin cá nhân hợp lệ";
        boolean isApproved = (shopApproveDTO.isValidBusinessDoc() || shopApproveDTO.isValidEkyc());

        srr.setShopName(shopApproveDTO.getShopName());
        srr.setShopAddress(shopApproveDTO.getShopAddress());
        srr.setEmail(shopApproveDTO.getShopEmail());
        srr.setPhoneNumber(shopApproveDTO.getShopPhoneNumber());
        srr.setNationId(shopApproveDTO.getNationId());
        srr.setLogoMediaId(shopApproveDTO.getShopAvatarUrl());
        srr.setStatus(RegistrationStatus.DRAFT.name());
        srr.setBusinessType(shopApproveDTO.getBusinessType());
        srr.setShopId(UUID.fromString(shopApproveDTO.getShopId()));
        srr.setReviewedBy(username);
        srr.setRejectReason(rejectReason);

        shopRegistrationRequestRepository.save(srr);


        ShopClient.ShopApproveResponse res = ShopClient.ShopApproveResponse.builder()
                .shopId(String.valueOf(srr.getShopId()))
                .shopName(srr.getShopName())
                .isApproved(isApproved)
                .messgage(isApproved == true ? ShopRegistrationMessage.SHOP_APPROVED : ShopRegistrationMessage.SHOP_REJECTED)
                .reason(isApproved == true ? "" : rejectReason)
                .rejectedBy(username) //admin
                .build();


        return null;
    }

    @Override
    public ShopClient.ShopApproveResponse approveShop(String requestId) {

        ShopRegistrationRequest req = shopRegistrationRequestRepository.findById(requestId).get();
        CustomUserPrincipal userContext = UserContextHolder.getContext();
        String username = userContext.getUsername();

        ShopClient.ShopApproveResponse res = shopRegistrationMapper.toShopApproveResponse(req);

        return res;
    }
}
