package com.theblood.productservice.repository;

import com.theblood.productservice.domain.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductImagesRepository extends JpaRepository<ProductImages, UUID> {
    @Query("SELECT COUNT(pi.id) FROM ProductImages pi WHERE pi.product_id = ?1 AND pi.status = com.theblood.springfood.common.enums.FileStatus.ACTIVE")
    int totalFileCurrentStore(UUID productId);
}
