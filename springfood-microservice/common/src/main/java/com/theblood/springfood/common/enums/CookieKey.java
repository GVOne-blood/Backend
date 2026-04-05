package com.theblood.springfood.common.enums;

public enum CookieKey {
    ACCESS_TOKEN("X-Authorization"),
    REFRESH_TOKEN("X-F5");

    private String header;

    CookieKey() {
    }

    ;

    CookieKey(String code) {
        this.header = code;
    }


}
