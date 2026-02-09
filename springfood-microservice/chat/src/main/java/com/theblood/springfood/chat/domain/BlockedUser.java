package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * BlockedUser - Block list
 */
@Entity
@Table(name = "blocked_user")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BlockedUser extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "block_id", length = 50, nullable = false)
    private String blockId;

    @NotNull
    @Column(name = "blocker_id", nullable = false)
    private String blockerId;

    @NotNull
    @Column(name = "blocked_user_id", nullable = false)
    private String blockedUserId;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getBlockId() {
        return this.blockId;
    }

    public BlockedUser blockId(String blockId) {
        this.setBlockId(blockId);
        return this;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getBlockerId() {
        return this.blockerId;
    }

    public BlockedUser blockerId(String blockerId) {
        this.setBlockerId(blockerId);
        return this;
    }

    public void setBlockerId(String blockerId) {
        this.blockerId = blockerId;
    }

    public String getBlockedUserId() {
        return this.blockedUserId;
    }

    public BlockedUser blockedUserId(String blockedUserId) {
        this.setBlockedUserId(blockedUserId);
        return this;
    }

    public void setBlockedUserId(String blockedUserId) {
        this.blockedUserId = blockedUserId;
    }

    public String getReason() {
        return this.reason;
    }

    public BlockedUser reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.blockId;
    }

    @Override
    public void setId(String id) {
        this.blockId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public BlockedUser setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockedUser)) {
            return false;
        }
        return getBlockId() != null && getBlockId().equals(((BlockedUser) o).getBlockId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BlockedUser{" +
            "blockId=" + getBlockId() +
            ", blockerId='" + getBlockerId() + "'" +
            ", blockedUserId='" + getBlockedUserId() + "'" +
            ", reason='" + getReason() + "'" +
            "}";
    }
}
