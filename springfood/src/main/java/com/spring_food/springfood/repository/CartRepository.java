package com.spring_food.springfood.repository;

import com.spring_food.springfood.dto.response.CartDetailResponse;
import com.spring_food.springfood.model.Cart;
import com.spring_food.springfood.model.CartItem;
import com.spring_food.springfood.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {


    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(String userId);

    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByProductId(String productId);

    @Query("SELECT new com.spring_food.springfood.dto.response.CartDetailResponse(p.id, p.name, ci.quantity, p.price, p.images) " +
            "FROM CartItem ci JOIN Product p " +
            "WHERE ci.cart.id = (SELECT u.cart.id FROM User u WHERE u.id = :userId) " +
            "ORDER BY ci.id ASC")
    Page<CartDetailResponse> findCartItemByUserId(Pageable pageable, String userId);

    @Query("SELECT ci " +
            "FROM CartItem ci " +
            "WHERE ci.cart.id = (SELECT u.cart.id FROM User u WHERE u.id = :userId) " +
            "ORDER BY ci.id ASC")
    List<CartItem> findCartItemByUserId(String userId);

}
