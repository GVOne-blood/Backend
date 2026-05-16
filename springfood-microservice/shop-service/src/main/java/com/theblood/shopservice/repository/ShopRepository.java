package com.theblood.shopservice.repository;


import com.theblood.shopservice.domain.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {

    @Query("select s from Shop s where s.ownerId = :ownerId")
    Optional<Shop> findShopByOwnerId(String ownerId);

    boolean existsByShopNameIgnoreCase(String shopName);

    /**
     * Lấy danh sách shop nổi bật, sắp xếp theo totalSold giảm dần.
     *
     * <p>Trước đây có 2 điều kiện lọc {@code lastModifiedDate >= :since} và
     * {@code isActive = 1}. Cả hai đều khắt khe với seed data (modifiedDate là
     * thời điểm seed một lần, không có user activity nên hiếm khi rơi vào
     * window 30 ngày). Kết quả là endpoint trả empty page → FE không hiện được
     * featured shops nào.</p>
     *
     * <p>Đã bỏ filter để show toàn bộ shop sort theo totalSold. Trong production
     * có thể restore filter qua flag config hoặc query alternative.</p>
     */
    @Query("""
        select s
        from Shop s
        order by s.totalSold desc
        """)
    Page<Shop> findTop10ShopsByTotalSoldLastMonth(Instant since, Pageable pageable);
}
