package com.theblood.shopservice.service;

import com.theblood.shopservice.common.enums.ShopStatus;
import com.theblood.shopservice.domain.Shop;
import com.theblood.shopservice.dto.request.AdminShopUpdateRequest;
import com.theblood.shopservice.dto.response.AdminShopRowResponse;
import com.theblood.shopservice.dto.response.AdminShopStatsResponse;
import com.theblood.shopservice.repository.AdminShopReportRepository;
import com.theblood.shopservice.repository.ShopRepository;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import com.theblood.springfood.common.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin-only operations cho Shop. Mọi mutation đều log để audit.
 *
 * <p>Status transitions admin được phép thực hiện:</p>
 * <ul>
 *   <li>{@code ACTIVE → INACTIVE} (suspend)</li>
 *   <li>{@code ACTIVE → BANNED} (ban vì vi phạm)</li>
 *   <li>{@code INACTIVE → ACTIVE} (reactivate)</li>
 *   <li>{@code BANNED → ACTIVE} (unban)</li>
 *   <li>{@code * → CLOSED} (đóng vĩnh viễn)</li>
 * </ul>
 *
 * <p>{@code PENDING_APPROVAL → ACTIVE} đi qua flow approve registration ở
 * {@code AdminShopRegistrationService}, không qua đây.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminShopService {

    private final ShopRepository shopRepository;
    private final AdminShopReportRepository adminShopReportRepository;

    public Page<AdminShopRowResponse> listShops(
        String statusFilter, String search, Pageable pageable
    ) {
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<AdminShopRowResponse> rows = adminShopReportRepository.findShops(statusFilter, search, limit, offset);
        long total = adminShopReportRepository.countShops(statusFilter, search);
        return new PageImpl<>(rows, pageable, total);
    }

    public AdminShopStatsResponse getStats() {
        return adminShopReportRepository.getStats();
    }

    public AdminShopRowResponse getShop(UUID shopId) {
        AdminShopRowResponse row = adminShopReportRepository.findOneById(shopId);
        if (row == null) {
            throw new NotFoundException("Shop not found: " + shopId);
        }
        return row;
    }

    /**
     * Generic status mutation. Validate transition để tránh admin set sai state
     * (vd: từ CLOSED → ACTIVE — khi shop đã đóng vĩnh viễn không nên revive).
     */
    public AdminShopRowResponse changeStatus(UUID shopId, ShopStatus target, String adminUsername) {
        Shop shop = loadShop(shopId);
        ShopStatus current = shop.getShopStatus();

        if (current == target) {
            // Idempotent: trả về row hiện tại không log gì.
            return adminShopReportRepository.findOneById(shopId);
        }
        if (current == ShopStatus.CLOSED) {
            throw new InvalidDataException("Cannot change status of a CLOSED shop");
        }
        if (current == ShopStatus.PENDING_APPROVAL) {
            throw new InvalidDataException(
                "Shop is still pending approval — use registration approval flow");
        }
        // Banned → Active phải qua endpoint /unban riêng để clear ban metadata.
        if (current == ShopStatus.BANNED && target == ShopStatus.ACTIVE) {
            throw new InvalidDataException("Use /unban to lift a ban (clears reason/timestamp)");
        }

        shop.setShopStatus(target);
        if (target == ShopStatus.INACTIVE) shop.setIsActive(0);
        if (target == ShopStatus.ACTIVE) shop.setIsActive(1);
        if (target == ShopStatus.CLOSED) shop.setIsActive(0);

        Shop saved = shopRepository.save(shop);
        log.info("Admin '{}' changed shop {} status: {} → {}",
            adminUsername, saved.getShopId(), current, target);
        return adminShopReportRepository.findOneById(saved.getShopId());
    }

    public AdminShopRowResponse banShop(UUID shopId, String reason, String adminUsername) {
        if (!StringUtils.hasText(reason)) {
            throw new InvalidDataException("Ban reason is required");
        }
        Shop shop = loadShop(shopId);
        if (shop.getShopStatus() == ShopStatus.BANNED) {
            // Đã bị ban — update reason mới (admin có thể bổ sung lý do).
            shop.setBannedReason(reason);
            shop.setBannedAt(Instant.now());
            shop.setBannedBy(adminUsername);
            Shop saved = shopRepository.save(shop);
            log.info("Admin '{}' updated ban reason for shop {}: {}",
                adminUsername, saved.getShopId(), reason);
            return adminShopReportRepository.findOneById(saved.getShopId());
        }
        if (shop.getShopStatus() == ShopStatus.CLOSED) {
            throw new InvalidDataException("Cannot ban a CLOSED shop");
        }

        shop.setShopStatus(ShopStatus.BANNED);
        shop.setIsActive(0);
        shop.setBannedReason(reason);
        shop.setBannedAt(Instant.now());
        shop.setBannedBy(adminUsername);
        Shop saved = shopRepository.save(shop);
        log.warn("Admin '{}' banned shop {}: {}", adminUsername, saved.getShopId(), reason);
        return adminShopReportRepository.findOneById(saved.getShopId());
    }

    public AdminShopRowResponse unbanShop(UUID shopId, String adminUsername) {
        Shop shop = loadShop(shopId);
        if (shop.getShopStatus() != ShopStatus.BANNED) {
            // Idempotent: đã active rồi thì trả luôn.
            return adminShopReportRepository.findOneById(shopId);
        }
        shop.setShopStatus(ShopStatus.ACTIVE);
        shop.setIsActive(1);
        // Giữ lại lịch sử (bannedReason/bannedAt/bannedBy) để audit, không clear.
        Shop saved = shopRepository.save(shop);
        log.info("Admin '{}' unbanned shop {}", adminUsername, saved.getShopId());
        return adminShopReportRepository.findOneById(saved.getShopId());
    }

    public AdminShopRowResponse updateShop(UUID shopId, AdminShopUpdateRequest req, String adminUsername) {
        Shop shop = loadShop(shopId);
        if (StringUtils.hasText(req.getShopName())) shop.setShopName(req.getShopName());
        if (req.getLogo() != null) shop.setLogo(req.getLogo());
        if (req.getIntroduction() != null) shop.setIntroduction(req.getIntroduction());
        if (req.getShopType() != null) shop.setShopType(req.getShopType());
        if (req.getBusinessType() != null) shop.setBusinessType(req.getBusinessType());
        if (req.getEmail() != null) shop.setEmail(req.getEmail());
        if (req.getPhoneNumber() != null) shop.setPhoneNumber(req.getPhoneNumber());
        if (req.getTaxId() != null) shop.setTaxId(req.getTaxId());
        if (req.getShopAddress() != null) shop.setShopAddress(req.getShopAddress());
        if (req.getCity() != null) shop.setCity(req.getCity());
        if (req.getProvince() != null) shop.setProvince(req.getProvince());
        if (req.getPostalCode() != null) shop.setPostalCode(req.getPostalCode());
        if (req.getActiveHours() != null) shop.setActiveHours(req.getActiveHours());
        Shop saved = shopRepository.save(shop);
        log.info("Admin '{}' updated shop {}", adminUsername, saved.getShopId());
        return adminShopReportRepository.findOneById(saved.getShopId());
    }

    /** Soft-delete: status → CLOSED. Không xoá row vì FK ràng buộc với orders/products. */
    public void closeShop(UUID shopId, String adminUsername) {
        Shop shop = loadShop(shopId);
        if (shop.getShopStatus() == ShopStatus.CLOSED) {
            return;
        }
        shop.setShopStatus(ShopStatus.CLOSED);
        shop.setIsActive(0);
        shopRepository.save(shop);
        log.warn("Admin '{}' closed shop {}", adminUsername, shop.getShopId());
    }

    private Shop loadShop(UUID shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new NotFoundException("Shop not found: " + shopId));
    }
}
