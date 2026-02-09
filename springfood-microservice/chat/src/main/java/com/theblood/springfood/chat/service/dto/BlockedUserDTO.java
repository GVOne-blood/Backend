package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.BlockedUser} entity.
 */
@Schema(description = "BlockedUser - Block list")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BlockedUserDTO implements Serializable {

    @NotNull
    private String blockId;

    @NotNull
    private String blockerId;

    @NotNull
    private String blockedUserId;

    @Size(max = 500)
    private String reason;

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getBlockerId() {
        return blockerId;
    }

    public void setBlockerId(String blockerId) {
        this.blockerId = blockerId;
    }

    public String getBlockedUserId() {
        return blockedUserId;
    }

    public void setBlockedUserId(String blockedUserId) {
        this.blockedUserId = blockedUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockedUserDTO)) {
            return false;
        }

        BlockedUserDTO blockedUserDTO = (BlockedUserDTO) o;
        if (this.blockId == null) {
            return false;
        }
        return Objects.equals(this.blockId, blockedUserDTO.blockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.blockId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BlockedUserDTO{" +
            "blockId='" + getBlockId() + "'" +
            ", blockerId='" + getBlockerId() + "'" +
            ", blockedUserId='" + getBlockedUserId() + "'" +
            ", reason='" + getReason() + "'" +
            "}";
    }
}
