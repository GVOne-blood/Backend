package com.theblood.productservice.service.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaleResponse {

    UUID id;
    String title;
    String description;
    BigDecimal discountPercentage;
    String conditions;
    LocalDateTime startDate;
    LocalDateTime endDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Trạng thái đang chạy hay không (dựa trên startDate/endDate vs now)
     */
    Boolean active;

    /**
     * Danh sách productId được áp dụng (chỉ trả khi cần)
     */
    List<UUID> productIds;
}
