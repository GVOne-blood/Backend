package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.model.Product;

import java.util.List;

public interface ProductService {

    List<ProductDetail> getAllProductDetails();

    ProductDetail getProductDetailById(String productId);

    Product addProduct(ProductRequest productRequest);

    Product updateProduct(String productId, ProductRequest productRequest);

    void deleteProduct(String productId);


}
