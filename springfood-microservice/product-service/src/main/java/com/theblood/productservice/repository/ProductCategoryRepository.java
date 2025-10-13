package com.theblood.productservice.repository;

import com.theblood.productservice.model.ProductCategory;
import com.theblood.productservice.repository.projection.ProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "JOIN p.productCategories pc1 " +
            "JOIN pc1.categories c " +
            "WHERE c.name IN (SELECT pc2.categories.name FROM ProductCategory pc2 WHERE pc2.product.id = :productId) " +
            "AND p.id != :productId")
    Page<ProductProjection> findAllProductsByCategoryName(UUID productId, Pageable pageable);

    @Query("SELECT DISTINCT p.id " +
            "FROM Product p " +
            "JOIN p.productCategories pc1 " +
            "JOIN pc1.categories c " +
            "WHERE c.name IN :categoryNames " +
            "AND p.id != :productId ORDER BY p.updatedAt LIMIT 30")
    List<UUID> findRelatedProductIds(UUID productId, List<String> categoryNames);
}
