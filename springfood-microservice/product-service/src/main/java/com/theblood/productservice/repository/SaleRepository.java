package com.theblood.productservice.repository;

import com.theblood.productservice.domain.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    @Query("SELECT s FROM Sale s WHERE " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.startDate DESC")
    Page<Sale> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm tất cả Sale đang active (start_date <= now <= end_date) cho một danh sách product.
     * Mỗi product có thể có nhiều sale, query này trả về tất cả các bản ghi tương ứng.
     */
    @Query("SELECT ps.product.id AS productId, s " +
            "FROM ProductSale ps JOIN ps.sale s " +
            "WHERE ps.product.id IN :productIds " +
            "AND (s.startDate IS NULL OR s.startDate <= :now) " +
            "AND (s.endDate IS NULL OR s.endDate >= :now)")
    List<Object[]> findActiveSalesForProducts(@Param("productIds") List<UUID> productIds,
                                              @Param("now") LocalDateTime now);

    /**
     * Tìm Sale đang active có discount cao nhất cho 1 product.
     */
    @Query("SELECT s FROM Sale s JOIN s.productSales ps " +
            "WHERE ps.product.id = :productId " +
            "AND (s.startDate IS NULL OR s.startDate <= :now) " +
            "AND (s.endDate IS NULL OR s.endDate >= :now) " +
            "ORDER BY s.discountPercentage DESC")
    List<Sale> findActiveSalesForProduct(@Param("productId") UUID productId,
                                         @Param("now") LocalDateTime now);

    Optional<Sale> findByTitle(String title);
}
