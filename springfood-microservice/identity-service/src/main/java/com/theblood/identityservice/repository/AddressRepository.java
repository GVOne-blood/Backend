package com.theblood.identityservice.repository;

import com.theblood.identityservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId")
    List<Address> findAllUserAddressesByUserId(UUID userId);

}
