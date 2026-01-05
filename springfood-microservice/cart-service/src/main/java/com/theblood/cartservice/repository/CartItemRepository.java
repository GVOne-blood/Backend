package com.theblood.cartservice.repository;

import com.theblood.cartservice.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartItemRepository extends MongoRepository<CartItem, UUID> {
}
