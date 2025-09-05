package com.spring_food.springfood.model;

import com.spring_food.springfood.model.ENUM.ShopStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "shop")
@AttributeOverride(name = "id", column = @Column(name = "shop_id"))
public class Shop extends AbstractEntity {

    @OneToOne
    @JoinColumn(name = "owner_id", unique = true, nullable = false)
    private User owner;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    private String logo;

    @Column(name = "total_product")
    private int totalProduct = 0;

    @Column(name = "total_sold")
    private int totalSold = 0;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_status")
    private ShopStatus shopStatus;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();
}
