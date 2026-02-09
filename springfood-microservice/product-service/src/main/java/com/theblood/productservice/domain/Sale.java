package com.theblood.productservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "sales")
@AttributeOverride(name = "id", column = @Column(name = "sale_id"))
public class Sale extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

//    @Column(name = "discount_amount", precision = 15, scale = 2)
//    private BigDecimal discountAmount;

    private String conditions;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private Set<ProductSale> productSales = new HashSet<>();
}
