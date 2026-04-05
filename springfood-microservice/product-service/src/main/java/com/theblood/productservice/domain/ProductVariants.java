package com.theblood.productservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariants extends AbstractEntity {

    @Column(name = "product_id", nullable = false)
    private String productId;  // Liên kết đến Product.id

    @Column(name = "sku", unique = true, nullable = false)
    private String sku;  // VD: "LAPTOP-001-RAM16-SSD512"

    @Column(name = "variant_name")
    private String variantName;  // VD: "RAM 16GB - SSD 512GB"

    // JSONB field - flexible attributes
//    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "text[]")
    private String[] attributes;
    // VD: {"ram": "16GB", "ssd": "512GB", "color": "Silver"}

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;  // Override base product price

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "image_url")
    private String imageUrl;  // Variant-specific image (optional)
}
