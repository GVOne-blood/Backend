package com.theblood.shopservice.repository;

import com.theblood.shopservice.domain.PlatformCommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PlatformCommissionConfig entity.
 */
@Repository
public interface PlatformCommissionConfigRepository extends JpaRepository<PlatformCommissionConfig, UUID> {

    /**
     * Find active commission config
     */
    @Query("SELECT pcc FROM PlatformCommissionConfig pcc WHERE pcc.isActive = true")
    Optional<PlatformCommissionConfig> findActiveConfig();

    /**
     * Find currently effective commission config
     */
    @Query("SELECT pcc FROM PlatformCommissionConfig pcc WHERE pcc.isActive = true " +
           "AND pcc.effectiveFrom <= :now AND (pcc.effectiveTo IS NULL OR pcc.effectiveTo > :now)")
    Optional<PlatformCommissionConfig> findCurrentlyEffectiveConfig(@Param("now") Instant now);

    /**
     * Find configs by commission type
     */
    List<PlatformCommissionConfig> findByCommissionType(String commissionType);

    /**
     * Find configs effective within date range
     */
    @Query("SELECT pcc FROM PlatformCommissionConfig pcc WHERE " +
           "pcc.effectiveFrom <= :endDate AND (pcc.effectiveTo IS NULL OR pcc.effectiveTo >= :startDate)")
    List<PlatformCommissionConfig> findEffectiveInDateRange(@Param("startDate") Instant startDate, 
                                                           @Param("endDate") Instant endDate);

    /**
     * Find configs created by specific admin
     */
    List<PlatformCommissionConfig> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find configs that will become effective in the future
     */
    @Query("SELECT pcc FROM PlatformCommissionConfig pcc WHERE pcc.effectiveFrom > :now ORDER BY pcc.effectiveFrom ASC")
    List<PlatformCommissionConfig> findFutureConfigs(@Param("now") Instant now);

    /**
     * Find expired configs
     */
    @Query("SELECT pcc FROM PlatformCommissionConfig pcc WHERE pcc.effectiveTo IS NOT NULL AND pcc.effectiveTo < :now")
    List<PlatformCommissionConfig> findExpiredConfigs(@Param("now") Instant now);

    /**
     * Find configs by name containing (case insensitive)
     */
    @Query("SELECT pcc FROM PlatformCommissionConfig pcc WHERE LOWER(pcc.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<PlatformCommissionConfig> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Count active configs
     */
    @Query("SELECT COUNT(pcc) FROM PlatformCommissionConfig pcc WHERE pcc.isActive = true")
    long countActiveConfigs();

    /**
     * Find latest config by creation date
     */
    Optional<PlatformCommissionConfig> findTopByOrderByCreatedAtDesc();

    /**
     * Check if there's an active config for the given time
     */
    @Query("SELECT COUNT(pcc) > 0 FROM PlatformCommissionConfig pcc WHERE pcc.isActive = true " +
           "AND pcc.effectiveFrom <= :time AND (pcc.effectiveTo IS NULL OR pcc.effectiveTo > :time)")
    boolean hasActiveConfigAt(@Param("time") Instant time);
}