package com.spring_food.springfood.model;

import com.spring_food.springfood.common.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransactions extends AbstractEntity {

    @Column(name = "amount")
    BigDecimal amount;

    @Column(name = "vnp_transaction_no")
    String transactionNo;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    TransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "paymentTransactions", cascade = CascadeType.ALL)
    List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "payment_method_name")
    Payment payment;


}
