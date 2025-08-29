package com.example.demo.AssignmentBR;

import java.time.LocalDateTime;

/**
 * Một đối tượng bất biến đại diện cho một dòng log.
 * Sử dụng record để tự động tạo constructor, getters, equals(), hashCode(), và toString().
 */
public record LogEntry(
        LocalDateTime timestamp,
        String level,
        String service,
        String message
) {
}
