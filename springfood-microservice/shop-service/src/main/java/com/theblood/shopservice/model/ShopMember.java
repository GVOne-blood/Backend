package com.theblood.shopservice.model;


import com.theblood.common.model.AbstractEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shop_members")
@Entity
@AttributeOverride(name = "id", column = @Column(name = "shop_id"))
public class ShopMember extends AbstractEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role_name", nullable = false)
    private String roleName;

}
