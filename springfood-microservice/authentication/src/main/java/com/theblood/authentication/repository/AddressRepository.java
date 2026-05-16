package com.theblood.authentication.repository;

import com.theblood.authentication.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt ASC")
    List<Address> findAllUserAddressesByUserId(@Param("userId") UUID userId);

    Optional<Address> findByIdAndUserId(UUID id, UUID userId);

    /** Bỏ flag default ở tất cả địa chỉ của user (để chuẩn bị set default mới). */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    int unsetDefaultForUser(@Param("userId") UUID userId);

    /** Đếm số địa chỉ của user — dùng để biết user có địa chỉ nào chưa, set default cho address đầu tiên. */
    long countByUserId(UUID userId);
}
