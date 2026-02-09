package com.theblood.shopservice.repository;

import com.theblood.shopservice.domain.ShopMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ShopMember entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ShopMemberRepository extends JpaRepository<ShopMember, String> {
    boolean existsByIdAndUserIdAndRoleName(String id, String userId, String roleName);

}
