package com.theblood.productservice.service.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

/**
 * Request payload để tạo / cập nhật Sale (chương trình khuyến mãi).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaleRequest {

    @NotBlank(message = "Sale title is required")
    @Size(max = 255, message = "Sale title must be at most 255 characters")
    String title;

    @Size(max = 5000, message = "Description is too long")
    String description;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Discount percentage must be >= 0")
    @DecimalMax(value = "100.00", inclusive = true, message = "Discount percentage must be <= 100")
    BigDecimal discountPercentage;

    @Size(max = 255, message = "Conditions text is too long")
    String conditions;

    LocalDateTime startDate;
    LocalDateTime endDate;

    /**
     * (Tùy chọn) Danh sách product được áp dụng sale này.
     * Khi tạo mới: nếu có sẽ tạo các bản ghi product_sales tương ứng.
     * Khi update: nếu null thì giữ nguyên, nếu rỗng/list mới sẽ thay thế toàn bộ mapping.
     */
    List<UUID> productIds;
}
