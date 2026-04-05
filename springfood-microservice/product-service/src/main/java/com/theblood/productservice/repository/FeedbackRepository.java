package com.theblood.productservice.repository;

import com.theblood.productservice.domain.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    @Query("select count (f) from Feedback f where f.product.id = :productId")
    Long countAllFeedbackFromProduct(String productId);

    @Query("select count (f) from Feedback f where f.shopId = :shopId")
    Long countAllFeedbackFromShop(String shopId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.product.id = :productId")
    Long countFeedbacksByProductId(@Param("productId") UUID productId);

    @Query("select f from Feedback f where f.shopId = :shopId and f.isActive = true")
    Page<Feedback> findAllByShopId(Pageable pageable, @Param("shopId") UUID shopId);

    @Query("select f from Feedback f where f.product.id = :productId and f.isActive = true")
    Page<Feedback> findAllByProductId(Pageable pageable, @Param("productId") UUID productId);

    @Query("select f from Feedback f where f.id in :ids")
    List<Feedback> findByIds(@Param("ids") List<String> ids);
}