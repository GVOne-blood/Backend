package com.theblood.springfood.common.enums;

import lombok.Getter;

/**
 * File status enum - Trạng thái của file trong hệ thống
 */
@Getter
public enum FileStatus {

    ACTIVE("ACTIVE", "File đang hoạt động, có thể hiển thị"),
    INACTIVE("INACTIVE", "File tạm ngừng hoạt động"),
    PENDING("PENDING", "File đang xử lý (resize, compress, v.v.)"),
    DELETED("DELETED", "File đã bị xóa (soft delete)"),
    CORRUPTED("CORRUPTED", "File bị hỏng, không thể truy cập"),
    QUARANTINED("QUARANTINED", "File bị cách ly do an toàn"),
    ARCHIVED("ARCHIVED", "File được lưu trữ, ít sử dụng");

    private final String code;
    private final String description;

    FileStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Tìm FileStatus từ string code
     *
     * @param code ví dụ: "ACTIVE"
     * @return FileStatus nếu tìm thấy, mặc định ACTIVE
     */
    public static FileStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ACTIVE;
        }

        try {
            return FileStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE; // Default status
        }
    }

    /**
     * Kiểm tra file có thể hiển thị không
     */
    public boolean isViewable() {
        return this == ACTIVE || this == ARCHIVED;
    }

    /**
     * Kiểm tra file đang xử lý không
     */
    public boolean isProcessing() {
        return this == PENDING;
    }

    /**
     * Kiểm tra file có khả dụng không (không phải deleted hay corrupted)
     */
    public boolean isAvailable() {
        return this != DELETED && this != CORRUPTED && this != QUARANTINED;
    }
}
