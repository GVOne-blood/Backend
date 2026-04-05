package com.theblood.shopservice.repository;

import com.theblood.shopservice.domain.CommissionRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for CommissionRule entity.
 */
@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, UUID> {

    /**
     * Find active rules for a specific shop ordered by priority
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.shopId = :shopId AND cr.isActive = true " +
           "AND cr.effectiveFrom <= :now AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :now) " +
           "ORDER BY cr.priority ASC")
    List<CommissionRule> findActiveRulesForShop(@Param("shopId") UUID shopId, @Param("now") Instant now);

    /**
     * Find active rules for a specific category ordered by priority
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.categoryName = :categoryName AND cr.isActive = true " +
           "AND cr.effectiveFrom <= :now AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :now) " +
           "ORDER BY cr.priority ASC")
    List<CommissionRule> findActiveRulesForCategory(@Param("categoryName") String categoryName, @Param("now") Instant now);

    /**
     * Find active rules for a specific product ordered by priority
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.productId = :productId AND cr.isActive = true " +
           "AND cr.effectiveFrom <= :now AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :now) " +
           "ORDER BY cr.priority ASC")
    List<CommissionRule> findActiveRulesForProduct(@Param("productId") UUID productId, @Param("now") Instant now);

    /**
     * Find applicable rules for shop, category, and product (ordered by priority)
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.isActive = true " +
           "AND cr.effectiveFrom <= :now AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :now) " +
           "AND (cr.shopId = :shopId OR cr.categoryName = :categoryName OR cr.productId = :productId " +
           "OR (cr.shopId IS NULL AND cr.categoryName IS NULL AND cr.productId IS NULL)) " +
           "ORDER BY cr.priority ASC")
    List<CommissionRule> findApplicableRules(@Param("shopId") UUID shopId, 
                                           @Param("categoryName") String categoryName,
                                           @Param("productId") UUID productId, 
                                           @Param("now") Instant now);

    /**
     * Find global rules (no specific scope)
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.shopId IS NULL AND cr.categoryName IS NULL " +
           "AND cr.productId IS NULL AND cr.isActive = true " +
           "AND cr.effectiveFrom <= :now AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :now) " +
           "ORDER BY cr.priority ASC")
    List<CommissionRule> findActiveGlobalRules(@Param("now") Instant now);

    /**
     * Find rules by commission type
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.commissionType = :commissionType AND cr.isActive = true")
    List<CommissionRule> findByCommissionType(@Param("commissionType") String commissionType);

    /**
     * Find rules created by specific admin
     */
    List<CommissionRule> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find rules by priority range
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.priority BETWEEN :minPriority AND :maxPriority " +
           "AND cr.isActive = true ORDER BY cr.priority ASC")
    List<CommissionRule> findByPriorityRange(@Param("minPriority") Integer minPriority, 
                                           @Param("maxPriority") Integer maxPriority);

    /**
     * Find rules effective within date range
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE " +
           "cr.effectiveFrom <= :endDate AND (cr.effectiveTo IS NULL OR cr.effectiveTo >= :startDate)")
    List<CommissionRule> findEffectiveInDateRange(@Param("startDate") Instant startDate, 
                                                @Param("endDate") Instant endDate);

    /**
     * Find expired rules
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.effectiveTo IS NOT NULL AND cr.effectiveTo < :now")
    List<CommissionRule> findExpiredRules(@Param("now") Instant now);

    /**
     * Find rules that will become effective in the future
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.effectiveFrom > :now ORDER BY cr.effectiveFrom ASC")
    List<CommissionRule> findFutureRules(@Param("now") Instant now);

    /**
     * Find rules by name containing (case insensitive)
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE LOWER(cr.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<CommissionRule> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Count active rules
     */
    @Query("SELECT COUNT(cr) FROM CommissionRule cr WHERE cr.isActive = true")
    long countActiveRules();

    /**
     * Count rules for a specific shop
     */
    @Query("SELECT COUNT(cr) FROM CommissionRule cr WHERE cr.shopId = :shopId AND cr.isActive = true")
    long countActiveRulesForShop(@Param("shopId") UUID shopId);

    /**
     * Find highest priority rule for specific context
     */
    @Query("SELECT cr FROM CommissionRule cr WHERE cr.isActive = true " +
           "AND cr.effectiveFrom <= :now AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :now) " +
           "AND (cr.shopId = :shopId OR cr.categoryName = :categoryName OR cr.productId = :productId " +
           "OR (cr.shopId IS NULL AND cr.categoryName IS NULL AND cr.productId IS NULL)) " +
           "ORDER BY cr.priority ASC LIMIT 1")
    CommissionRule findHighestPriorityRule(@Param("shopId") UUID shopId, 
                                         @Param("categoryName") String categoryName,
                                         @Param("productId") UUID productId, 
                                         @Param("now") Instant now);

    /**
     * Check if there are conflicting rules with same priority
     */
    @Query("SELECT COUNT(cr) > 1 FROM CommissionRule cr WHERE cr.priority = :priority AND cr.isActive = true " +
           "AND cr.effectiveFrom <= :now AND (cr.effectiveTo IS NULL OR cr.effectiveTo > :now)")
    boolean hasConflictingRulesWithPriority(@Param("priority") Integer priority, @Param("now") Instant now);
}