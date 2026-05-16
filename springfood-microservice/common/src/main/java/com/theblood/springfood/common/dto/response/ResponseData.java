package com.theblood.springfood.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Standard wrapper used by every REST controller.
 *
 * <p>The on-the-wire JSON shape exposes the status code under <b>two</b>
 * field names:
 * <ul>
 *   <li>{@code appStatus} — the canonical name produced by Lombok's getter.</li>
 *   <li>{@code code} — a duplicate, emitted via {@code @JsonProperty} so that
 *       FE clients written against either contract keep working. Several
 *       Angular services check {@code response.code === 200}; without this
 *       alias their predicates silently fail and UI components stay in their
 *       loading state forever.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
public class ResponseData<T> {

    private int appStatus;
    private String message;
    private T data;

    public ResponseData(int appStatus, String message) {
        this.appStatus = appStatus;
        this.message = message;
    }

    public ResponseData(int appStatus, String message, T data) {
        this.appStatus = appStatus;
        this.message = message;
        this.data = data;
    }

    /**
     * Backward-compat alias: emit the same status under the {@code code} key
     * so legacy/Angular callers that rely on {@code response.code} keep
     * working without forcing every controller to change its DTO.
     */
    @JsonProperty("code")
    public int getCode() {
        return appStatus;
    }
}
