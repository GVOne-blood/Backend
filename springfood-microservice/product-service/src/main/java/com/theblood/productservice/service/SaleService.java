package com.theblood.productservice.service;

import com.theblood.productservice.service.dto.request.SaleRequest;
import com.theblood.productservice.service.dto.response.SaleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * CRUD service cho chương trình khuyến mãi (Sale) và mapping product <-> sale.
 */
public interface SaleService {

    SaleResponse createSale(SaleRequest request);

    SaleResponse updateSale(UUID saleId, SaleRequest request);

    SaleResponse getSaleById(UUID saleId);

    Page<SaleResponse> getAllSales(String keyword, Pageable pageable);

    List<SaleResponse> getActiveSales();

    void deleteSale(UUID saleId);

    /**
     * Thêm các product vào 1 sale (idempotent, bỏ qua nếu đã tồn tại).
     */
    SaleResponse addProductsToSale(UUID saleId, List<UUID> productIds);

    /**
     * Bỏ các product khỏi 1 sale.
     */
    SaleResponse removeProductsFromSale(UUID saleId, List<UUID> productIds);

    /**
     * Lấy danh sách productId được áp dụng cho 1 sale.
     */
    List<UUID> getProductIdsBySaleId(UUID saleId);
}
