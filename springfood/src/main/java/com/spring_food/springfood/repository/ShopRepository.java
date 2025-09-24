package com.spring_food.springfood.repository;

import com.spring_food.springfood.model.Shop;
import com.spring_food.springfood.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, String> {

    Optional<Shop> findShopById(String id);

    @Query("SELECT p.shop.id FROM ShopMember p WHERE p.user = :userId AND p.role.id = 'SHOP_OWNER'")
    String findShopByUserId(String userId);

    @Query("SELECT u FROM User u WHERE u.id = (SELECT m.user.id FROM ShopMember m WHERE m.shop.id = :shopId) ")
    Page<User> getAllStaffByShopId(Pageable pageable, String shopId);
}
