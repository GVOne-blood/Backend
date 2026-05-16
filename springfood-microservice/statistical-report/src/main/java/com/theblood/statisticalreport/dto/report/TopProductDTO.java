package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Top selling product entry for the shop dashboard.
 *
 * @param productId      Product UUID
 * @param productName    Display name
 * @param sku            Product SKU
 * @param imageUrl       First image URL (extracted from {@code products.images} JSONB)
 *                       or {@code null} when no image is configured
 * @param stockQuantity  Current stock level (inventory)
 * @param quantitySold   Units sold in the queried period
 * @param revenue        Revenue generated in the queried period
 */
public record TopProductDTO(
    UUID productId,
    String productName,
    String sku,
    String imageUrl,
    Long stockQuantity,
    Long quantitySold,
    BigDecimal revenue
) {}
