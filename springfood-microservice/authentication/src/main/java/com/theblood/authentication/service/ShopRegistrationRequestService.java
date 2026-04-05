package com.theblood.authentication.service;

import com.theblood.springfood.client.api.ShopClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopRegistrationRequestService {


    Page<ShopClient.ShopApproveResponse> getListShopRegistrationRequest(Pageable pageable, String adminId);

    ShopClient.ShopApproveResponse approveShop(ShopClient.ShopApproveDTO shopApproveDTO);

    ShopClient.ShopApproveResponse approveShop(String requestId);
}
