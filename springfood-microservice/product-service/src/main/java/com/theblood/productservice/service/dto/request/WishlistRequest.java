package com.theblood.productservice.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WishlistRequest {
    @NotBlank(message = "Product ID is required")
    String productId;

    /** Optional — wish 1 variant cụ thể; null = wish bất kỳ variant. */
    String variantId;

    @Size(max = 500, message = "Note too long")
    String note;
}
