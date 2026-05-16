package com.theblood.productservice.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating or updating a category. Mirrors the writable fields of
 * {@link com.theblood.productservice.domain.Categories} that a shop owner is
 * allowed to set. The owning shop is resolved server-side from the
 * authenticated principal so callers cannot forge ownership.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String slug;

    @Size(max = 4000)
    private String description;

    /** Optional parent category name to build a hierarchy. */
    @Size(max = 255)
    private String parentName;

    /** Free-form group code, mirrors {@code categories.category_group_code}. */
    @Size(max = 255)
    private String categoryGroupCode;
}
