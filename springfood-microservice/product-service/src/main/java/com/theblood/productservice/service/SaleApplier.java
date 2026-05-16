package com.theblood.productservice.service;

import com.theblood.springfood.common.dto.response.ProductDetail;
import com.theblood.springfood.common.dto.response.ProductDetailWithShop;

import java.util.List;
import java.util.UUID;

/**
 * Áp dụng chương trình khuyến mãi (Sale) đang active lên ProductDetail.
 * Chỉ tính sale có % discount cao nhất tại thời điểm hiện tại.
 *
 * Quy ước:
 * - originalPrice luôn được set bằng giá gốc của Product.
 * - Khi có sale active: price = originalPrice * (1 - discountPercentage/100), làm tròn lên đơn vị đồng (scale=0).
 * - Khi không có sale active: price = originalPrice, discountPercentage = null.
 */
public interface SaleApplier {

    void applyActiveSale(ProductDetail detail);

    void applyActiveSale(ProductDetailWithShop detail);

    void applyActiveSales(List<ProductDetail> details);

    void applyActiveSalesWithShop(List<ProductDetailWithShop> details);

    /**
     * Trả về % discount lớn nhất đang active cho 1 product, hoặc null nếu không có.
     */
    java.math.BigDecimal findActiveDiscount(UUID productId);
}
