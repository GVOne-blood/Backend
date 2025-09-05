package com.spring_food.springfood.model;

import com.spring_food.springfood.model.ENUM.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "product")
@AttributeOverride(name = "id", column = @Column(name = "product_id"))
public class Product extends AbstractEntity {
    
    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "MSG")
    private LocalDateTime msg;

    @Column(name = "EXP")
    private LocalDateTime exp;

    @Column(name = "product_status")
    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;

    @Column(name = "avg_rate")
    private BigDecimal avgRate;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "images")
    private String images;

    @OneToMany(mappedBy = "product")
    private List<BookingItem> bookingItems = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductTag> productTags = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductSale> productSales = new HashSet<>();
}
