package com.theblood.paymentservice.repository;

import com.theblood.paymentservice.model.PaymentTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PaymentTransactionsRepository extends JpaRepository<PaymentTransactions, UUID> {


    @Query("SELECT p FROM PaymentTransactions p WHERE p.referenceId = ?1")
    List<PaymentTransactions> findAllByReferenceId(UUID referenceId);
}
