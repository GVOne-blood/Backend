package com.theblood.cartservice.service.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request body cho endpoint PATCH /cart/items/select.
 * <p>
 * Logic ưu tiên (server side):
 * <ol>
 *   <li>Nếu {@code selectAll != null} → set tất cả items theo {@code selected}.</li>
 *   <li>Else if {@code shopId != null} → set tất cả items thuộc shop đó theo {@code selected}.</li>
 *   <li>Else if {@code items != null && !items.isEmpty()} → set từng item theo sku trong list.</li>
 *   <li>Else → throw InvalidDataException.</li>
 * </ol>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SelectionUpdateRequest {

    /**
     * Danh sách item cần toggle (mode item-level).
     */
    List<SkuSelection> items;

    /**
     * Shop ID cần toggle toàn bộ (mode shop-level).
     */
    String shopId;

    /**
     * Cờ chọn tất cả cart (mode cart-level). Khi true, áp dụng giá trị {@code selected}.
     */
    Boolean selectAll;

    /**
     * Giá trị selected áp dụng cho mode {@code shopId} hoặc {@code selectAll}.
     */
    Boolean selected;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SkuSelection {
        String sku;
        Boolean selected;
    }
}
