package com.theblood.springfood.actionlog.service.criteria;

import com.theblood.springfood.common.enums.AuthType;
import com.theblood.springfood.common.enums.DeviceType;
import com.theblood.springfood.common.enums.LoginEventType;
import com.theblood.springfood.actionlog.domain.AccLoginLog;
import com.theblood.springfood.actionlog.web.rest.LogActionResource;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.StringFilter;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Criteria class for the {@link AccLoginLog} entity. This class is used
 * in {@link LogActionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /acc-login-logs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AccLoginLogCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;
    private StringFilter id;
    private StringFilter accountId;
    private AuthTypeFilter authType;
    private LoginEventTypeFilter eventType;
    private StringFilter eventDetails;
    private StringFilter ipAddress;
    private DeviceTypeFilter deviceType;
    private InstantFilter loginAttemptTime;
    private Boolean distinct;

    public AccLoginLogCriteria() {
    }

    public AccLoginLogCriteria(AccLoginLogCriteria other) {
        this.id = other.optionalId().map(StringFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(StringFilter::copy).orElse(null);
        this.authType = other.optionalAuthType().map(AuthTypeFilter::copy).orElse(null);
        this.eventType = other.optionalEventType().map(LoginEventTypeFilter::copy).orElse(null);
        this.eventDetails = other.optionalEventDetails().map(StringFilter::copy).orElse(null);
        this.ipAddress = other.optionalIpAddress().map(StringFilter::copy).orElse(null);
        this.deviceType = other.optionalDeviceType().map(DeviceTypeFilter::copy).orElse(null);
        this.loginAttemptTime = other.optionalLoginAttemptTime().map(InstantFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AccLoginLogCriteria copy() {
        return new AccLoginLogCriteria(this);
    }

    public StringFilter getId() {
        return id;
    }

    public void setId(StringFilter id) {
        this.id = id;
    }

    public Optional<StringFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public StringFilter id() {
        if (id == null) {
            setId(new StringFilter());
        }
        return id;
    }

    public StringFilter getAccountId() {
        return accountId;
    }

    public void setAccountId(StringFilter accountId) {
        this.accountId = accountId;
    }

    public Optional<StringFilter> optionalAccountId() {
        return Optional.ofNullable(accountId);
    }

    public StringFilter accountId() {
        if (accountId == null) {
            setAccountId(new StringFilter());
        }
        return accountId;
    }

    public AuthTypeFilter getAuthType() {
        return authType;
    }

    public void setAuthType(AuthTypeFilter authType) {
        this.authType = authType;
    }

    public Optional<AuthTypeFilter> optionalAuthType() {
        return Optional.ofNullable(authType);
    }

    public AuthTypeFilter authType() {
        if (authType == null) {
            setAuthType(new AuthTypeFilter());
        }
        return authType;
    }

    public LoginEventTypeFilter getEventType() {
        return eventType;
    }

    public void setEventType(LoginEventTypeFilter eventType) {
        this.eventType = eventType;
    }

    public Optional<LoginEventTypeFilter> optionalEventType() {
        return Optional.ofNullable(eventType);
    }

    public LoginEventTypeFilter eventType() {
        if (eventType == null) {
            setEventType(new LoginEventTypeFilter());
        }
        return eventType;
    }

    public StringFilter getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(StringFilter eventDetails) {
        this.eventDetails = eventDetails;
    }

    public Optional<StringFilter> optionalEventDetails() {
        return Optional.ofNullable(eventDetails);
    }

    public StringFilter eventDetails() {
        if (eventDetails == null) {
            setEventDetails(new StringFilter());
        }
        return eventDetails;
    }

    public StringFilter getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(StringFilter ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Optional<StringFilter> optionalIpAddress() {
        return Optional.ofNullable(ipAddress);
    }

    public StringFilter ipAddress() {
        if (ipAddress == null) {
            setIpAddress(new StringFilter());
        }
        return ipAddress;
    }

    public DeviceTypeFilter getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceTypeFilter deviceType) {
        this.deviceType = deviceType;
    }

    public Optional<DeviceTypeFilter> optionalDeviceType() {
        return Optional.ofNullable(deviceType);
    }

    public DeviceTypeFilter deviceType() {
        if (deviceType == null) {
            setDeviceType(new DeviceTypeFilter());
        }
        return deviceType;
    }

    public InstantFilter getLoginAttemptTime() {
        return loginAttemptTime;
    }

    public void setLoginAttemptTime(InstantFilter loginAttemptTime) {
        this.loginAttemptTime = loginAttemptTime;
    }

    public Optional<InstantFilter> optionalLoginAttemptTime() {
        return Optional.ofNullable(loginAttemptTime);
    }

    public InstantFilter loginAttemptTime() {
        if (loginAttemptTime == null) {
            setLoginAttemptTime(new InstantFilter());
        }
        return loginAttemptTime;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AccLoginLogCriteria that = (AccLoginLogCriteria) o;
        return (
                Objects.equals(id, that.id) &&
                        Objects.equals(accountId, that.accountId) &&
                        Objects.equals(authType, that.authType) &&
                        Objects.equals(eventType, that.eventType) &&
                        Objects.equals(eventDetails, that.eventDetails) &&
                        Objects.equals(ipAddress, that.ipAddress) &&
                        Objects.equals(deviceType, that.deviceType) &&
                        Objects.equals(loginAttemptTime, that.loginAttemptTime) &&
                        Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, authType, eventType, eventDetails, ipAddress, deviceType, loginAttemptTime, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AccLoginLogCriteria{" +
                optionalId().map(f -> "accAuditLogId=" + f + ", ").orElse("") +
                optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
                optionalAuthType().map(f -> "authType=" + f + ", ").orElse("") +
                optionalEventType().map(f -> "eventType=" + f + ", ").orElse("") +
                optionalEventDetails().map(f -> "eventDetails=" + f + ", ").orElse("") +
                optionalIpAddress().map(f -> "ipAddress=" + f + ", ").orElse("") +
                optionalDeviceType().map(f -> "deviceType=" + f + ", ").orElse("") +
                optionalLoginAttemptTime().map(f -> "loginAttemptTime=" + f + ", ").orElse("") +
                optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
                "}";
    }

    /**
     * Class for filtering LoginEventType
     */
    public static class LoginEventTypeFilter extends Filter<LoginEventType> {

        public LoginEventTypeFilter() {
        }

        public LoginEventTypeFilter(LoginEventTypeFilter filter) {
            super(filter);
        }

        @Override
        public LoginEventTypeFilter copy() {
            return new LoginEventTypeFilter(this);
        }
    }

    public static class AuthTypeFilter extends Filter<AuthType> {
        public AuthTypeFilter() {
        }

        public AuthTypeFilter(AuthTypeFilter filter) {
            super(filter);
        }

        @Override
        public AuthTypeFilter copy() {
            return new AuthTypeFilter(this);
        }
    }

    public static class DeviceTypeFilter extends Filter<DeviceType> {
        public DeviceTypeFilter() {
        }

        public DeviceTypeFilter(DeviceTypeFilter filter) {
            super(filter);
        }

        @Override
        public DeviceTypeFilter copy() {
            return new DeviceTypeFilter(this);
        }
    }
}
