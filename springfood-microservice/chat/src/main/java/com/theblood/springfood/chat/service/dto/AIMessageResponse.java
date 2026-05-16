package com.theblood.springfood.chat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMessageResponse {

    private String conversationId;
    private String message;
    private String response;
    private Instant timestamp;
    private List<ProductCard> products;
    private List<ShopCard> shops;

    public static AIMessageResponse of(String conversationId, String message, String response) {
        return new AIMessageResponse(conversationId, message, response, Instant.now(), null, null);
    }

    public static AIMessageResponse of(String conversationId, String message, String response,
                                        List<ProductCard> products, List<ShopCard> shops) {
        return new AIMessageResponse(conversationId, message, response, Instant.now(), products, shops);
    }
}
