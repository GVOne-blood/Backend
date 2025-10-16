package com.theblood.shopservice.model;

import com.theblood.common.model.AbstractEntity;
import com.theblood.shopservice.common.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shops")
@AttributeOverride(name = "id", column = @jakarta.persistence.Column(name = "shop_id"))
public class Shop extends AbstractEntity {

    @Column(name = "shop_name", nullable = false, unique = true)
    private String shopName;

    @Column(name = "logo")
    private String logo;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_status", nullable = false)
    private ShopStatus shopStatus;

    @Column(name = "total_product")
    private Integer totalProducts;

    @Column(name = "total_sold")
    private Integer totalSold;


}
