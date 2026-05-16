package com.theblood.productservice.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload cho admin tạo / cập nhật categories.
 *
 * <p>Khác với {@link CategoryRequest} (shop owner chỉ được tạo category cho
 * shop của mình), admin có thể tạo:</p>
 * <ul>
 *   <li><b>System category</b> ({@code shopId} null/blank) — dùng chung mọi shop.</li>
 *   <li><b>Override category</b> của shop bất kỳ — dành cho support case.</li>
 * </ul>
 *
 * <p>{@code shopId} hợp lệ phải là UUID dạng string; nếu để trống thì server
 * coi là system category.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCategoryRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String slug;

    @Size(max = 4000)
    private String description;

    @Size(max = 255)
    private String parentName;

    @Size(max = 255)
    private String categoryGroupCode;

    /**
     * UUID string của shop sở hữu, hoặc null/empty để biến thành system
     * category. Validation ở service.
     */
    private String shopId;
}
