package com.theblood.shopservice.repository;


import com.theblood.shopservice.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {

    @Query("select s from Shop s where s.ownerId = :ownerId")
    Optional<Shop> findShopByOwnerId(UUID ownerId);
}
