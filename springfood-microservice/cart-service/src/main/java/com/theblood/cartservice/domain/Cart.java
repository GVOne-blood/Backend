package com.theblood.cartservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts") // Tên collection trong MongoDB
public class Cart {

    @Id
    private String userId; // Dùng chính UUID của User làm khóa chính cho Cart

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal totalPrice = BigDecimal.ZERO; // Tổng tiền tạm tính

    private Integer totalItems = 0; // Tổng số lượng sản phẩm

    // Mặc định khởi tạo list rỗng để tránh NullPointerException
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @LastModifiedDate
    private LocalDateTime updatedAt; // Tự động cập nhật khi save()
}