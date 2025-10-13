package com.theblood.productservice.repository;

import com.theblood.productservice.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.product.id = :productId")
    Long countFeedbacksByProductId(@Param("productId") UUID productId);
}