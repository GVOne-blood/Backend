package com.theblood.productservice.repository;

import com.theblood.productservice.domain.ProductVariants;
import com.theblood.productservice.service.dto.response.VariantsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantsRepository extends JpaRepository<ProductVariants, String> {

    @Query("SELECT new com.theblood.productservice.service.dto.response.VariantsResponse(pv.productId, pv.id, pv.variantName, pv.attributes, pv.price, pv.stock) " +
            "FROM ProductVariants pv WHERE pv.productId = :productId")
    List<VariantsResponse> findAllByProductId(String productId);

    @Query("select pv from ProductVariants pv where pv.productId = :productId and pv.id in :variantsId")
    List<ProductVariants> findAllByProductIdAndVariantsId(String productId, List<String> variantsId);
}
