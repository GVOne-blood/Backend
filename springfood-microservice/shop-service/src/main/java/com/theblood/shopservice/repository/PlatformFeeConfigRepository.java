package com.theblood.shopservice.repository;

import com.theblood.shopservice.domain.PlatformFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PlatformFeeConfig entity.
 */
@Repository
public interface PlatformFeeConfigRepository extends JpaRepository<PlatformFeeConfig, UUID> {

    /**
     * Find fee config by fee code
     */
    Optional<PlatformFeeConfig> findByFeeCode(String feeCode);

    /**
     * Find active fee config by fee code
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE pfc.feeCode = :feeCode AND pfc.isActive = true " +
           "AND pfc.effectiveFrom <= :now AND (pfc.effectiveTo IS NULL OR pfc.effectiveTo > :now)")
    Optional<PlatformFeeConfig> findActiveFeeConfigByCode(@Param("feeCode") String feeCode, @Param("now") Instant now);

    /**
     * Find all active fee configs
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE pfc.isActive = true " +
           "AND pfc.effectiveFrom <= :now AND (pfc.effectiveTo IS NULL OR pfc.effectiveTo > :now)")
    List<PlatformFeeConfig> findAllActiveConfigs(@Param("now") Instant now);

    /**
     * Find fee configs by apply scope
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE pfc.applyScope = :applyScope AND pfc.isActive = true " +
           "AND pfc.effectiveFrom <= :now AND (pfc.effectiveTo IS NULL OR pfc.effectiveTo > :now)")
    List<PlatformFeeConfig> findActiveConfigsByScope(@Param("applyScope") String applyScope, @Param("now") Instant now);

    /**
     * Find fee configs by fee type
     */
    List<PlatformFeeConfig> findByFeeType(String feeType);

    /**
     * Find fee configs effective within date range
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE " +
           "pfc.effectiveFrom <= :endDate AND (pfc.effectiveTo IS NULL OR pfc.effectiveTo >= :startDate)")
    List<PlatformFeeConfig> findEffectiveInDateRange(@Param("startDate") Instant startDate, 
                                                    @Param("endDate") Instant endDate);

    /**
     * Find expired fee configs
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE pfc.effectiveTo IS NOT NULL AND pfc.effectiveTo < :now")
    List<PlatformFeeConfig> findExpiredConfigs(@Param("now") Instant now);

    /**
     * Find future fee configs
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE pfc.effectiveFrom > :now ORDER BY pfc.effectiveFrom ASC")
    List<PlatformFeeConfig> findFutureConfigs(@Param("now") Instant now);

    /**
     * Find fee configs created by specific admin
     */
    List<PlatformFeeConfig> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find fee configs by name containing (case insensitive)
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE LOWER(pfc.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<PlatformFeeConfig> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Check if fee code exists
     */
    boolean existsByFeeCode(String feeCode);

    /**
     * Count active fee configs
     */
    @Query("SELECT COUNT(pfc) FROM PlatformFeeConfig pfc WHERE pfc.isActive = true")
    long countActiveConfigs();

    /**
     * Count fee configs by apply scope
     */
    @Query("SELECT COUNT(pfc) FROM PlatformFeeConfig pfc WHERE pfc.applyScope = :applyScope AND pfc.isActive = true")
    long countActiveConfigsByScope(@Param("applyScope") String applyScope);

    /**
     * Find latest fee config by creation date
     */
    Optional<PlatformFeeConfig> findTopByOrderByCreatedAtDesc();

    /**
     * Find fee configs by fee type and scope
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE pfc.feeType = :feeType AND pfc.applyScope = :applyScope " +
           "AND pfc.isActive = true AND pfc.effectiveFrom <= :now AND (pfc.effectiveTo IS NULL OR pfc.effectiveTo > :now)")
    List<PlatformFeeConfig> findActiveConfigsByTypeAndScope(@Param("feeType") String feeType, 
                                                           @Param("applyScope") String applyScope, 
                                                           @Param("now") Instant now);

    /**
     * Get fee statistics by type
     */
    @Query("SELECT pfc.feeType, COUNT(pfc) FROM PlatformFeeConfig pfc WHERE pfc.isActive = true GROUP BY pfc.feeType")
    List<Object[]> getFeeStatsByType();

    /**
     * Get fee statistics by scope
     */
    @Query("SELECT pfc.applyScope, COUNT(pfc) FROM PlatformFeeConfig pfc WHERE pfc.isActive = true GROUP BY pfc.applyScope")
    List<Object[]> getFeeStatsByScope();

    /**
     * Find configs that will expire soon
     */
    @Query("SELECT pfc FROM PlatformFeeConfig pfc WHERE pfc.isActive = true AND pfc.effectiveTo IS NOT NULL " +
           "AND pfc.effectiveTo BETWEEN :now AND :expiryThreshold ORDER BY pfc.effectiveTo ASC")
    List<PlatformFeeConfig> findConfigsExpiringSoon(@Param("now") Instant now, @Param("expiryThreshold") Instant expiryThreshold);

    /**
     * Check if there's an active config for the given fee code at specific time
     */
    @Query("SELECT COUNT(pfc) > 0 FROM PlatformFeeConfig pfc WHERE pfc.feeCode = :feeCode AND pfc.isActive = true " +
           "AND pfc.effectiveFrom <= :time AND (pfc.effectiveTo IS NULL OR pfc.effectiveTo > :time)")
    boolean hasActiveConfigAt(@Param("feeCode") String feeCode, @Param("time") Instant time);
}