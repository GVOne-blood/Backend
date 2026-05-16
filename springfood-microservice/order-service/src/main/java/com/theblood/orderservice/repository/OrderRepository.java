package com.theblood.orderservice.repository;

import com.theblood.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE o.referenceId = :referenceId")
    List<Order> findByReferenceId(@Param("referenceId") UUID referenceId);

    @Query("SELECT o FROM Order o WHERE o.shopId = :shopId ORDER BY o.createdAt DESC")
    Page<Order> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.shopId = :shopId")
    Optional<Order> findByShopIdAndOrderId(@Param("shopId") UUID shopId, @Param("orderId") UUID orderId);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.userId = :userId")
    Optional<Order> findByUserIdAndOrderId(@Param("userId") UUID userId, @Param("orderId") UUID orderId);

}
