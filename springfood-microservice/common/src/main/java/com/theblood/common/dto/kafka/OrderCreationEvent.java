package com.theblood.common.dto.kafka;

import com.theblood.common.dto.request.ItemRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationEvent {

    UUID orderId;
    UUID userId;
    List<ItemRequest> products;
    BigDecimal totalPrice;

}
