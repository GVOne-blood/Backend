package com.theblood.productservice.repository;

import com.theblood.productservice.domain.ProductSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductSaleRepository extends JpaRepository<ProductSale, ProductSale.ProductSaleId> {

    @Query("SELECT ps FROM ProductSale ps WHERE ps.sale.id = :saleId")
    List<ProductSale> findBySaleId(@Param("saleId") UUID saleId);

    @Query("SELECT ps FROM ProductSale ps WHERE ps.product.id = :productId")
    List<ProductSale> findByProductId(@Param("productId") UUID productId);

    @Modifying
    @Query("DELETE FROM ProductSale ps WHERE ps.sale.id = :saleId")
    void deleteBySaleId(@Param("saleId") UUID saleId);

    @Modifying
    @Query("DELETE FROM ProductSale ps WHERE ps.sale.id = :saleId AND ps.product.id IN :productIds")
    void deleteBySaleIdAndProductIds(@Param("saleId") UUID saleId,
                                     @Param("productIds") List<UUID> productIds);

    @Query("SELECT ps.product.id FROM ProductSale ps WHERE ps.sale.id = :saleId")
    List<UUID> findProductIdsBySaleId(@Param("saleId") UUID saleId);
}
