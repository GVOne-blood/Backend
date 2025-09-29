package com.spring_food.springfood.repository;

import com.spring_food.springfood.model.PaymentTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionsRepository extends JpaRepository<PaymentTransactions, String> {
    
}
