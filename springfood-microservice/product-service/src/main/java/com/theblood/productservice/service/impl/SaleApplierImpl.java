package com.theblood.productservice.service.impl;

import com.theblood.productservice.domain.Sale;
import com.theblood.productservice.repository.SaleRepository;
import com.theblood.productservice.service.SaleApplier;
import com.theblood.springfood.common.dto.response.ProductDetail;
import com.theblood.springfood.common.dto.response.ProductDetailWithShop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleApplierImpl implements SaleApplier {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final SaleRepository saleRepository;

    @Override
    public void applyActiveSale(ProductDetail detail) {
        if (detail == null || detail.getId() == null) return;
        BigDecimal discount = findActiveDiscount(detail.getId());
        applyDiscount(detail, discount);
    }

    @Override
    public void applyActiveSale(ProductDetailWithShop detail) {
        if (detail == null || detail.getId() == null) return;
        BigDecimal discount = findActiveDiscount(detail.getId());
        applyDiscount(detail, discount);
    }

    @Override
    public void applyActiveSales(List<ProductDetail> details) {
        if (details == null || details.isEmpty()) return;
        Map<UUID, BigDecimal> map = bulkFindActiveDiscount(
                details.stream().map(ProductDetail::getId).collect(Collectors.toList())
        );
        for (ProductDetail d : details) {
            applyDiscount(d, map.get(d.getId()));
        }
    }

    @Override
    public void applyActiveSalesWithShop(List<ProductDetailWithShop> details) {
        if (details == null || details.isEmpty()) return;
        Map<UUID, BigDecimal> map = bulkFindActiveDiscount(
                details.stream().map(ProductDetailWithShop::getId).collect(Collectors.toList())
        );
        for (ProductDetailWithShop d : details) {
            applyDiscount(d, map.get(d.getId()));
        }
    }

    @Override
    public BigDecimal findActiveDiscount(UUID productId) {
        try {
            List<Sale> sales = saleRepository.findActiveSalesForProduct(productId, LocalDateTime.now());
            if (sales == null || sales.isEmpty()) return null;
            // Repository đã ORDER BY discount_percentage DESC -> phần tử đầu là cao nhất.
            return sales.get(0).getDiscountPercentage();
        } catch (Exception ex) {
            log.warn("Không thể load active sale cho product {}: {}", productId, ex.getMessage());
            return null;
        }
    }

    /**
     * Bulk query active sales cho danh sách productIds. Trả về map productId -> max discount %.
     */
    private Map<UUID, BigDecimal> bulkFindActiveDiscount(List<UUID> productIds) {
        Map<UUID, BigDecimal> result = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return result;
        try {
            List<Object[]> rows = saleRepository.findActiveSalesForProducts(productIds, LocalDateTime.now());
            for (Object[] row : rows) {
                UUID pid = (UUID) row[0];
                Sale sale = (Sale) row[1];
                BigDecimal pct = sale.getDiscountPercentage();
                if (pct == null) continue;
                BigDecimal current = result.get(pid);
                if (current == null || pct.compareTo(current) > 0) {
                    result.put(pid, pct);
                }
            }
        } catch (Exception ex) {
            log.warn("Bulk active-sale lookup lỗi: {}", ex.getMessage());
        }
        return result;
    }

    // -------- helpers --------

    private void applyDiscount(ProductDetail d, BigDecimal discountPct) {
        if (d == null || d.getPrice() == null) return;
        BigDecimal original = d.getOriginalPrice() != null ? d.getOriginalPrice() : d.getPrice();
        d.setOriginalPrice(original);
        if (discountPct == null || discountPct.signum() <= 0) {
            d.setPrice(original);
            d.setDiscountPercentage(null);
            return;
        }
        d.setDiscountPercentage(discountPct);
        d.setPrice(calcSalePrice(original, discountPct));
    }

    private void applyDiscount(ProductDetailWithShop d, BigDecimal discountPct) {
        if (d == null || d.getPrice() == null) return;
        BigDecimal original = d.getOriginalPrice() != null ? d.getOriginalPrice() : d.getPrice();
        d.setOriginalPrice(original);
        if (discountPct == null || discountPct.signum() <= 0) {
            d.setPrice(original);
            d.setDiscountPercentage(null);
            return;
        }
        d.setDiscountPercentage(discountPct);
        d.setPrice(calcSalePrice(original, discountPct));
    }

    private BigDecimal calcSalePrice(BigDecimal original, BigDecimal discountPct) {
        BigDecimal factor = BigDecimal.ONE.subtract(discountPct.divide(HUNDRED, 6, RoundingMode.HALF_UP));
        // Giá tiền VND -> scale = 0
        return original.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }
}
