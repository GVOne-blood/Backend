package com.theblood.shopservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.shopservice.domain.ShopFollow;
import com.theblood.shopservice.repository.ShopFollowRepository;
import com.theblood.springfood.client.service.LoggingService;
import com.theblood.springfood.common.enums.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing ShopFollow entities.
 */
@Service
@Transactional
public class ShopFollowService {

    private final ShopFollowRepository shopFollowRepository;
    private final LoggingService loggingService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ShopFollowService(ShopFollowRepository shopFollowRepository,
                             LoggingService loggingService,
                             ObjectMapper objectMapper) {
        this.shopFollowRepository = shopFollowRepository;
        this.loggingService = loggingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Follow a shop
     */
    public ShopFollow followShop(UUID userId, UUID shopId) {
        // Check if already following
        Optional<ShopFollow> existing = shopFollowRepository.findByUserIdAndShopId(userId, shopId);
        if (existing.isPresent()) {
            return existing.get(); // Already following
        }

        ShopFollow shopFollow = new ShopFollow(userId, shopId);
        ShopFollow saved = shopFollowRepository.save(shopFollow);

        // Log follow action
        try {
            String newValue = objectMapper.writeValueAsString(saved);
            loggingService.createLogAction(
                ActionType.CREATE.name(),
                null,
                newValue,
                "User followed shop",
                "shop_follow",
                saved.getShopId().toString(),
                userId.toString(),
                null,
                shopId.toString(),
                null,
                null
            );
        } catch (Exception e) {
            // Log error but don't fail the operation
        }

        return saved;
    }

    /**
     * Unfollow a shop
     */
    public boolean unfollowShop(UUID userId, UUID shopId) {
        if (shopFollowRepository.existsByUserIdAndShopId(userId, shopId)) {
            shopFollowRepository.deleteByUserIdAndShopId(userId, shopId);
            return true;
        }
        return false;
    }

    /**
     * Check if user is following a shop
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(UUID userId, UUID shopId) {
        return shopFollowRepository.existsByUserIdAndShopId(userId, shopId);
    }

    /**
     * Get shops followed by user
     */
    @Transactional(readOnly = true)
    public Page<ShopFollow> getFollowedShops(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return shopFollowRepository.findByUserId(userId, pageable);
    }

    /**
     * Get followers of a shop
     */
    @Transactional(readOnly = true)
    public Page<ShopFollow> getShopFollowers(UUID shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return shopFollowRepository.findByShopId(shopId, pageable);
    }

    /**
     * Get follower count for a shop
     */
    @Transactional(readOnly = true)
    public long getShopFollowerCount(UUID shopId) {
        return shopFollowRepository.countByShopId(shopId);
    }

    /**
     * Get count of shops followed by user
     */
    @Transactional(readOnly = true)
    public long getUserFollowingCount(UUID userId) {
        return shopFollowRepository.countByUserId(userId);
    }

    /**
     * Get follower counts for multiple shops
     */
    @Transactional(readOnly = true)
    public List<Object[]> getFollowerCountsForShops(List<UUID> shopIds) {
        return shopFollowRepository.getFollowerCountsByShopIds(shopIds);
    }

    /**
     * Get most followed shops (for recommendations)
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getMostFollowedShops(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return shopFollowRepository.findMostFollowedShops(pageable);
    }

    /**
     * Get recent followers for a shop
     */
    @Transactional(readOnly = true)
    public List<ShopFollow> getRecentFollowers(UUID shopId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        return shopFollowRepository.findRecentFollowersByShop(shopId, since);
    }

    /**
     * Get users who follow multiple shops from a list (for recommendations)
     */
    @Transactional(readOnly = true)
    public List<UUID> getUsersFollowingMultipleShops(List<UUID> shopIds) {
        return shopFollowRepository.findUsersFollowingMultipleShops(shopIds);
    }

    /**
     * Get follow statistics by date range
     */
    @Transactional(readOnly = true)
    public List<Object[]> getFollowStatistics(Instant startDate, Instant endDate) {
        return shopFollowRepository.getFollowStatsByDateRange(startDate, endDate);
    }

    /**
     * Get follow relationship details
     */
    @Transactional(readOnly = true)
    public Optional<ShopFollow> getFollowRelationship(UUID userId, UUID shopId) {
        return shopFollowRepository.findByUserIdAndShopId(userId, shopId);
    }

    /**
     * Toggle follow status (follow if not following, unfollow if following)
     */
    public boolean toggleFollow(UUID userId, UUID shopId) {
        if (isFollowing(userId, shopId)) {
            unfollowShop(userId, shopId);
            return false; // Now unfollowing
        } else {
            followShop(userId, shopId);
            return true; // Now following
        }
    }

    /**
     * Get follow status for multiple shops (batch check)
     */
    @Transactional(readOnly = true)
    public List<ShopFollow> getFollowStatusForShops(UUID userId, List<UUID> shopIds) {
        // This would need a custom query, for now we'll use a simple approach
        return shopFollowRepository.findByUserId(userId, Pageable.unpaged())
            .getContent()
            .stream()
            .filter(follow -> shopIds.contains(follow.getShopId()))
            .toList();
    }

    /**
     * Remove all follows for a user (when user is deleted)
     */
    public void removeAllUserFollows(UUID userId) {
        shopFollowRepository.deleteByUserId(userId);
    }

    /**
     * Remove all follows for a shop (when shop is deleted)
     */
    public void removeAllShopFollows(UUID shopId) {
        shopFollowRepository.deleteByShopId(shopId);
    }

    /**
     * Get daily follow statistics for the last N days
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDailyFollowStats(int days) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);
        return shopFollowRepository.getFollowStatsByDateRange(startDate, endDate);
    }
}
