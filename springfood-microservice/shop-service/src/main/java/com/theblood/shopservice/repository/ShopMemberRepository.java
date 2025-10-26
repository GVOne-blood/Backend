package com.theblood.shopservice.repository;

import com.theblood.shopservice.model.ShopMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShopMemberRepository extends JpaRepository<ShopMember, UUID> {


    boolean existsByIdAndUserIdAndRoleName(UUID id, UUID userId, String roleName);

}
