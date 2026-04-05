package com.theblood.productservice.repository;

import com.theblood.productservice.domain.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for CategoryGroup entity.
 */
@Repository
public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, UUID> {

    /**
     * Find category group by group code
     */
    Optional<CategoryGroup> findByGroupCode(String groupCode);

    /**
     * Find all active category groups ordered by display order
     */
    @Query("SELECT cg FROM CategoryGroup cg WHERE cg.isActive = true ORDER BY cg.displayOrder ASC, cg.groupName ASC")
    List<CategoryGroup> findAllActiveOrderByDisplayOrder();

    /**
     * Find category groups by name containing (case insensitive)
     */
    @Query("SELECT cg FROM CategoryGroup cg WHERE LOWER(cg.groupName) LIKE LOWER(CONCAT('%', :name, '%')) AND cg.isActive = true")
    List<CategoryGroup> findByGroupNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Check if group code exists
     */
    boolean existsByGroupCode(String groupCode);

    /**
     * Count active category groups
     */
    @Query("SELECT COUNT(cg) FROM CategoryGroup cg WHERE cg.isActive = true")
    long countActive();
}