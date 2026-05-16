package com.theblood.productservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.theblood.productservice.domain.Product;
import com.theblood.productservice.service.dto.request.ProductRequest;
import com.theblood.productservice.service.dto.response.ProductImageResponse;
import com.theblood.springfood.common.dto.response.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    Page<ProductDetail> getAllProductDetails(Pageable pageable);

    Page<ProductDetail> getRecommendedProducts(Pageable pageable);

    Page<ProductDetail> searchProductsByKeyword(String keyword, Pageable pageable);

    java.util.List<java.util.Map<String, Object>> listCategoryOptions();

    List<ProductDetail> getAllProductDetails();

    List<ProductDetail> getAllLastUpdatedProducts(LocalDateTime lastModifyAt);

    Page<ProductDetail> getProductsByShopId(UUID shopId, Pageable pageable);

    /**
     * Build the storefront menu for a shop: every product the shop sells,
     * grouped by the categories linked through {@code product_categories}.
     * Products with no category are placed under a synthetic "OTHER" bucket
     * so the FE can still render them in their own section.
     */
    java.util.List<com.theblood.productservice.service.dto.response.ShopMenuCategoryResponse> getShopMenu(UUID shopId);

    Product updateProduct(UUID productId, ProductRequest productRequest);

    boolean isProductExists(UUID productId);

    ProductDetail getProductDetailById(UUID productId);

    Page<ProductDetail> getListProductsRelated(Pageable pageable, UUID productId);

    //List<ProductDetail> getListProductsRelated(UUID productId, int limit);

    Product addProduct(ProductRequest productRequest) throws JsonProcessingException;

    List<ProductDetail> addProductsByExcel(MultipartFile file) throws IOException;

    //    Product addProduct(ProductRequest productRequest);
//
    List<ProductDetail> getListProductsRelated(UUID productId, int limit);

    //    Product addProduct(ProductRequest productRequest);
//
//
    void deleteProduct(UUID productId);

    ProductImageResponse uploadImages(UUID userId, UUID productId, List<MultipartFile> files);

    void deleteProductImage(UUID productImagesId);
    //   Page<ProductDetail> findByPrice(String from, String to, Pageable pageable);

//    Page<ProductDetail> search(Pageable pageable, Map<String, String> params);
}
