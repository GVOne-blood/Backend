package com.theblood.springfood.actionlog.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Request DTO for finding log actions by table name and object ID.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogActionRequest {

    /**
     * Table name to search for
     */
    @NotNull
    @Size(max = 100)
    @Schema(description = "Table name to search for", required = true, example = "DOCUMENT")
    private String tableName;

    /**
     * Object ID to search for
     */
    @NotNull
    @Size(max = 50)
    @Schema(description = "Object ID to search for", required = true, example = "123456")
    private String objectId;

    /**
     * Sort field and direction (e.g., "createdDate,desc")
     */
    @Schema(description = "Sort field and direction (e.g., 'createdDate,desc')", example = "createdDate,desc")
    private String sort;
}
