package com.theblood.springfood.actionlog.domain;

import com.theblood.springfood.actionlog.domain.enumeration.ActionTypeAnnualUpdate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UuidGenerator;

/**
 * Action Log entity
 * Lưu nhật ký hệ thống
 */
@Entity
@Table(name = "log_action_annual_update")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogActionAnnualUpdate extends AbstractAuditingEntity<String> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator
    @Column(name = "log_action_annual_update_id")
    private String id;

    @Column(name = "account_id")
    private String accountId;

    @Size(max = 50)
    @Column(name = "user_name", length = 50)
    private String userName;

    @Column(name = "organization_id")
    private String organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionTypeAnnualUpdate actionType;

    @Lob
    @Column(name = "old_value")
    private String oldValue;

    @Lob
    @Column(name = "new_value")
    private String newValue;

    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 50)
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Lob
    @Column(name = "user_agent")
    private String userAgent;

    @Lob
    @Column(name = "table_name")
    private String tableName;

    @Size(max = 50)
    @Column(name = "object_id", length = 50)
    private String objectId;

    @Column(name = "affect_current")
    private Integer affectCurrent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public LogActionAnnualUpdate id(String id) {
        this.setId(id);
        return this;
    }

    public LogActionAnnualUpdate accountId(String accountId) {
        this.setAccountId(accountId);
        return this;
    }

    public LogActionAnnualUpdate userName(String userName) {
        this.setUserName(userName);
        return this;
    }

    public LogActionAnnualUpdate organizationId(String organizationId) {
        this.setOrganizationId(organizationId);
        return this;
    }

    public LogActionAnnualUpdate actionType(ActionTypeAnnualUpdate actionType) {
        this.setActionType(actionType);
        return this;
    }

    public LogActionAnnualUpdate oldValue(String oldValue) {
        this.setOldValue(oldValue);
        return this;
    }

    public LogActionAnnualUpdate newValue(String newValue) {
        this.setNewValue(newValue);
        return this;
    }

    public LogActionAnnualUpdate description(String description) {
        this.setDescription(description);
        return this;
    }

    public LogActionAnnualUpdate ipAddress(String ipAddress) {
        this.setIpAddress(ipAddress);
        return this;
    }

    public LogActionAnnualUpdate userAgent(String userAgent) {
        this.setUserAgent(userAgent);
        return this;
    }

    public LogActionAnnualUpdate tableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }

    public LogActionAnnualUpdate objectId(String objectId) {
        this.setObjectId(objectId);
        return this;
    }

    public LogActionAnnualUpdate affectCurrent(Integer affectCurrent) {
        this.setAffectCurrent(affectCurrent);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogActionAnnualUpdate)) {
            return false;
        }
        return getId() != null && getId().equals(((LogActionAnnualUpdate) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LogActionAnnualUpdate{" +
                "id=" + getId() +
                ", accountId=" + getAccountId() +
                ", userName='" + getUserName() + "'" +
                ", organizationId=" + getOrganizationId() +
                ", actionType='" + getActionType() + "'" +
                ", oldValue='" + getOldValue() + "'" +
                ", newValue='" + getNewValue() + "'" +
                ", description='" + getDescription() + "'" +
                ", ipAddress='" + getIpAddress() + "'" +
                ", userAgent='" + getUserAgent() + "'" +
                ", tableName='" + getTableName() + "'" +
                ", objectId='" + getObjectId() + "'" +
                ", affectCurrent=" + getAffectCurrent() +
                "}";
    }
}
