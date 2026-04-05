package com.theblood.springfood.chat.service.ai.mcp.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool Definition - Định nghĩa function schema cho Gemini Function Calling
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPToolDefinition {

    /**
     * Tên function (phải unique)
     */
    private String name;

    /**
     * Mô tả function - Gemini dùng để quyết định khi nào gọi
     */
    private String description;

    /**
     * Parameters schema (JSON Schema format)
     */
    private ParameterSchema parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterSchema {
        /**
         * Type luôn là "object"
         */
        private String type = "object";

        /**
         * Properties của parameters
         * Key: tên parameter
         * Value: schema của parameter đó
         */
        private Map<String, PropertySchema> properties;

        /**
         * Danh sách required parameters
         */
        private List<String> required;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertySchema {
        /**
         * Type: string, number, integer, boolean, array, object
         */
        private String type;

        /**
         * Mô tả parameter
         */
        private String description;

        /**
         * Enum values (nếu có)
         */
        private List<String> enumValues;

        /**
         * Items schema (cho type array)
         */
        private PropertySchema items;

        /**
         * Min/Max values (cho number/integer)
         */
        private Number minimum;
        private Number maximum;
    }
}
