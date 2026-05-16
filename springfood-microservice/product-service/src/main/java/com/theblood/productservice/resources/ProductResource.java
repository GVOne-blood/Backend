package com.theblood.productservice.resources;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.theblood.productservice.domain.Product;
import com.theblood.productservice.service.ProductService;
import com.theblood.productservice.service.dto.request.ProductRequest;
import com.theblood.productservice.service.dto.response.ProductImageResponse;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ProductDetail;
import com.theblood.springfood.common.dto.response.ResponseData;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/products")
@Validated
public class ProductResource {

    ProductService productService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> getAllProducts(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get all products successfully", productService.getAllProductDetails(pageable)), HttpStatus.OK);
    }

    /**
     * Recommended products for the homepage "featured products" section.
     * Returns AVAILABLE products in random order so each visit feels fresh.
     */
    @GetMapping("/recommended")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> getRecommendedProducts(
            @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get recommended products successfully",
                        productService.getRecommendedProducts(pageable)),
                HttpStatus.OK);
    }

    /**
     * Public keyword search by product name/description (case-insensitive contains).
     * Falls back to listing all products when keyword is empty.
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> searchProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Search products successfully",
                        productService.searchProductsByKeyword(keyword, pageable)),
                HttpStatus.OK);
    }

    /**
     * Public list of all categories — used by the shop owner UI when picking
     * categories for a product. Returns only id+name+slug to keep payload small.
     */
    @GetMapping("/categories")
    public ResponseEntity<ResponseData<List<java.util.Map<String, Object>>>> listCategories() {
        List<java.util.Map<String, Object>> rows = productService.listCategoryOptions();
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get categories successfully", rows),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<ProductDetail>> getProductById(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get product by id successfully", productService.getProductDetailById(id)), HttpStatus.OK);
    }

    @GetMapping("/related/{id}")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> getRelatedProducts(@PathVariable("id") UUID id, @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return new ResponseEntity<>(new ResponseData<>(200, "Get related products successfully", productService.getListProductsRelated(pageable, id)), HttpStatus.OK);
    }

    @GetMapping("/randomRelated/{id}")
    public ResponseEntity<ResponseData<List<ProductDetail>>> getRelatedProducts(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(new ResponseData<>(200, "Get related products successfully", productService.getListProductsRelated(id, 20)), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping("/")
    public ResponseEntity<ResponseData<Product>> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        try {
            Product newProduct = productService.addProduct(productRequest);
            return new ResponseEntity<>(
                    new ResponseData<>(201, "Create product successfully", newProduct), HttpStatus.CREATED);
        } catch (InvalidDataException | JsonProcessingException e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Create product failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PostMapping("/batch")
    public ResponseEntity<ResponseData<List<ProductDetail>>> createProducts(
            @RequestBody MultipartFile file
    ) {
        try {
            List<ProductDetail> newProducts = productService.addProductsByExcel(file);
            return new ResponseEntity<>(new ResponseData<>(201, "Import products successfully", newProducts), HttpStatus.CREATED);
        } catch (RuntimeException | IOException e) {
            return new ResponseEntity<>(new ResponseData<>(400, "Import products failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'SHOP_OWNER')")
    @PostMapping("/img")
    public ResponseData<ProductImageResponse> uploadProductImages(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @RequestParam(value = "productId", required = false) UUID productId,
            @RequestBody List<MultipartFile> files
    ) {

        ProductImageResponse res = productService.uploadImages(user.getUserId(), productId, files);
        try {
            return new ResponseData<>(200, "Upload image successfully", res);
        } catch (Exception e) {
            return new ResponseData<>(400, "Upload image failed: " + e.getMessage(), res);
        }
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Product>> updateProduct(@PathVariable("id") UUID id, @RequestBody @Valid ProductRequest productRequest) {
        try {
            Product updatedProduct = productService.updateProduct(id, productRequest);
            return new ResponseEntity<>(
                    new ResponseData<>(200, "Update product successfully", updatedProduct), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Update product failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteProduct(@PathVariable("id") UUID id) {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(
                    new ResponseData<>(204, "Delete product successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Delete product failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('SHOP_OWNER')")
    @GetMapping("/shop")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> getShopProducts(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get shop products successfully",
                        productService.getProductsByShopId(UUID.fromString(user.getShopId()), pageable)), HttpStatus.OK);
    }

    /**
     * Public endpoint — returns products belonging to a specific shop.
     *
     * <p>Used by the storefront's shop detail page to render the menu when a
     * customer taps a shop card on the homepage. Anonymous users can browse
     * the menu without logging in, mirroring the shop detail endpoint at
     * {@code GET /shop/{shopId}}.</p>
     */
    @GetMapping("/by-shop/{shopId}")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> getProductsByShopPublic(
            @PathVariable("shopId") UUID shopId,
            @PageableDefault(size = 100, page = 0) Pageable pageable) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get shop products successfully",
                        productService.getProductsByShopId(shopId, pageable)), HttpStatus.OK);
    }

    /**
     * Public endpoint — returns the full storefront menu for a shop, grouped
     * by category. Each bucket carries an id (stable scroll anchor for the
     * FE), display name, slug, count, and the matching products.
     *
     * <p>Single round-trip alternative to combining {@code /by-shop} with the
     * categories endpoint. Used by the shop detail page so the chip row
     * always shows the real category names + counts.</p>
     */
    @GetMapping("/by-shop/{shopId}/menu")
    public ResponseEntity<ResponseData<List<com.theblood.productservice.service.dto.response.ShopMenuCategoryResponse>>>
    getShopMenu(@PathVariable("shopId") UUID shopId) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get shop menu successfully",
                        productService.getShopMenu(shopId)), HttpStatus.OK);
    }
//
//    @GetMapping("/search/price")
//    public ResponseEntity<ResponseData<Page<ProductDetail>>> searchByPrice(@RequestParam String from,
//                                                                           @RequestParam String to,
//                                                                           @PageableDefault(page = 0, size = 5, sort = "product_id", direction = Sort.Direction.ASC) Pageable pageable) {
//
//        return ResponseEntity.ok(new ResponseData<>(200, "Search successfully", productService.findByPrice(from, to, pageable)));
//
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<ResponseData<Page<ProductDetail>>> searchProducts(
//            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
//            @RequestParam Map<String, String> criteria) {
//
//        try {
//            Page<ProductDetail> results;
//            results = productService.search(pageable, criteria);
//            return ResponseEntity.ok(
//                    new ResponseData<>(200, "Search products successfully", results)
//            );
//        } catch (Exception e) {
//            return new ResponseEntity<>(
//                    new ResponseData<>(400, "Search failed: " + e.getMessage(), null),
//                    HttpStatus.BAD_REQUEST
//            );
//        }
//    }


}
