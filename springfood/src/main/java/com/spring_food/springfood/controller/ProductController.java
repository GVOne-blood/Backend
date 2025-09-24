package com.spring_food.springfood.controller;

import com.spring_food.springfood.dto.request.ProductRequest;
import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.model.Product;
import com.spring_food.springfood.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/products")
@Validated
public class ProductController {

    ProductService productService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> getAllProducts(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get all products successfully", productService.getAllProductDetails(pageable)), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<ProductDetail>> getProductById(@PathVariable("id") String id) {
        return new ResponseEntity<>(
                new ResponseData<>(200, "Get product by id successfully", productService.getProductDetailById(id)), HttpStatus.OK);
    }

    @PostAuthorize(value = "ADMIN")
    @PostMapping("/")
    public ResponseEntity<ResponseData<Product>> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        try {
            Product newProduct = productService.addProduct(productRequest);
            return new ResponseEntity<>(
                    new ResponseData<>(201, "Create product successfully", newProduct), HttpStatus.CREATED);
        } catch (InvalidDataException e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Create product failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Product>> updateProduct(@PathVariable("id") String id, @RequestBody @Valid ProductRequest productRequest) {
        try {
            Product updatedProduct = productService.updateProduct(id, productRequest);
            return new ResponseEntity<>(
                    new ResponseData<>(200, "Update product successfully", updatedProduct), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Update product failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteProduct(@PathVariable("id") String id) {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(
                    new ResponseData<>(200, "Delete product successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Delete product failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search/price")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> searchByPrice(@RequestParam String from,
                                                                           @RequestParam String to,
                                                                           @PageableDefault(page = 0, size = 5, sort = "product_id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(new ResponseData<>(200, "Search successfully", productService.findByPrice(from, to, pageable)));

    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<ProductDetail>>> searchProducts(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Map<String, String> criteria) {

        try {
            Page<ProductDetail> results;
            results = productService.search(pageable, criteria);
            return ResponseEntity.ok(
                    new ResponseData<>(200, "Search products successfully", results)
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Search failed: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
