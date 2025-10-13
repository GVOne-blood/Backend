package com.theblood.productservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode // Rất quan trọng cho khóa phức hợp
@Embeddable // Đánh dấu lớp này có thể được nhúng vào một entity khác
class ProductCategoryId implements Serializable {

    @Column(name = "product_id")
    private UUID productId; // Kiểu dữ liệu phải khớp với ID của entity Product

    @Column(name = "category_name")
    private String categoryName; // Kiểu dữ liệu phải khớp với ID của entity Categories
}

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_categories")
public class ProductCategory {

    // Sử dụng @EmbeddedId thay vì nhiều @Id
    @EmbeddedId
    private ProductCategoryId id;

    // fetch = FetchType.LAZY là một thói quen tốt để tối ưu hiệu suất
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId") // Ánh xạ tới thuộc tính 'productId' trong lớp ProductCategoryId
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryName") // Ánh xạ tới thuộc tính 'categoryName' trong lớp ProductCategoryId
    @JoinColumn(name = "category_name")
    private Categories categories;
}
