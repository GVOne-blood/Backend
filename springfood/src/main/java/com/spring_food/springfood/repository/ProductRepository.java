package com.spring_food.springfood.repository;

import com.spring_food.springfood.dto.response.ProductDetail;
import com.spring_food.springfood.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    void deleteProductById(String id);

    @Query("SELECT new com.spring_food.springfood.dto.response.ProductDetail(p.id, p.name, p.description, p.price, p.images, p.quantity,  p.msg, p.exp) FROM Product p")
    List<ProductDetail> findListProduct();

    @Query("SELECT new com.spring_food.springfood.dto.response.ProductDetail(p.id, p.name, p.description, p.price, p.images, p.quantity,  p.msg, p.exp) FROM Product p WHERE p.shop.id = :shopId")
    List<Product> findProductsByShopId(@Param("shopId") String shopId);

    @Query("SELECT p.name, p.price, p.wholesalePrice,p.msg, p.exp FROM Product p WHERE p.shop.id = :shopId and p.sku = :sku")
    Optional<Product> findProductBySku(
            @Param("shopId") String shopId,
            @Param("sku") String sku);


    @Query("SELECT new com.spring_food.springfood.dto.response.ProductDetail(p.id, p.name, p.description, p.price, p.images, p.quantity, p.msg, p.exp) FROM Product p WHERE p.id = :productId ")
    Optional<ProductDetail> findProductDetailById(@Param("productId") String productId);

}
