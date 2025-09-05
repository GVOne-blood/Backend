package com.spring_food.springfood.model;


import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shop_wallet")
@AttributeOverride(name = "id", column = @Column(name = "wallet_id"))
public class ShopWallet extends AbstractEntity{

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "pending_amount")
    private BigDecimal pending_amount;

    @Column(name = "locked_amount")
    private BigDecimal locked_amount;

}
