package com.theblood.shopservice.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body khi admin reject đơn đăng ký — bắt buộc có lý do để gửi cho user qua
 * email/notification (FE sau này sẽ wire vào Kafka topic notification).
 */
@Data
public class AdminRejectRegistrationRequest {

    @NotBlank
    @Size(max = 1000)
    private String reason;
}
