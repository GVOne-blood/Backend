package com.spring_food.springfood.repository;

import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends
        JpaRepository<Product, String>,
        CustomProductRepository, //criteria
        JpaSpecificationExecutor<Product> // Specification
{

    void deleteProductById(String id);

    // <T> Page<T> findAll(Pageable pageable);

    @Query("SELECT new com.spring_food.springfood.dto.response.ProductDetail(p.id, p.name, p.description, p.price, p.images, p.quantity,  p.msg, p.exp) FROM Product p")
    Page<ProductDetail> findListProduct(Pageable pageable);

    @Query("SELECT new com.spring_food.springfood.dto.response.ProductDetail(p.id, p.name, p.description, p.price, p.images, p.quantity,  p.msg, p.exp) FROM Product p WHERE p.shop.id = :shopId")
    List<ProductDetail> findProductsByShopId(@Param("shopId") String shopId);


    @Query("SELECT p.sku FROM Product p WHERE p.shop.id = :shopId and p.sku = :sku")
    String findProductBySku(
            @Param("shopId") String shopId,
            @Param("sku") String sku);


    @Query("SELECT p.id, p.name, p.quantity, p.price, p.images FROM Product p WHERE p.shop.id = :shopId")
    Optional<Product> findProductByShopId(String shopId);


    @Query("SELECT new com.spring_food.springfood.dto.response.ProductDetail(p.id, p.name, p.description, p.price, p.images, p.quantity, p.msg, p.exp) FROM Product p WHERE p.id = :productId ")
    Optional<ProductDetail> findProductDetailById(@Param("productId") String productId);

}
