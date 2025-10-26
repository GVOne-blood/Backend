package com.theblood.paymentservice.model;

import com.theblood.common.model.AbstractEntity;
import com.theblood.paymentservice.common.enums.TransactionStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions")
@AttributeOverride(name = "id", column = @Column(name = "id"))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactions extends AbstractEntity {

    @Column(name = "user_id")
    String userId;

    @Column(name = "payment_method_name")
    String paymentMethodName;

    @Column(name = "amount")
    BigDecimal amount;

    @Column(name = "status")
    TransactionStatus status;

    @Column(name = "provider_transaction_ref")
    String providerTransactionRef;

    @Column(name = "reference_type")
    String referenceType;

    @Column(name = "reference_id")
    String referenceId;

}
