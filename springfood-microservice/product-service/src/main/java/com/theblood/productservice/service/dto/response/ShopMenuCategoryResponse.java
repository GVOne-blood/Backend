package com.theblood.productservice.service.dto.response;

import com.theblood.springfood.common.dto.response.ProductDetail;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Single category bucket inside a shop's menu, returned by
 * {@code GET /products/by-shop/{shopId}/menu}.
 *
 * <p>The menu groups every product the shop sells under the categories that
 * are linked to those products via {@code product_categories}. Products with
 * no category are placed in a synthetic "OTHER" bucket so the storefront can
 * still render them.</p>
 *
 * <p>Field naming mirrors what the Angular store-detail page already expects:
 * the FE template keys off {@code id} (numeric for scroll anchors), {@code name}
 * (display label) and {@code count} (badge next to the chip).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopMenuCategoryResponse {

    /**
     * Stable numeric id derived from category name hash. Used by the FE to
     * anchor scroll positions ({@code #category-{id}}). Numeric (not the
     * category string) so the existing Angular template doesn't have to be
     * re-typed.
     */
    Integer id;

    /** Display name shown on the category chip and section header. */
    String name;

    /** Slug from the canonical {@code categories} table — null for OTHER. */
    String slug;

    /** Number of products in this category for this shop. */
    Integer count;

    /** Products belonging to this category, already enriched with sale info. */
    List<ProductDetail> products;
}
