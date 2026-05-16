package com.theblood.shopservice.repository;

import com.theblood.shopservice.domain.ShopMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the ShopMember entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ShopMemberRepository extends JpaRepository<ShopMember, String> {
    boolean existsByIdAndUserIdAndRoleName(String id, String userId, String roleName);

    @Query("SELECT sm FROM ShopMember sm WHERE sm.shopId = :shopId ORDER BY sm.createdAt DESC")
    Page<ShopMember> findByShopId(@Param("shopId") String shopId, Pageable pageable);

    @Query("SELECT sm FROM ShopMember sm WHERE sm.shopId = :shopId AND sm.shopMemberId = :shopMemberId")
    Optional<ShopMember> findByShopIdAndMemberId(@Param("shopId") String shopId, @Param("shopMemberId") String shopMemberId);
}
