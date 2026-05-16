package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;

/**
 * Platform-wide KPIs cho admin dashboard.
 *
 * <p>Lấy snapshot toàn nền tảng tại thời điểm gọi (không phụ thuộc range cụ thể):
 * tổng số shops/users/products cùng với GMV (gross merchandise value) và số đơn
 * COMPLETED tích luỹ. Range-scoped numbers (revenue 7 ngày, đăng ký mới…) được
 * tách ra ở các DTO khác.</p>
 *
 * <p>Status enum khớp với {@code ShopStatus.java} của shop-service:
 * {@code PENDING_APPROVAL, ACTIVE, CLOSED, INACTIVE}.</p>
 *
 * @param totalShops          Số shop đã từng được duyệt (ACTIVE + CLOSED).
 * @param activeShops         Số shop đang ACTIVE (có thể bán hàng).
 * @param pendingShops        Số shop đang chờ duyệt (PENDING_APPROVAL).
 * @param suspendedShops      Số shop bị tạm khoá (INACTIVE) hoặc CLOSED.
 * @param totalUsers          Tổng người dùng (status != BANNED).
 * @param totalCustomers      Số user có role CUSTOMER.
 * @param totalShopOwners     Số user có role SHOP_OWNER.
 * @param totalProducts       Tổng sản phẩm trên nền tảng (active).
 * @param totalOrders         Tổng đơn hàng đã COMPLETED toàn platform.
 * @param totalRevenue        Tổng GMV (sum final_price) các đơn COMPLETED.
 * @param pendingRegistrations Số đơn đăng ký shop chờ xử lý (status PENDING/DRAFT).
 */
public record PlatformOverviewDTO(
    long totalShops,
    long activeShops,
    long pendingShops,
    long suspendedShops,
    long totalUsers,
    long totalCustomers,
    long totalShopOwners,
    long totalProducts,
    long totalOrders,
    BigDecimal totalRevenue,
    long pendingRegistrations
) {}
