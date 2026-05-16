package com.theblood.shopservice.dto.response;

import java.math.BigDecimal;

/**
 * Stats cards trên trang Admin Shops.
 *
 * @param totalShops      Tổng shop trên nền tảng (đã được duyệt — bất cứ status nào ngoại trừ PENDING_APPROVAL).
 * @param activeShops     Shop đang ACTIVE (bán hàng).
 * @param inactiveShops   Shop đang INACTIVE (tạm dừng).
 * @param bannedShops     Shop đang BANNED (vi phạm chính sách).
 * @param closedShops     Shop đã CLOSED.
 * @param pendingShops    Shop đang PENDING_APPROVAL.
 * @param totalGmv        Tổng GMV (sum final_price các đơn COMPLETED).
 * @param newShopsLast30  Số shop mới (created_at trong 30 ngày gần đây).
 */
public record AdminShopStatsResponse(
    long totalShops,
    long activeShops,
    long inactiveShops,
    long bannedShops,
    long closedShops,
    long pendingShops,
    BigDecimal totalGmv,
    long newShopsLast30
) {}
