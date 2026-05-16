package com.theblood.springfood.chat.service.ai.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductTools {

    private final ObjectMapper objectMapper;

    public ProductTools(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Get product details and related products. Use this when user asks about a specific product, wants recommendations, or needs product information")
    public String getProductRelated(
        @ToolParam(description = "JSON string with product info: {\"productId\": \"...\", \"productName\": \"...\"}") String productInput
    ) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> input = objectMapper.readValue(productInput, Map.class);
            String productId = (String) input.getOrDefault("productId", "");
            String productName = (String) input.getOrDefault("productName", "");

            StringBuilder result = new StringBuilder();
            result.append("Product search: ");
            if (!productId.isEmpty()) result.append("ID=").append(productId).append(" ");
            if (!productName.isEmpty()) result.append("Name=").append(productName);
            return result.toString();
        } catch (Exception e) {
            return "Unable to parse product input: " + e.getMessage();
        }
    }
}
