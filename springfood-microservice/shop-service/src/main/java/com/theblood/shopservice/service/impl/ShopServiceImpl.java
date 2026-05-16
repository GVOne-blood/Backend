package com.theblood.shopservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.shopservice.domain.Shop;
import com.theblood.shopservice.dto.request.ShopRequest;
import com.theblood.shopservice.dto.response.ShopDetailResponse;
import com.theblood.shopservice.dto.response.ShopResponse;
import com.theblood.shopservice.repository.ShopMemberRepository;
import com.theblood.shopservice.repository.ShopRepository;
import com.theblood.shopservice.service.ShopConsumerService;
import com.theblood.shopservice.service.ShopService;
import com.theblood.springfood.client.service.LoggingService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import com.theblood.springfood.common.enums.Role;
import com.theblood.springfood.common.exception.custom.NotFoundException;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopServiceImpl implements ShopService {

    ShopRepository shopRepository;
    ShopConsumerService shopConsumerService;
    ShopMemberRepository shopMemberRepository;
    LoggingService loggingService;
    RedisServiceWrapper redisServiceWrapper;
    
    @Qualifier("redisObjectMapper")
    ObjectMapper objectMapper;

    @Override
    public Page<ShopResponse> getAllShops(Pageable pageable) {
        return null;
    }

    @Override
    public ShopResponse getShopById(String shopId) {
        return null;
    }

    @Override
    public ShopResponse getShop(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidDataException("User ID is required");
        }
        Optional<Shop> shop = shopRepository.findShopByOwnerId(userId);
        if (shop.isEmpty()) throw new NotFoundException("Shop not found for user id: " + userId);

        ShopResponse res = objectMapper.convertValue(shop.get(), ShopResponse.class);
        return res;
    }

    @Override
    public Optional<ShopDetailResponse> findShopOfUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return shopRepository.findShopByOwnerId(userId)
            .map(this::toShopDetailResponse);
    }

    private ShopDetailResponse toShopDetailResponse(Shop shop) {
        return ShopDetailResponse.builder()
            .shopId(shop.getShopId() != null ? shop.getShopId().toString() : null)
            .shopName(shop.getShopName())
            .logo(shop.getLogo())
            .introduction(shop.getIntroduction())
            .shopAddress(shop.getShopAddress())
            .city(shop.getCity())
            .province(shop.getProvince())
            .avgStar(shop.getAvgStar())
            .totalFeedback(shop.getTotalFeedback())
            .activeHours(shop.getActiveHours())
            .distance(0.0)
            .totalProducts(shop.getTotalProduct())
            .totalSold(shop.getTotalSold())
            .totalOrders(shop.getTotalOrders())
            .phoneNumber(shop.getPhoneNumber())
            .email(shop.getEmail())
            .shopStatus(shop.getShopStatus() != null ? shop.getShopStatus().name() : null)
            .isActive(shop.getIsActive())
            .shopType(shop.getShopType())
            .businessType(shop.getBusinessType())
            .build();
    }

    @Override
    @Transactional
    public ShopResponse shopRegister(ShopRequest shopRequest) {
        CustomUserPrincipal userContext = UserContextHolder.getContext();

        Shop shop = new Shop();
        shop.setShopName(shopRequest.getShopName());
        shop.setShopAddress(shopRequest.getShopAddress());
        shop.setAvgStar(BigDecimal.ZERO);
        shop.setShopAddress(shopRequest.getShopAddress());
        shop.setIsActive(0);
        shop.setShopType(shopRequest.getShopType());
//        shop.setTotalSold(0);
//        shop.setTotalTraffic(0);
//        shop.setTotalOrders(0);

        //send to admin to get approvement
        ShopResponse req = ShopResponse.builder().build();
        return req;
    }

    @Override
    public boolean isUserOwnShop(UUID userId, UUID shopId) {
        return shopMemberRepository.existsByIdAndUserIdAndRoleName(shopId.toString(), userId.toString(), Role.SHOP_OWNER.name());
    }

    @Override
    public Page<ShopResponse> getFeaturedShops(Pageable pageable) {
        // Cache key for featured shops
        String cacheKey = "featured_shops:page:" + pageable.getPageNumber() + ":size:" + pageable.getPageSize();
        
        // Try to get from cache first
        Object cachedData = redisServiceWrapper.getValue(cacheKey);
        if (cachedData != null) {
            try {
                // Deserialize cached page data
                @SuppressWarnings("unchecked")
                List<ShopResponse> cachedShops = objectMapper.convertValue(cachedData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ShopResponse.class));
                
                log.info("Cache hit for featured shops with key: {}", cacheKey);
                // Return cached page
                return new PageImpl<>(cachedShops, pageable, cachedShops.size());
            } catch (Exception e) {
                log.warn("Failed to deserialize cached featured shops, fetching from DB", e);
            }
        }
        
        // Cache miss - fetch from database
        log.info("Cache miss for featured shops, fetching from DB");
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        Page<Shop> shops = shopRepository.findTop10ShopsByTotalSoldLastMonth(since, pageable);
        List<ShopResponse> shopResponses = shops.getContent().stream()
            .map(shop -> objectMapper.convertValue(shop, ShopResponse.class))
            .collect(Collectors.toList());
        
        Page<ShopResponse> result = new PageImpl<>(shopResponses, pageable, shops.getTotalElements());
        
        // Cache the result for 10 minutes (featured shops don't change frequently)
        try {
            redisServiceWrapper.setValueWithTimeout(cacheKey, shopResponses, 10, TimeUnit.MINUTES);
            log.info("Cached featured shops with key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Failed to cache featured shops", e);
        }
        
        return result;
    }

    @Override
    public ShopDetailResponse getShopDetail(String shopId) {
        if (shopId == null || shopId.isBlank()) {
            throw new InvalidDataException("Shop ID is required");
        }
        // Cache key for shop detail
        String cacheKey = "shop_detail:" + shopId;
        
        // Try to get from cache first
        Object cachedData = redisServiceWrapper.getValue(cacheKey);
        if (cachedData != null) {
            try {
                ShopDetailResponse cachedShop = objectMapper.convertValue(cachedData, ShopDetailResponse.class);
                log.info("Cache hit for shop detail with key: {}", cacheKey);
                return cachedShop;
            } catch (Exception e) {
                log.warn("Failed to deserialize cached shop detail, fetching from DB", e);
            }
        }
        
        // Cache miss - fetch from database
        log.info("Cache miss for shop detail, fetching from DB");
        Shop shop = shopRepository.findById(UUID.fromString(shopId))
            .orElseThrow(() -> new NotFoundException("Shop not found with id: " + shopId));
        
        // Map Shop entity to ShopDetailResponse
        ShopDetailResponse response = ShopDetailResponse.builder()
            .shopId(shop.getShopId() != null ? shop.getShopId().toString() : null)
            .shopName(shop.getShopName())
            .logo(shop.getLogo())
            .introduction(shop.getIntroduction())
            .shopAddress(shop.getShopAddress())
            .city(shop.getCity())
            .province(shop.getProvince())
            .avgStar(shop.getAvgStar())
            .totalFeedback(shop.getTotalFeedback())
            .activeHours(shop.getActiveHours())
            .distance(0.0) // TODO: Calculate distance based on user location
            .totalProducts(shop.getTotalProduct())
            .totalSold(shop.getTotalSold())
            .totalOrders(shop.getTotalOrders())
            .phoneNumber(shop.getPhoneNumber())
            .email(shop.getEmail())
            .shopStatus(shop.getShopStatus() != null ? shop.getShopStatus().name() : null)
            .isActive(shop.getIsActive())
            .shopType(shop.getShopType())
            .businessType(shop.getBusinessType())
            .build();
        
        // Cache the result for 5 minutes (shop detail may change)
        try {
            redisServiceWrapper.setValueWithTimeout(cacheKey, response, 5, TimeUnit.MINUTES);
            log.info("Cached shop detail with key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Failed to cache shop detail", e);
        }
        
        return response;
    }


}
