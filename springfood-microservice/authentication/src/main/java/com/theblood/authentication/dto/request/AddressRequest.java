package com.theblood.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Request body cho POST /profile/addr (create) và PUT /profile/addr/{id} (update).
 *
 * Tất cả validation áp dụng cho create. Update có thể partial — controller chỉ
 * apply field nào được gửi (không null/blank).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {

    @Size(max = 100, message = "Label too long")
    String label;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 200)
    String recipientName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    String phoneNumber;

    @NotBlank(message = "Street address is required")
    String streetAddress;

    @NotBlank(message = "Ward is required")
    String ward;

    @NotBlank(message = "District is required")
    @Size(max = 100)
    String district;

    @NotBlank(message = "City is required")
    String city;

    @Size(max = 500)
    String details;

    /**
     * Có set làm default không. Service sẽ tự unset default cũ.
     */
    Boolean isDefault;
}
