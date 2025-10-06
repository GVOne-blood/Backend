package com.theblood.identityservice.repository;

import com.theblood.identityservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> , JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // Tìm cả user đã bị xóa (bypass @Where)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameIncludingDeleted(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdIncludingDeleted(@Param("id") String id);

    // Tìm user đã bị xóa
    @Query("SELECT u FROM User u WHERE u.isDeleted = true OR u.status = 'DELETED'")
    List<User> findAllDeleted();

    // Đếm số user active
    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false AND u.status = 'ACTIVE'")
    long countActiveUsers();

}
