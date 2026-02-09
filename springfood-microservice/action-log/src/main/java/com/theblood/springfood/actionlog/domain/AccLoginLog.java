package com.theblood.springfood.actionlog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.theblood.common.enums.AuthType;
import com.theblood.common.enums.DeviceType;
import com.theblood.common.enums.LoginEventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.Instant;

/**
 * A AccLoginLog.
 */
@Entity
@Table(name = "acc_login_log")
@JsonIgnoreProperties(value = {"new", "id"})
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AccLoginLog extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "acc_audit_log_id", length = 50, unique = true, nullable = false)
    private String id;

    @Size(max = 50)
    @Column(name = "account_id", length = 50)
    private String accountId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", length = 20, nullable = false)
    private AuthType authType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private LoginEventType eventType;

    @Size(max = 4000)
    @Column(name = "event_details", length = 4000)
    private String eventDetails;

    @NotNull
    @Size(max = 45)
    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    @NotNull
    @Column(name = "login_attempt_time", nullable = false)
    private Instant loginAttemptTime;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AccLoginLog id(String id) {
        this.setId(id);
        return this;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public AccLoginLog accountId(String accountId) {
        this.setAccountId(accountId);
        return this;
    }

    public AuthType getAuthType() {
        return this.authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public AccLoginLog authType(AuthType authType) {
        this.setAuthType(authType);
        return this;
    }

    public LoginEventType getEventType() {
        return this.eventType;
    }

    public void setEventType(LoginEventType eventType) {
        this.eventType = eventType;
    }

    public AccLoginLog eventType(LoginEventType eventType) {
        this.setEventType(eventType);
        return this;
    }

    public String getEventDetails() {
        return this.eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public AccLoginLog eventDetails(String eventDetails) {
        this.setEventDetails(eventDetails);
        return this;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public AccLoginLog ipAddress(String ipAddress) {
        this.setIpAddress(ipAddress);
        return this;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public AccLoginLog deviceType(DeviceType deviceType) {
        this.setDeviceType(deviceType);
        return this;
    }

    public Instant getLoginAttemptTime() {
        return this.loginAttemptTime;
    }

    public void setLoginAttemptTime(Instant loginAttemptTime) {
        this.loginAttemptTime = loginAttemptTime;
    }

    public AccLoginLog loginAttemptTime(Instant loginAttemptTime) {
        this.setLoginAttemptTime(loginAttemptTime);
        return this;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public AccLoginLog setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccLoginLog)) {
            return false;
        }
        return getId() != null && getId().equals(((AccLoginLog) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AccLoginLog{" +
                "accAuditLogId=" + getId() +
                ", accountId='" + getAccountId() + "'" +
                ", authType='" + getAuthType() + "'" +
                ", eventType='" + getEventType() + "'" +
                ", eventDetails='" + getEventDetails() + "'" +
                ", ipAddress='" + getIpAddress() + "'" +
                ", deviceType='" + getDeviceType() + "'" +
                ", loginAttemptTime='" + getLoginAttemptTime() + "'" +
                "}";
    }
}
