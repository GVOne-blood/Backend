package com.theblood.shopservice.repository;

import com.theblood.shopservice.domain.ShopContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ShopContract entity.
 */
@Repository
public interface ShopContractRepository extends JpaRepository<ShopContract, UUID> {

    /**
     * Find contracts by shop ID ordered by start date descending
     */
    List<ShopContract> findByShopIdOrderByStartDateDesc(UUID shopId);

    /**
     * Find active contract for a shop
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.shopId = :shopId AND sc.status = 'ACTIVE' " +
           "AND sc.startDate <= :today AND (sc.endDate IS NULL OR sc.endDate >= :today)")
    Optional<ShopContract> findActiveContractForShop(@Param("shopId") UUID shopId, @Param("today") LocalDate today);

    /**
     * Find contract by contract code
     */
    Optional<ShopContract> findByContractCode(String contractCode);

    /**
     * Find contracts by status
     */
    List<ShopContract> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find contracts by contract type
     */
    List<ShopContract> findByContractTypeOrderByStartDateDesc(String contractType);

    /**
     * Find contracts expiring within date range
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.endDate BETWEEN :startDate AND :endDate " +
           "AND sc.status = 'ACTIVE' ORDER BY sc.endDate ASC")
    List<ShopContract> findContractsExpiringBetween(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Find expired contracts
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.endDate < :today AND sc.status = 'ACTIVE'")
    List<ShopContract> findExpiredContracts(@Param("today") LocalDate today);

    /**
     * Find contracts by shop and status
     */
    List<ShopContract> findByShopIdAndStatusOrderByStartDateDesc(UUID shopId, String status);

    /**
     * Find contracts signed by specific admin
     */
    List<ShopContract> findBySignedByAdminOrderBySignedAtDesc(String signedByAdmin);

    /**
     * Find contracts created by specific admin
     */
    List<ShopContract> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find contracts with specific commission type
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.commissionType = :commissionType")
    List<ShopContract> findByCommissionType(@Param("commissionType") String commissionType);

    /**
     * Find contracts starting within date range
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sc.startDate ASC")
    List<ShopContract> findContractsStartingBetween(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Find contracts by title containing (case insensitive)
     */
    @Query("SELECT sc FROM ShopContract sc WHERE LOWER(sc.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<ShopContract> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    /**
     * Count contracts by status
     */
    long countByStatus(String status);

    /**
     * Count active contracts for a shop
     */
    @Query("SELECT COUNT(sc) FROM ShopContract sc WHERE sc.shopId = :shopId AND sc.status = 'ACTIVE' " +
           "AND sc.startDate <= :today AND (sc.endDate IS NULL OR sc.endDate >= :today)")
    long countActiveContractsForShop(@Param("shopId") UUID shopId, @Param("today") LocalDate today);

    /**
     * Find latest contract for a shop
     */
    Optional<ShopContract> findTopByShopIdOrderByCreatedAtDesc(UUID shopId);

    /**
     * Find contracts with document ID
     */
    List<ShopContract> findByDocumentIdIsNotNull();

    /**
     * Find unsigned contracts (draft status)
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.status = 'DRAFT' AND sc.signedAt IS NULL " +
           "ORDER BY sc.createdAt ASC")
    List<ShopContract> findUnsignedContracts();

    /**
     * Find contracts signed within date range
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.signedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sc.signedAt DESC")
    List<ShopContract> findContractsSignedBetween(@Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);

    /**
     * Check if shop has any active contract
     */
    @Query("SELECT COUNT(sc) > 0 FROM ShopContract sc WHERE sc.shopId = :shopId AND sc.status = 'ACTIVE' " +
           "AND sc.startDate <= :today AND (sc.endDate IS NULL OR sc.endDate >= :today)")
    boolean hasActiveContract(@Param("shopId") UUID shopId, @Param("today") LocalDate today);

    /**
     * Find contracts needing renewal (expiring soon)
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.status = 'ACTIVE' AND sc.endDate IS NOT NULL " +
           "AND sc.endDate BETWEEN :today AND :renewalDate ORDER BY sc.endDate ASC")
    List<ShopContract> findContractsNeedingRenewal(@Param("today") LocalDate today, 
                                                  @Param("renewalDate") LocalDate renewalDate);

    /**
     * Get contract statistics by type
     */
    @Query("SELECT sc.contractType, COUNT(sc) FROM ShopContract sc WHERE sc.status = 'ACTIVE' " +
           "GROUP BY sc.contractType")
    List<Object[]> getContractStatsByType();

    /**
     * Find contracts by multiple shop IDs
     */
    @Query("SELECT sc FROM ShopContract sc WHERE sc.shopId IN :shopIds ORDER BY sc.shopId, sc.startDate DESC")
    List<ShopContract> findByShopIdIn(@Param("shopIds") List<UUID> shopIds);
}