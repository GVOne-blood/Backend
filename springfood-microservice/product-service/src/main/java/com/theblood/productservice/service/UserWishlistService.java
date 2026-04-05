package com.theblood.productservice.service;

import com.theblood.productservice.domain.UserWishlist;
import com.theblood.productservice.repository.UserWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing UserWishlist entities.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserWishlistService {

    private final UserWishlistRepository userWishlistRepository;

    /**
     * Add product to user's wishlist
     */
    public UserWishlist addToWishlist(UUID userId, UUID productId, UUID variantId, String note) {
        // Check if already exists
        Optional<UserWishlist> existing = variantId != null
                ? userWishlistRepository.findByUserIdAndProductIdAndVariantId(userId, productId, variantId)
                : userWishlistRepository.findByUserIdAndProductId(userId, productId);

        if (existing.isPresent()) {
            // Update existing wishlist item
            UserWishlist wishlistItem = existing.get();
            wishlistItem.setNote(note);
            if (variantId != null) {
                wishlistItem.setVariantId(variantId);
            }
            return userWishlistRepository.save(wishlistItem);
        }

        // Create new wishlist item
        UserWishlist wishlistItem = new UserWishlist(userId, productId, variantId, note);
        return userWishlistRepository.save(wishlistItem);
    }

    /**
     * Add product to wishlist (simple version)
     */
    public UserWishlist addToWishlist(UUID userId, UUID productId) {
        return addToWishlist(userId, productId, null, null);
    }

    /**
     * Remove product from user's wishlist
     */
    public boolean removeFromWishlist(UUID userId, UUID productId) {
        if (userWishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            userWishlistRepository.deleteByUserIdAndProductId(userId, productId);
            return true;
        }
        return false;
    }

    /**
     * Remove specific wishlist item by ID
     */
    public boolean removeWishlistItem(UUID wishlistId, UUID userId) {
        Optional<UserWishlist> wishlistItem = userWishlistRepository.findById(wishlistId);
        if (wishlistItem.isPresent() && wishlistItem.get().getUserId().equals(userId)) {
            userWishlistRepository.deleteById(wishlistId);
            return true;
        }
        return false;
    }

    /**
     * Get user's wishlist
     */
    @Transactional(readOnly = true)
    public Page<UserWishlist> getUserWishlist(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userWishlistRepository.findByUserId(userId, pageable);
    }

    /**
     * Check if product is in user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(UUID userId, UUID productId) {
        return userWishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * Check if specific variant is in user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isVariantInWishlist(UUID userId, UUID productId, UUID variantId) {
        return userWishlistRepository.existsByUserIdAndProductIdAndVariantId(userId, productId, variantId);
    }

    /**
     * Get wishlist count for user
     */
    @Transactional(readOnly = true)
    public long getUserWishlistCount(UUID userId) {
        return userWishlistRepository.countByUserId(userId);
    }

    /**
     * Get most wishlisted products (for analytics)
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getMostWishlistedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userWishlistRepository.findMostWishlistedProducts(pageable);
    }

    /**
     * Get wishlist count for a product
     */
    @Transactional(readOnly = true)
    public long getProductWishlistCount(UUID productId) {
        return userWishlistRepository.countByProductId(productId);
    }

    /**
     * Get users who wishlisted a product (for notifications)
     */
    @Transactional(readOnly = true)
    public List<UUID> getUsersWhoWishlistedProduct(UUID productId) {
        return userWishlistRepository.findUserIdsByProductId(productId);
    }

    /**
     * Check which products from a list are in user's wishlist
     */
    @Transactional(readOnly = true)
    public List<UserWishlist> checkWishlistStatus(UUID userId, List<UUID> productIds) {
        return userWishlistRepository.findByUserIdAndProductIdIn(userId, productIds);
    }

    /**
     * Clear user's entire wishlist
     */
    public void clearUserWishlist(UUID userId) {
        userWishlistRepository.deleteByUserId(userId);
    }

    /**
     * Update wishlist item note
     */
    public Optional<UserWishlist> updateWishlistNote(UUID wishlistId, UUID userId, String note) {
        Optional<UserWishlist> wishlistItem = userWishlistRepository.findById(wishlistId);
        if (wishlistItem.isPresent() && wishlistItem.get().getUserId().equals(userId)) {
            UserWishlist item = wishlistItem.get();
            item.setNote(note);
            return Optional.of(userWishlistRepository.save(item));
        }
        return Optional.empty();
    }

    /**
     * Get wishlist item by ID (with user validation)
     */
    @Transactional(readOnly = true)
    public Optional<UserWishlist> getWishlistItem(UUID wishlistId, UUID userId) {
        Optional<UserWishlist> wishlistItem = userWishlistRepository.findById(wishlistId);
        if (wishlistItem.isPresent() && wishlistItem.get().getUserId().equals(userId)) {
            return wishlistItem;
        }
        return Optional.empty();
    }
}