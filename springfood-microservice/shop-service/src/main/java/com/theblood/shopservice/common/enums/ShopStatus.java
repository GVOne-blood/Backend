package com.theblood.shopservice.common.enums;

/**
 * Trạng thái vận hành của shop trong hệ thống.
 *
 * <ul>
 *   <li>{@code PENDING_APPROVAL} — vừa đăng ký, chờ admin duyệt.</li>
 *   <li>{@code ACTIVE} — đang hoạt động bình thường.</li>
 *   <li>{@code INACTIVE} — owner tự đóng tạm hoặc admin disable.</li>
 *   <li>{@code BANNED} — admin ban vĩnh viễn vì vi phạm chính sách. Khác
 *       INACTIVE ở chỗ shop không thể tự re-active; chỉ admin unban được.</li>
 *   <li>{@code CLOSED} — đã đóng cửa hoàn toàn (soft delete bởi owner hoặc admin).</li>
 * </ul>
 */
public enum ShopStatus {
    PENDING_APPROVAL, ACTIVE, INACTIVE, BANNED, CLOSED
}
