package com.theblood.productservice.repository;

import com.theblood.productservice.domain.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, String> {

    boolean existsById(String categoryName);

    Optional<Categories> findById(String categoryName);

    Page<Categories> findAllByName(String name, Pageable pageable);

    /**
     * System-wide categories (no owner). These are visible to every shop and
     * editable only by admins.
     */
    @Query("select c from Categories c where c.shopId is null and c.isActive = true order by c.name")
    List<Categories> findSystemCategories();

    /**
     * Categories owned by a specific shop.
     */
    @Query("select c from Categories c where c.shopId = :shopId and c.isActive = true order by c.name")
    List<Categories> findByShopId(@Param("shopId") UUID shopId);

    /**
     * Everything the supplied shop is allowed to use: system-wide categories
     * plus its own categories.
     */
    @Query("select c from Categories c where c.isActive = true and (c.shopId is null or c.shopId = :shopId) order by c.shopId nulls first, c.name")
    List<Categories> findVisibleForShop(@Param("shopId") UUID shopId);
}
