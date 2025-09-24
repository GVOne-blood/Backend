package com.spring_food.springfood.repository;

import com.spring_food.springfood.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    @Query("SELECT a FROM Address a WHERE a.id = :addressId AND a.user.id = :userId")
    Optional<Address> findByUserId(String addressId, String userId);
}
