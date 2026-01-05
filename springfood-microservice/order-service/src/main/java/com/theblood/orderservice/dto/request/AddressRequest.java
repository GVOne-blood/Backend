package com.theblood.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {

    String street;
    String ward;
    String city;
    @NotBlank(message = "Address details must be not blank")
    String details;
}
