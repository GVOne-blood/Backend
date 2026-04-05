package com.theblood.springfood.chat.service.ai.mcp.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP Tool Result - Kết quả sau khi execute tool
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPToolResult {

    /**
     * Tên tool đã execute
     */
    private String toolName;

    /**
     * Success flag
     */
    private boolean success;

    /**
     * Kết quả (JSON string hoặc plain text)
     */
    private Object result;

    /**
     * Error message (nếu có)
     */
    private String error;

    /**
     * Execution time (ms)
     */
    private Long executionTimeMs;

    public static MCPToolResult success(String toolName, Object result, Long executionTimeMs) {
        return MCPToolResult.builder()
            .toolName(toolName)
            .success(true)
            .result(result)
            .executionTimeMs(executionTimeMs)
            .build();
    }

    public static MCPToolResult error(String toolName, String error) {
        return MCPToolResult.builder()
            .toolName(toolName)
            .success(false)
            .error(error)
            .build();
    }
}
