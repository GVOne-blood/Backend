package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ProductService {

    Page<ProductDetail> getAllProductDetails(Pageable pageable);

    boolean isProductExists(String productId);

    ProductDetail getProductDetailById(String productId);

    Product addProduct(ProductRequest productRequest);

    Product updateProduct(String productId, ProductRequest productRequest);

    void deleteProduct(String productId);

    Page<ProductDetail> findByPrice(String from, String to, Pageable pageable);

    Page<ProductDetail> search(Pageable pageable, Map<String, String> params);
}
