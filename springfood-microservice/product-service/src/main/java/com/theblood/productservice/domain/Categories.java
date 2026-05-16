package com.theblood.productservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Categories {

    @OneToMany(mappedBy = "parentCategories", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Categories> children;

    @Column(name = "slug", unique = true, nullable = false)
    private String slug;

    @Id
    @Column(name = "category_name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private boolean isActive;

    /**
     * Owning shop. {@code NULL} means the category is a system-wide category
     * shared by every shop. Hibernate's {@code ddl-auto=update} will add the
     * column on first boot.
     */
    @Column(name = "shop_id")
    private UUID shopId;

    /** Optional grouping code, mirrors {@code categories.category_group_code}. */
    @Column(name = "category_group_code")
    private String categoryGroupCode;

    @Column(name = "is_lock")
    private Integer isLock;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Categories parentCategories;

    @OneToMany(mappedBy = "categories", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductCategory> productCategories;

    @Transient //  Báo cho JPA bỏ qua phương thức này, không ánh xạ vào cột nào cả.
    public String getCategoryName() {
        return this.getName();// Ủy quyền lời gọi đến getId() của lớp cha
    }

    // 2. Tạo setter ngữ nghĩa
    @Transient
    public void setCategoryName(String categoryName) {
        this.setName(categoryName); // Ủy quyền lời gọi đến setId() của lớp cha
    }
}
