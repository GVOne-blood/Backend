package com.theblood.productservice.service.impl;

import com.theblood.productservice.domain.Product;
import com.theblood.productservice.domain.ProductSale;
import com.theblood.productservice.domain.Sale;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.repository.ProductSaleRepository;
import com.theblood.productservice.repository.SaleRepository;
import com.theblood.productservice.service.SaleService;
import com.theblood.productservice.service.dto.request.SaleRequest;
import com.theblood.productservice.service.dto.response.SaleResponse;
import com.theblood.productservice.service.mapper.SaleMapper;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductSaleRepository productSaleRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        Sale sale = saleMapper.toSale(request);
        Sale saved = saleRepository.save(sale);

        if (!CollectionUtils.isEmpty(request.getProductIds())) {
            attachProducts(saved, request.getProductIds());
        }

        log.info("Sale created: id={}, title={}", saved.getId(), saved.getTitle());
        return toResponseWithProducts(saved);
    }

    @Override
    @Transactional
    public SaleResponse updateSale(UUID saleId, SaleRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new InvalidDataException("Sale not found: " + saleId));

        validateDateRange(
                request.getStartDate() != null ? request.getStartDate() : sale.getStartDate(),
                request.getEndDate() != null ? request.getEndDate() : sale.getEndDate()
        );

        saleMapper.updateSaleFromRequest(request, sale);
        Sale saved = saleRepository.save(sale);

        // Nếu request truyền danh sách productIds (kể cả rỗng) thì replace toàn bộ mapping.
        if (request.getProductIds() != null) {
            productSaleRepository.deleteBySaleId(saved.getId());
            productSaleRepository.flush();
            if (!request.getProductIds().isEmpty()) {
                attachProducts(saved, request.getProductIds());
            }
        }

        log.info("Sale updated: id={}", saved.getId());
        return toResponseWithProducts(saved);
    }

    @Override
    public SaleResponse getSaleById(UUID saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new InvalidDataException("Sale not found: " + saleId));
        return toResponseWithProducts(sale);
    }

    @Override
    public Page<SaleResponse> getAllSales(String keyword, Pageable pageable) {
        String normalized = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<Sale> page = normalized != null
                ? saleRepository.searchByTitle(normalized, pageable)
                : saleRepository.findAll(pageable);
        return page.map(this::toResponseWithoutProducts);
    }

    @Override
    public List<SaleResponse> getActiveSales() {
        LocalDateTime now = LocalDateTime.now();
        return saleRepository.findAll().stream()
                .filter(s -> isActive(s, now))
                .map(this::toResponseWithoutProducts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSale(UUID saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new InvalidDataException("Sale not found: " + saleId));
        // ProductSale cascade ALL ở Sale.productSales nên sẽ tự xoá; gọi explicit để chắc chắn.
        productSaleRepository.deleteBySaleId(sale.getId());
        saleRepository.delete(sale);
        log.info("Sale deleted: id={}", saleId);
    }

    @Override
    @Transactional
    public SaleResponse addProductsToSale(UUID saleId, List<UUID> productIds) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new InvalidDataException("Sale not found: " + saleId));

        if (CollectionUtils.isEmpty(productIds)) {
            return toResponseWithProducts(sale);
        }
        attachProducts(sale, productIds);
        return toResponseWithProducts(sale);
    }

    @Override
    @Transactional
    public SaleResponse removeProductsFromSale(UUID saleId, List<UUID> productIds) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new InvalidDataException("Sale not found: " + saleId));
        if (!CollectionUtils.isEmpty(productIds)) {
            productSaleRepository.deleteBySaleIdAndProductIds(sale.getId(), productIds);
        }
        return toResponseWithProducts(sale);
    }

    @Override
    public List<UUID> getProductIdsBySaleId(UUID saleId) {
        if (!saleRepository.existsById(saleId)) {
            throw new InvalidDataException("Sale not found: " + saleId);
        }
        return productSaleRepository.findProductIdsBySaleId(saleId);
    }

    // ----------------------- helpers -----------------------

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new InvalidDataException("End date must be after start date");
        }
    }

    private void attachProducts(Sale sale, List<UUID> productIds) {
        Set<UUID> uniqueIds = new HashSet<>(productIds);
        List<Product> products = productRepository.findAllById(uniqueIds);
        if (products.size() != uniqueIds.size()) {
            Set<UUID> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            uniqueIds.removeAll(foundIds);
            throw new InvalidDataException("Some products were not found: " + uniqueIds);
        }

        // Tránh trùng (idempotent): lọc các bản đã có
        Set<UUID> existing = productSaleRepository.findBySaleId(sale.getId()).stream()
                .map(ps -> ps.getProduct().getId())
                .collect(Collectors.toSet());

        for (Product p : products) {
            if (existing.contains(p.getId())) continue;
            ProductSale ps = new ProductSale();
            ps.setSale(sale);
            ps.setProduct(p);
            productSaleRepository.save(ps);
        }
    }

    private boolean isActive(Sale sale, LocalDateTime now) {
        boolean afterStart = sale.getStartDate() == null || !sale.getStartDate().isAfter(now);
        boolean beforeEnd = sale.getEndDate() == null || !sale.getEndDate().isBefore(now);
        return afterStart && beforeEnd;
    }

    private SaleResponse toResponseWithProducts(Sale sale) {
        SaleResponse response = saleMapper.toSaleResponse(sale);
        response.setActive(isActive(sale, LocalDateTime.now()));
        response.setProductIds(productSaleRepository.findProductIdsBySaleId(sale.getId()));
        return response;
    }

    private SaleResponse toResponseWithoutProducts(Sale sale) {
        SaleResponse response = saleMapper.toSaleResponse(sale);
        response.setActive(isActive(sale, LocalDateTime.now()));
        return response;
    }
}
