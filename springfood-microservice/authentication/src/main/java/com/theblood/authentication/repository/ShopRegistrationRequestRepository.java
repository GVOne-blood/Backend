package com.theblood.authentication.repository;

import com.theblood.authentication.model.ShopRegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRegistrationRequestRepository extends JpaRepository<ShopRegistrationRequest, String> {

    @Query("select req from ShopRegistrationRequest req order by req.status desc")
    Page<ShopRegistrationRequest> findAll(Pageable pageable);
}
