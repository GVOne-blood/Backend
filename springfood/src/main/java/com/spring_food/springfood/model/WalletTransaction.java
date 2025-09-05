package com.spring_food.springfood.model;


import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "wallet_transaction")
@AttributeOverride(name = "id", column = @Column(name = "transaction_id"))
public class WalletTransaction extends AbstractEntity {

    @Column(name = "transaction_code")
    private String transactionCode;





}
