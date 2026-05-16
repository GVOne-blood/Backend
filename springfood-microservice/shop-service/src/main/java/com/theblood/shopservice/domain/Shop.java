package com.theblood.shopservice.domain;

import com.theblood.shopservice.common.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shops")
public class Shop extends AbstractAuditingEntity<UUID> implements Serializable {

    @Id
    @UuidGenerator
    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "ownerId")
    private String ownerId;

    @Column(name = "shop_name", length = 255)
    private String shopName;

    @Column(name = "logo", length = 255)
    private String logo;

    @Column(name = "introduction", length = 2000)
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_status", length = 255)
    private ShopStatus shopStatus;

    @Column(name = "total_product")
    private Integer totalProduct;

    @Column(name = "total_sold")
    private Integer totalSold;

//    @Column(name = "created_at")
//    private Instant createdAt;
//
//    @Column(name = "updated_at")
//    private Instant updatedAt;

    @Column(name = "shop_type", length = 50)
    private String shopType;

    @Column(name = "total_traffic")
    private Integer totalTraffic;

    @Column(name = "avg_star", precision = 2, scale = 2)
    private BigDecimal avgStar;

    @Column(name = "total_feedback")
    private Integer totalFeedback;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "business_type", length = 50)
    private String businessType;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "is_bln")
    private Integer isBln;

    @Column(name = "is_active")
    private Integer isActive;

    @Column(name = "commission", precision = 2, scale = 4)
    private BigDecimal commission;

    @Column(name = "shop_address", length = 255)
    private String shopAddress;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "nation_id", length = 50)
    private String nationId;

    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Column(name = "active_hours", length = 1000)
    private String activeHours;

    @Column(name = "contract_start_date")
    private Instant contractStartDate;

    @Column(name = "contract_end_date")
    private Instant contractEndDate;

    @Column(name = "shop_level")
    private Integer shopLevel;

    // ----- Admin moderation fields ------------------------------------
    // Hibernate ddl-auto=update sẽ tự ALTER TABLE thêm 3 cột này khi service
    // restart lần đầu. Không có Liquibase changelog vì project hiện tại đang
    // dùng auto-migration cho convenience trong dev.

    /**
     * Lý do ban gần nhất nếu shop từng bị admin ban. Null khi chưa từng bị ban
     * (kể cả sau khi unban thì giữ lại lịch sử cuối cùng).
     */
    @Column(name = "banned_reason", length = 1000)
    private String bannedReason;

    /** Thời điểm shop bị ban gần nhất, null nếu chưa từng. */
    @Column(name = "banned_at")
    private Instant bannedAt;

    /** Username của admin thực hiện ban gần nhất. */
    @Column(name = "banned_by", length = 100)
    private String bannedBy;

    @Override
    public UUID getId() {
        return this.shopId;
    }
}
