package com.theblood.paymentservice.repository;

import com.theblood.paymentservice.model.PaymentTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentTransactionsRepository extends JpaRepository<PaymentTransactions, UUID> {

}
