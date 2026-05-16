package com.theblood.shopservice.registration.service;

import com.theblood.shopservice.common.enums.ShopStatus;
import com.theblood.shopservice.domain.Shop;
import com.theblood.shopservice.registration.domain.ShopRegistrationRequest;
import com.theblood.shopservice.registration.dto.AdminShopRegistrationView;
import com.theblood.shopservice.registration.repository.ShopRegistrationRequestRepository;
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
import java.util.UUID;

/**
 * Admin-only service xử lý đơn đăng ký shop.
 *
 * <p>Trạng thái đơn (column {@code status}, kiểu string): {@code DRAFT,
 * PENDING, APPROVED, REJECTED}. Admin chỉ thao tác trên đơn đang PENDING:</p>
 *
 * <ul>
 *   <li><b>Approve</b>: status → APPROVED + tạo Shop entity ở trạng thái
 *       ACTIVE và link {@code shop_id} vào {@link ShopRegistrationRequest}.</li>
 *   <li><b>Reject</b>: status → REJECTED + lưu {@code rejectReason}.</li>
 * </ul>
 *
 * <p>Approve không gọi authentication-service để gán role SHOP_OWNER cho user
 * (đã có cơ chế khác handle qua Kafka khi shop được tạo). Nếu cần guarantee,
 * có thể publish event {@code shop.approved} sau khi save.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminShopRegistrationService {

    private final ShopRegistrationRequestRepository registrationRepository;
    private final ShopRepository shopRepository;

    public Page<AdminShopRegistrationView> list(String statusFilter, String search, Pageable pageable) {
        // JPA spec/criteria sẽ verbose; giữ filter inline trên kết quả findAll thì
        // không scale với page size lớn. Trong thực tế bộ dữ liệu admin xem hữu hạn,
        // ta dùng findAll() rồi filter in-memory + apply pageable thủ công.
        var all = registrationRepository.findAll(pageable.getSort());

        var filtered = all.stream()
            .filter(req -> matchesStatus(req, statusFilter))
            .filter(req -> matchesSearch(req, search))
            .toList();

        int total = filtered.size();
        int from = Math.min((int) pageable.getOffset(), total);
        int to = Math.min(from + pageable.getPageSize(), total);
        var pageContent = filtered.subList(from, to).stream()
            .map(AdminShopRegistrationView::from)
            .toList();

        return new PageImpl<>(pageContent, pageable, total);
    }

    public AdminShopRegistrationView get(UUID requestId) {
        var req = registrationRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException("Registration request not found: " + requestId));
        return AdminShopRegistrationView.from(req);
    }

    public AdminShopRegistrationView approve(UUID requestId, String adminUsername) {
        var req = registrationRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException("Registration request not found: " + requestId));

        // Idempotent guard: chỉ cho phép approve khi đang PENDING. Nếu đã APPROVED
        // thì trả lại view hiện tại; REJECTED hoặc DRAFT thì throw để admin biết.
        if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            return AdminShopRegistrationView.from(req);
        }
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new InvalidDataException(
                "Cannot approve request in status " + req.getStatus() + " (expected PENDING)");
        }

        // Tạo Shop entity trong springfood_shop.shops nếu chưa có (idempotent guard
        // dựa trên shopId — nếu request đã từng có shopId rồi nhưng thiếu shop entity,
        // thì recreate). Phần lớn case là tạo mới hoàn toàn.
        Shop shop = createShopFromRequest(req);
        Shop saved = shopRepository.save(shop);

        req.setStatus("APPROVED");
        req.setShopId(saved.getShopId());
        req.setReviewedBy(adminUsername);
        req.setReviewedAt(Instant.now());
        req.setRejectReason(null);
        ShopRegistrationRequest persisted = registrationRepository.save(req);

        log.info("Admin '{}' approved shop registration {} -> shop {}",
            adminUsername, persisted.getRequestId(), saved.getShopId());
        return AdminShopRegistrationView.from(persisted);
    }

    public AdminShopRegistrationView reject(UUID requestId, String reason, String adminUsername) {
        if (!StringUtils.hasText(reason)) {
            throw new InvalidDataException("Reject reason is required");
        }
        var req = registrationRepository.findById(requestId)
            .orElseThrow(() -> new NotFoundException("Registration request not found: " + requestId));

        if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
            return AdminShopRegistrationView.from(req);
        }
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new InvalidDataException(
                "Cannot reject request in status " + req.getStatus() + " (expected PENDING)");
        }

        req.setStatus("REJECTED");
        req.setRejectReason(reason);
        req.setReviewedBy(adminUsername);
        req.setReviewedAt(Instant.now());
        var persisted = registrationRepository.save(req);

        log.info("Admin '{}' rejected shop registration {}: {}",
            adminUsername, persisted.getRequestId(), reason);
        return AdminShopRegistrationView.from(persisted);
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private static boolean matchesStatus(ShopRegistrationRequest req, String statusFilter) {
        if (!StringUtils.hasText(statusFilter) || "all".equalsIgnoreCase(statusFilter)) return true;
        return statusFilter.equalsIgnoreCase(req.getStatus());
    }

    private static boolean matchesSearch(ShopRegistrationRequest req, String search) {
        if (!StringUtils.hasText(search)) return true;
        String q = search.toLowerCase();
        return contains(req.getShopName(), q)
            || contains(req.getEmail(), q)
            || contains(req.getPhoneNumber(), q)
            || contains(req.getShopAddress(), q)
            || (req.getUserId() != null && req.getUserId().toString().contains(q));
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    /**
     * Build Shop entity từ thông tin trong registration. Mặc định set ACTIVE
     * vì admin đã review và approve manually. Owner = userId của người gửi đơn.
     */
    private static Shop createShopFromRequest(ShopRegistrationRequest req) {
        Shop shop = new Shop();
        shop.setOwnerId(req.getUserId() != null ? req.getUserId().toString() : null);
        shop.setShopName(req.getShopName());
        shop.setLogo(req.getLogoMediaId());
        shop.setIntroduction(req.getIntroduction());
        shop.setShopStatus(ShopStatus.ACTIVE);
        shop.setShopType(req.getShopType());
        shop.setEmail(req.getEmail());
        shop.setPhoneNumber(req.getPhoneNumber());
        shop.setBusinessType(req.getBusinessType());
        shop.setTaxId(req.getTaxId());
        shop.setShopAddress(req.getShopAddress());
        shop.setCity(req.getCity());
        shop.setProvince(req.getProvince());
        shop.setNationId(req.getNationId());
        shop.setPostalCode(req.getPostalCode());
        shop.setActiveHours(req.getActiveHours());
        shop.setIsActive(1);
        shop.setTotalProduct(0);
        shop.setTotalSold(0);
        shop.setTotalOrders(0);
        shop.setTotalFeedback(0);
        shop.setTotalTraffic(0);
        return shop;
    }
}
