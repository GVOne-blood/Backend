package com.theblood.productservice.domain;

import com.theblood.productservice.common.enums.AttributeSchema;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

public class ProductAttributeTemplate extends AbstractEntity {
    @Column(name = "shop_id")
    private UUID shopId;

    @Column(name = "category_id")
    private UUID categoryId;  // Optional: template cho category

    @Column(name = "template_name")
    private String templateName;  // "Áo thun", "Laptop", "Điện thoại"

    // Schema definition
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_schema", columnDefinition = "jsonb")
    private AttributeSchema attributesSchema;

}
