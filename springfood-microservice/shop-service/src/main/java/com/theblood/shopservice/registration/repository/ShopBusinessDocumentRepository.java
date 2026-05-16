package com.theblood.shopservice.registration.repository;

import com.theblood.shopservice.registration.domain.ShopBusinessDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopBusinessDocumentRepository extends JpaRepository<ShopBusinessDocument, UUID> {
    Optional<ShopBusinessDocument> findTopByRequestId(UUID requestId);
}
