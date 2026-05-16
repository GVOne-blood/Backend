package com.theblood.paymentservice.model;

import com.theblood.springfood.common.model.AbstractEntity;
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
@Entity
@Table(name = "bank_accounts")
public class BankAccount extends AbstractEntity {

    @Column(name = "shop_id")
    private UUID shopId;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "account_holder_name", length = 255)
    private String accountHolderName;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "is_verified")
    private Boolean isVerified;
}
