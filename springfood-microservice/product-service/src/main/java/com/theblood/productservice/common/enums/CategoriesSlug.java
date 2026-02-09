package com.theblood.productservice.common.enums;

public enum CategoriesSlug {

    HOAQUA("hoa-qua", "Hoa quả");

    private String slug;
    private String name;
    CategoriesSlug(String slug, String name) {
        this.slug = slug;
        this.name = name;
    }

}
