package com.theblood.springfood.client.api;

import com.viettel.client.authentication.AccountApprovalDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

@BaseClient.ServiceClient(value = "authentication", path = "/api/user")
public interface AuthenticationClient extends BaseClient {

    @ClientMethod(
            httpMethod = "POST",
            path = "/approval",
            grpcMethod = "approveAccount"
    )
    ClientResponse<AccountApprovalDTO> approveAccount(AccountApprovalDTO request);

    @ClientMethod(
            httpMethod = "GET",
            path = "/notification/devices",
            grpcMethod = "getUserDevices",
            idempotent = true
    )
    ClientResponse<NotificationUserDevicesDTO> getNotificationUserDevices(@RequestBody String receiverIds);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class NotificationUserDevicesDTO {
        String accountId;
        String deviceId;
        String pushToken;
    }
}
