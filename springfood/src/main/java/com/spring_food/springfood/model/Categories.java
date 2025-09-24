package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
@AttributeOverride(name = "id", column = @Column(name = "slug"))
public class Categories extends AbstractEntity {

    @OneToMany(mappedBy = "parentCategories", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Categories> children;
    @Column(name = "category_name")
    private String name;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "is_active")
    private boolean isActive;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Categories parentCategories;
    @OneToMany(mappedBy = "categories", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductCategory> productCategories;

    @Transient //  Báo cho JPA bỏ qua phương thức này, không ánh xạ vào cột nào cả.
    public String getCategoryName() {
        return this.getId(); // Ủy quyền lời gọi đến getId() của lớp cha
    }

    // 2. Tạo setter ngữ nghĩa
    @Transient
    public void setCategoryName(String categoryName) {
        this.setId(categoryName); // Ủy quyền lời gọi đến setId() của lớp cha
    }
}
