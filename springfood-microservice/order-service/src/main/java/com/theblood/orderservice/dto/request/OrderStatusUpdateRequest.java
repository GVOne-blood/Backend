package com.theblood.orderservice.dto.request;

import com.theblood.springfood.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the shop-side order status transition endpoint.
 *
 * @see com.theblood.orderservice.controller.OrderController#updateShopOrderStatus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull
    private OrderStatus targetStatus;

    /** Optional reason / note shown to the buyer (e.g. cancel reason). */
    @Size(max = 500)
    private String reason;
}
