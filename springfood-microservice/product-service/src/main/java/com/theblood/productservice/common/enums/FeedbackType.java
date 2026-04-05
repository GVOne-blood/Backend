package com.theblood.productservice.common.enums;


public enum FeedbackType {

    PRODUCT_FEEDBACK("Phản hồi của khách hàng {username} về sản phẩm {productName}"),
    SHOP_FEEDBACK("Phản hồi của khách hàng {username} về cửa hàng"),
    SHOP_REPLY("Phản hồi của cửa hàng");

    private String title;

    FeedbackType(String title) {
        this.title = title;
    }
}
