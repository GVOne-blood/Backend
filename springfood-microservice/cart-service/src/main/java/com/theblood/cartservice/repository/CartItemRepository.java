package com.theblood.cartservice.repository;

import com.theblood.cartservice.domain.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends MongoRepository<CartItem, UUID> {

    Optional<CartItem> findByCartId(UUID cartId);
}

