package com.theblood.springfood.actionlog.service.criteria;

import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import com.theblood.springfood.actionlog.domain.enumeration.ActionTypeAnnualUpdate;
import com.theblood.springfood.actionlog.web.rest.LogActionAnnualUpdateResource;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link LogActionAnnualUpdate} entity. This class is used
 * in {@link LogActionAnnualUpdateResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /log-action-annual-updates?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LogActionAnnualUpdateCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;
    private StringFilter id;
    private StringFilter accountId;
    private StringFilter userName;
    private StringFilter organizationId;
    private ActionTypeFilter actionType;
    private StringFilter ipAddress;
    private StringFilter objectId;
    private IntegerFilter affectCurrent;
    private Boolean distinct;

    public LogActionAnnualUpdateCriteria() {
    }

    public LogActionAnnualUpdateCriteria(LogActionAnnualUpdateCriteria other) {
        this.id = other.optionalId().map(StringFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(StringFilter::copy).orElse(null);
        this.userName = other.optionalUserName().map(StringFilter::copy).orElse(null);
        this.organizationId = other.optionalOrganizationId().map(StringFilter::copy).orElse(null);
        this.actionType = other.optionalActionType().map(ActionTypeFilter::copy).orElse(null);
        this.ipAddress = other.optionalIpAddress().map(StringFilter::copy).orElse(null);
        this.objectId = other.optionalObjectId().map(StringFilter::copy).orElse(null);
        this.affectCurrent = other.optionalAffectCurrent().map(IntegerFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public LogActionAnnualUpdateCriteria copy() {
        return new LogActionAnnualUpdateCriteria(this);
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

    public StringFilter getUserName() {
        return userName;
    }

    public void setUserName(StringFilter userName) {
        this.userName = userName;
    }

    public Optional<StringFilter> optionalUserName() {
        return Optional.ofNullable(userName);
    }

    public StringFilter userName() {
        if (userName == null) {
            setUserName(new StringFilter());
        }
        return userName;
    }

    public StringFilter getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(StringFilter organizationId) {
        this.organizationId = organizationId;
    }

    public Optional<StringFilter> optionalOrganizationId() {
        return Optional.ofNullable(organizationId);
    }

    public StringFilter organizationId() {
        if (organizationId == null) {
            setOrganizationId(new StringFilter());
        }
        return organizationId;
    }

    public ActionTypeFilter getActionType() {
        return actionType;
    }

    public void setActionType(ActionTypeFilter actionType) {
        this.actionType = actionType;
    }

    public Optional<ActionTypeFilter> optionalActionType() {
        return Optional.ofNullable(actionType);
    }

    public ActionTypeFilter actionType() {
        if (actionType == null) {
            setActionType(new ActionTypeFilter());
        }
        return actionType;
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

    public StringFilter getObjectId() {
        return objectId;
    }

    public void setObjectId(StringFilter objectId) {
        this.objectId = objectId;
    }

    public Optional<StringFilter> optionalObjectId() {
        return Optional.ofNullable(objectId);
    }

    public StringFilter objectId() {
        if (objectId == null) {
            setObjectId(new StringFilter());
        }
        return objectId;
    }

    public IntegerFilter getAffectCurrent() {
        return affectCurrent;
    }

    public void setAffectCurrent(IntegerFilter affectCurrent) {
        this.affectCurrent = affectCurrent;
    }

    public Optional<IntegerFilter> optionalAffectCurrent() {
        return Optional.ofNullable(affectCurrent);
    }

    public IntegerFilter affectCurrent() {
        if (affectCurrent == null) {
            setAffectCurrent(new IntegerFilter());
        }
        return affectCurrent;
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
        final LogActionAnnualUpdateCriteria that = (LogActionAnnualUpdateCriteria) o;
        return (
                Objects.equals(id, that.id) &&
                        Objects.equals(accountId, that.accountId) &&
                        Objects.equals(userName, that.userName) &&
                        Objects.equals(organizationId, that.organizationId) &&
                        Objects.equals(actionType, that.actionType) &&
                        Objects.equals(ipAddress, that.ipAddress) &&
                        Objects.equals(objectId, that.objectId) &&
                        Objects.equals(affectCurrent, that.affectCurrent) &&
                        Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, userName, organizationId, actionType, ipAddress, objectId, affectCurrent, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LogActionAnnualUpdateCriteria{" +
                optionalId().map(f -> "id=" + f + ", ").orElse("") +
                optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
                optionalUserName().map(f -> "userName=" + f + ", ").orElse("") +
                optionalOrganizationId().map(f -> "organizationId=" + f + ", ").orElse("") +
                optionalActionType().map(f -> "actionType=" + f + ", ").orElse("") +
                optionalIpAddress().map(f -> "ipAddress=" + f + ", ").orElse("") +
                optionalObjectId().map(f -> "objectId=" + f + ", ").orElse("") +
                optionalAffectCurrent().map(f -> "affectCurrent=" + f + ", ").orElse("") +
                optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
                "}";
    }

    /**
     * Class for filtering ActionType
     */
    public static class ActionTypeFilter extends Filter<ActionTypeAnnualUpdate> {

        public ActionTypeFilter() {
        }

        public ActionTypeFilter(ActionTypeFilter filter) {
            super(filter);
        }

        @Override
        public ActionTypeFilter copy() {
            return new ActionTypeFilter(this);
        }
    }
}
