package com.theblood.shopservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.Persistable;

/**
 * A ShopMember.
 */
@Entity
@Table(name = "shop_member")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ShopMember implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(max = 50)
    @Id
    @Column(name = "shop_member_id", length = 50, nullable = false)
    private String shopMemberId;

    @Size(max = 50)
    @Column(name = "shop_id", length = 50)
    private String shopId;

    @Size(max = 50)
    @Column(name = "user_id", length = 50)
    private String userId;

    @Size(max = 255)
    @Column(name = "role_name", length = 255)
    private String roleName;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Size(max = 50)
    @Column(name = "department", length = 50)
    private String department;

    @Size(max = 50)
    @Column(name = "join_date", length = 50)
    private String joinDate;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Size(max = 50)
    @Column(name = "end_date", length = 50)
    private String endDate;

    @Size(max = 255)
    @Column(name = "work_schedule", length = 255)
    private String workSchedule;

    @Size(max = 50)
    @Column(name = "salary_type", length = 50)
    private String salaryType;

    @Column(name = "base_salary", precision = 21, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "commission", precision = 21, scale = 2)
    private BigDecimal commission;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getShopMemberId() {
        return this.shopMemberId;
    }

    public ShopMember shopMemberId(String shopMemberId) {
        this.setShopMemberId(shopMemberId);
        return this;
    }

    public void setShopMemberId(String shopMemberId) {
        this.shopMemberId = shopMemberId;
    }

    public String getShopId() {
        return this.shopId;
    }

    public ShopMember shopId(String shopId) {
        this.setShopId(shopId);
        return this;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getUserId() {
        return this.userId;
    }

    public ShopMember userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public ShopMember roleName(String roleName) {
        this.setRoleName(roleName);
        return this;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public ShopMember createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public ShopMember updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDepartment() {
        return this.department;
    }

    public ShopMember department(String department) {
        this.setDepartment(department);
        return this;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJoinDate() {
        return this.joinDate;
    }

    public ShopMember joinDate(String joinDate) {
        this.setJoinDate(joinDate);
        return this;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return this.status;
    }

    public ShopMember status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public ShopMember endDate(String endDate) {
        this.setEndDate(endDate);
        return this;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getWorkSchedule() {
        return this.workSchedule;
    }

    public ShopMember workSchedule(String workSchedule) {
        this.setWorkSchedule(workSchedule);
        return this;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }

    public String getSalaryType() {
        return this.salaryType;
    }

    public ShopMember salaryType(String salaryType) {
        this.setSalaryType(salaryType);
        return this;
    }

    public void setSalaryType(String salaryType) {
        this.salaryType = salaryType;
    }

    public BigDecimal getBaseSalary() {
        return this.baseSalary;
    }

    public ShopMember baseSalary(BigDecimal baseSalary) {
        this.setBaseSalary(baseSalary);
        return this;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getCommission() {
        return this.commission;
    }

    public ShopMember commission(BigDecimal commission) {
        this.setCommission(commission);
        return this;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.shopMemberId;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public ShopMember setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShopMember)) {
            return false;
        }
        return getShopMemberId() != null && getShopMemberId().equals(((ShopMember) o).getShopMemberId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ShopMember{" +
            "shopMemberId=" + getShopMemberId() +
            ", shopId='" + getShopId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", roleName='" + getRoleName() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", department='" + getDepartment() + "'" +
            ", joinDate='" + getJoinDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", workSchedule='" + getWorkSchedule() + "'" +
            ", salaryType='" + getSalaryType() + "'" +
            ", baseSalary=" + getBaseSalary() +
            ", commission=" + getCommission() +
            "}";
    }
}
