package com.theblood.productservice.resources;

import com.theblood.productservice.service.SaleService;
import com.theblood.productservice.service.dto.request.SaleRequest;
import com.theblood.productservice.service.dto.response.SaleResponse;
import com.theblood.springfood.common.dto.response.ResponseData;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API CRUD cho Sale (chương trình khuyến mãi) và mapping product-sale.
 * Base path: /sales (qua API Gateway: /api/v1/products/sales/**).
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/sales")
@Validated
public class SaleResource {

    SaleService saleService;

    @GetMapping
    public ResponseEntity<ResponseData<Page<SaleResponse>>> getAllSales(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {
        Page<SaleResponse> data = saleService.getAllSales(keyword, pageable);
        return ResponseEntity.ok(new ResponseData<>(200, "Get all sales successfully", data));
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseData<List<SaleResponse>>> getActiveSales() {
        List<SaleResponse> data = saleService.getActiveSales();
        return ResponseEntity.ok(new ResponseData<>(200, "Get active sales successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<SaleResponse>> getSaleById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(
                new ResponseData<>(200, "Get sale successfully", saleService.getSaleById(id)));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<ResponseData<List<UUID>>> getProductsOfSale(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(
                new ResponseData<>(200, "Get products of sale successfully",
                        saleService.getProductIdsBySaleId(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_OWNER')")
    public ResponseEntity<ResponseData<SaleResponse>> createSale(
            @RequestBody @Valid SaleRequest request) {
        SaleResponse created = saleService.createSale(request);
        return new ResponseEntity<>(
                new ResponseData<>(201, "Create sale successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_OWNER')")
    public ResponseEntity<ResponseData<SaleResponse>> updateSale(
            @PathVariable("id") UUID id,
            @RequestBody @Valid SaleRequest request) {
        return ResponseEntity.ok(
                new ResponseData<>(200, "Update sale successfully",
                        saleService.updateSale(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_OWNER')")
    public ResponseEntity<ResponseData<Void>> deleteSale(@PathVariable("id") UUID id) {
        saleService.deleteSale(id);
        return ResponseEntity.ok(new ResponseData<>(200, "Delete sale successfully", null));
    }

    @PostMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_OWNER')")
    public ResponseEntity<ResponseData<SaleResponse>> addProductsToSale(
            @PathVariable("id") UUID id,
            @RequestBody List<UUID> productIds) {
        return ResponseEntity.ok(
                new ResponseData<>(200, "Add products to sale successfully",
                        saleService.addProductsToSale(id, productIds)));
    }

    @DeleteMapping("/{id}/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_OWNER')")
    public ResponseEntity<ResponseData<SaleResponse>> removeProductsFromSale(
            @PathVariable("id") UUID id,
            @RequestBody List<UUID> productIds) {
        return ResponseEntity.ok(
                new ResponseData<>(200, "Remove products from sale successfully",
                        saleService.removeProductsFromSale(id, productIds)));
    }
}
