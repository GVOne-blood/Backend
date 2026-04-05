package com.theblood.authentication.common;

public enum RegistrationStatus {
    DRAFT,      // Mới tạo, chưa nộp
    PENDING,    // Đã nộp, chờ duyệt
    APPROVED,   // Đã duyệt
    REJECTED,   // Bị từ chối
    CANCELLED   // Người dùng tự huỷ
}

