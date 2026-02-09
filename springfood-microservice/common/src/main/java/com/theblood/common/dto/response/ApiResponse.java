package com.theblood.common.dto.response;


import com.theblood.common.util.MessageUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    String code;
    String message;
    T detail;

    // Success with message
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .detail(data)
                .code(MessageUtils.getMessage("success.code"))
                .build();
    }

    // Error response (without detail)
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .detail(null)
                .code(code)
                .build();
    }

    // Error response (with detail)
    public static <T> ApiResponse<T> error(String code, String message, T detail) {
        return ApiResponse.<T>builder()
                .message(message)
                .detail(detail)
                .code(code)
                .build();
    }
}
