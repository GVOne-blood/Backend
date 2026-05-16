package com.theblood.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order line item exposed to FE.
 *
 * <p>Replaces the previous practice of reusing
 * {@link com.theblood.springfood.common.dto.response.ProductDetail} as the
 * order line shape — that DTO's {@code quantity} field tracks <em>stock</em>,
 * not the quantity the customer actually ordered, which made every shop
 * dashboard render misleading numbers.</p>
 *
 * <p>Fields are sourced from {@code order_items} (productId, name, ordered
 * quantity, priceAtBooking) and joined with the product image when available.
 * BigDecimal is serialised as a JSON number, so the FE keeps these as
 * {@code number} on the wire.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemView {

    /** Reference to the product so the FE can deep-link into the catalog. */
    UUID productId;

    /** Snapshot of the product name at the time of booking. */
    String productName;

    /** Number of units the customer ordered for this line. */
    Integer quantity;

    /** Unit price captured at booking time, in VND. */
    BigDecimal priceAtBooking;

    /**
     * First image URL of the product. May be null when the product has no
     * images or has been deleted after the order was placed.
     */
    String image;
}
