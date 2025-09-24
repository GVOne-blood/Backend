package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.common.util.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingAddressRequest {

    @NotBlank(message = "recipient name must be not blank")
    String recipientName;

    @PhoneNumber(message = "Phone number must be not blank")
    String phoneNumber;

    @NotBlank(message = "street is require")
    String street;

    @NotBlank(message = "ward is require")
    String ward;

    @NotBlank(message = "district is require")
    String district;

    @NotBlank(message = "city is require")
    String city;


}
