package com.theblood.productservice.repository;

import com.theblood.productservice.domain.Product;
import com.theblood.productservice.repository.projection.ProductProjection;
import com.theblood.springfood.common.dto.response.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends
        JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> // Specification
{

    @Query("SELECT p FROM Product p ORDER BY p.updatedAt DESC LIMIT 300")
    List<Product> findTop300ByOrderByUpdatedAtDesc();

    void deleteProductById(UUID id);

    // <T> Page<T> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p")
    Page<ProductProjection> findListProduct(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.shopId = :shopId")
    Page<ProductProjection> findProductsByShopId(@Param("shopId") UUID shopId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    Page<ProductProjection> findByIds(List<UUID> ids, Pageable pageable);

    @Query(value = """
            SELECT 
                p.product_id as id,
                p.name as name,
                p.description as description,
                p.price as price,
                p.images as images,
                p.quantity as quantity,
                p.msg as msg,
                p.exp as exp
            FROM products p
            WHERE p.category_id IN (:categoryIds)
              AND p.product_id != :productId
            ORDER BY RANDOM()
            LIMIT :limit
            """,
            nativeQuery = true)
    List<ProductProjection> findRandomRelatedProducts(
            @Param("categoryIds") List<String> categoryIds,
            @Param("productId") UUID productId,
            @Param("limit") int limit
    );


    @Query("SELECT p.sku FROM Product p WHERE p.shopId = :shopId and p.sku = :sku")
    Optional<String> findProductBySku(
            @Param("shopId") UUID shopId,
            @Param("sku") String sku);


    @Query("SELECT p.id, p.name, p.quantity, p.price, p.images FROM Product p WHERE p.shopId = :shopId")
    Optional<Product> findProductByShopId(UUID shopId);


    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<ProductProjection> findProductProjectionById(@Param("productId") UUID productId);

    List<Product> findAllByUpdatedAtGreaterThan(LocalDateTime updatedAtIsGreaterThan);

    @Query("SELECT new com.theblood.springfood.common.dto.response.ProductDetail(p.id, p.name, p.description, p.price, p.images, p.quantity, p.msg, p.exp, p.averageRating,  p.totalFeedbacks) FROM Product p WHERE p.id = :productId ")
    Optional<ProductDetail> findProductDetailById(@Param("productId") UUID productId);

    /**
     * Random products for the "recommended" homepage section.
     * AVAILABLE products only with positive stock.
     *
     * Note: PostgreSQL random() ordering is wrapped in a JPQL function() call so
     * Hibernate doesn't validate it as a known JPQL function. Mapping to the
     * interface projection is the same as findListProduct() so type coercion
     * stays consistent (no native-query Integer/BigInteger surprises).
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.productStatus = com.theblood.productservice.common.enums.ProductStatus.AVAILABLE
              AND p.quantity > 0
            ORDER BY function('random')
            """)
    Page<ProductProjection> findRecommendedProducts(Pageable pageable);

    /**
     * Returns rows of {@code (categoryName, productId)} for every product belonging
     * to a given shop. Used to bucket the shop's products into category groups
     * for the storefront menu.
     *
     * <p>The query intentionally stays at the link table so we don't pay the
     * JSON {@code images} read for the product entity twice — once here for
     * grouping and again when fetching {@link ProductProjection} via
     * {@link #findProductsByShopId}. Frontend joins the two on the FE.</p>
     */
    @Query("""
            SELECT pc.categories.name AS categoryName, pc.product.id AS productId
            FROM ProductCategory pc
            WHERE pc.product.shopId = :shopId
            """)
    List<Object[]> findShopProductCategoryLinks(@Param("shopId") UUID shopId);

    /**
     * Simple keyword search by product name/description (case-insensitive contains).
     * Used by the FE search bar.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<ProductProjection> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
