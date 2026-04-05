package com.theblood.springfood.client.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@BaseClient.ServiceClient(value = "shop", path = "/api/shops")

public interface ShopClient extends BaseClient {


    @ClientMethod(
            httpMethod = "POST",
            path = "/approval",
            grpcMethod = "approveAccount"
    )
    ClientResponse<ShopApproveResponse> approveShopRegistrationRequest(ShopApproveDTO request);


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class ShopApproveDTO {
        private String shopId;
        private String shopName;
        private String shopEmail;
        private String shopAddress;
        private String shopType;
        private String businessType;
        private String shopPhoneNumber;
        private String nationId;
        private boolean validEkyc;
        private boolean validBusinessDoc;
        private String shopAvatarUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class ShopApproveResponse {
        private String shopId;
        private String shopName;
        private boolean isApproved;
        private String messgage;
        private String reason;
        private String rejectedBy;
    }
}
