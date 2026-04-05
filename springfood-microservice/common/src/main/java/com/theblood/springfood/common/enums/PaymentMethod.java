package com.theblood.springfood.common.enums;

import java.util.List;

public enum PaymentMethod {
    VNPAY("Thanh toán online qua ngân hàng số VNPay",
            List.of("NCB", "MB")),
    COD;

    private String description;
    private List<String> support;

    PaymentMethod() {
    }

    PaymentMethod(String des, List<String> support) {
        this.description = des;
        this.support = support;
    }
}
