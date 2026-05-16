package com.theblood.shopservice.registration.repository;

import com.theblood.shopservice.registration.domain.ShopRegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRegistrationRequestRepository extends JpaRepository<ShopRegistrationRequest, UUID> {
    Optional<ShopRegistrationRequest> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("""
        select count(req) > 0
        from ShopRegistrationRequest req
        where lower(req.shopName) = lower(:shopName)
          and req.status in :statuses
        """)
    boolean existsByShopNameAndStatusIn(@Param("shopName") String shopName, @Param("statuses") List<String> statuses);
}
