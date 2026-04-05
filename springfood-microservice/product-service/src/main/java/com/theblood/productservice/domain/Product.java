package com.theblood.productservice.domain;


import com.theblood.productservice.common.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AttributeOverride(name = "id", column = @Column(name = "product_id"))
public class Product extends AbstractEntity {

//    @ManyToOne
//    @JoinColumn(name = "shop_id", nullable = false)
//    private Shop shop;


    @Column(name = "shop_id")
    private UUID shopId;

    @Column(nullable = false)
    private String name;

    @Column(name = "sku", unique = true)
    private String sku;

    private String description;

    @Column(name = "MSG")
    private LocalDate msg;

    @Column(name = "EXP")
    private LocalDate exp;

    @Column(name = "product_status")
    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;

    @Column(name = "avg_rate")
    private BigDecimal avgRate = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer quantity = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "jsonb")
    private String images;

    @Column(name = "total_feedbacks")
    Long totalFeedbacks = 0L;

    @Column(name = "average_rating")
    Double averageRating = 0.0;

    //    @OneToMany(mappedBy = "product")
//    private List<OrderItem> orderItems = new ArrayList<>();
//
    @OneToMany(mappedBy = "product")
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductSale> productSales = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductCategory> productCategories = new HashSet<>();
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
//    private List<CartItem> cartItems = new ArrayList<>();

}

