package com.theblood.productservice.repository;

import com.theblood.productservice.domain.ProductCategory;
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

    @Query(value = "SELECT p.product_id " +
            "FROM products p " +
            "JOIN product_categories pc1 ON p.product_id = pc1.product_id " +
            "JOIN categories c ON c.category_name = pc1.category_name " +
            "WHERE c.category_name IN :categoryNames " +
            "AND p.product_id != :productId " +
            "GROUP BY p.product_id, p.updated_at " +
            "ORDER BY p.updated_at DESC " +
            "LIMIT 30", nativeQuery = true)
    List<UUID> findRelatedProductIds(UUID productId, List<String> categoryNames);

}
