package com.theblood.productservice.common.enums;

import lombok.Data;

import java.util.List;

public class AttributeSchema {


    private List<AttributeDefinition> attributes;

    @Data
    public class AttributeDefinition {
        private String key;           // "color", "size", "ram"
        private String label;         // "Màu sắc", "Kích thước", "RAM"
        private String type;          // "select", "text", "number"
        private List<String> options; // ["Đỏ", "Xanh", "Vàng"] for select
        private Boolean required;
        private Integer displayOrder;
    }
}
