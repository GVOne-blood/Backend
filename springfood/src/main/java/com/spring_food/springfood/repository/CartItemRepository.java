package com.spring_food.springfood.repository;

import com.spring_food.springfood.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id IN :productIds")
    void deleteAllByCartIdAndProductIds(String cartId, List<String> productIds);

    // Tìm tất cả cart items của một user
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId")
    List<CartItem> findAllByUserId(String userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id IN (SELECT c.id FROM Cart c WHERE c.user.id = :userId)")
    void deleteAllItemsByUserId(String userId);

}