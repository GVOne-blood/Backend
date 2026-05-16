package com.theblood.shopservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body khi admin ban shop — bắt buộc có lý do để store vào audit log và gửi
 * notification cho shop owner sau này.
 */
@Data
public class AdminBanShopRequest {

    @NotBlank
    @Size(min = 8, max = 1000)
    private String reason;
}
