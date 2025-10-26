package com.spring_food.springfood.model;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
class ShopMemberId implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "shop_id")
    private String shopId;
}


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shop_member")

public class ShopMember {
    @EmbeddedId
    private ShopMemberId shopMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("shopId")
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_name", nullable = false)
    private Role role;


}
