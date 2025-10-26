package com.theblood.productservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.theblood.productservice.dto.request.ProductRequest;
import com.theblood.productservice.dto.response.ProductDetail;
import com.theblood.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    Page<ProductDetail> getAllProductDetails(Pageable pageable);

    List<ProductDetail> getAllProductDetails();

    List<ProductDetail> getAllLastUpdatedProducts(LocalDateTime lastModifyAt);

    Product updateProduct(UUID productId, ProductRequest productRequest);

    boolean isProductExists(UUID productId);

    ProductDetail getProductDetailById(UUID productId);

    Page<ProductDetail> getListProductsRelated(Pageable pageable, UUID productId);

    //List<ProductDetail> getListProductsRelated(UUID productId, int limit);

    Product addProduct(ProductRequest productRequest) throws JsonProcessingException;

    //    Product addProduct(ProductRequest productRequest);
//
    List<ProductDetail> getListProductsRelated(UUID productId, int limit);
//    Product addProduct(ProductRequest productRequest);
//
//
//    void deleteProduct(UUID productId);
//
//    Page<ProductDetail> findByPrice(String from, String to, Pageable pageable);
//
//    Page<ProductDetail> search(Pageable pageable, Map<String, String> params);
}
