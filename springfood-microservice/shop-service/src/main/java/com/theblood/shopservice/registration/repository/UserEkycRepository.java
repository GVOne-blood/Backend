package com.theblood.shopservice.registration.repository;

import com.theblood.shopservice.registration.domain.UserEkyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEkycRepository extends JpaRepository<UserEkyc, UUID> {
    Optional<UserEkyc> findTopByRequestId(UUID requestId);
}
