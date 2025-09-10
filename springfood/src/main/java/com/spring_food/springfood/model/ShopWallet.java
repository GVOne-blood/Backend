package com.spring_food.springfood.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shop_wallet")
@AttributeOverride(name = "id", column = @Column(name = "wallet_id"))
public class ShopWallet extends AbstractEntity{

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "pending_amount", precision = 15, scale = 2)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    @Column(name = "locked_amount", precision = 15, scale = 2)
    private BigDecimal lockedAmount = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "shop_id", unique = true, nullable = false)
    private Shop shop;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_id")
    private List<WalletTransaction> walletTransactions = new ArrayList<>();

}
