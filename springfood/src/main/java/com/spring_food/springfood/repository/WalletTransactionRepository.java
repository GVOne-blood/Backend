package com.spring_food.springfood.repository;

import com.spring_food.springfood.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, String> {
    

}
