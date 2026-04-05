package com.theblood.springfood.chat.service.ai.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProductTools {

    @Tool(description = "Get product related about this product input. Return a list of product related (max 3 products info)")
    public String getProductRelated(
        @ToolParam(description = "JSON format for product input") Object productInput,
        @ToolParam(description = "")
    ){

    }
}
