package com.theblood.authentication.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "address")
@AttributeOverride(name = "id", column = @Column(name = "address_id"))
public class Address extends AbstractEntity {

    /**
     * Nhãn cho địa chỉ, vd "Nhà", "Công ty". Free text.
     */
    @Column(length = 100)
    private String label;

    /**
     * Tên người nhận. Có thể khác với user owner (user mua hộ).
     */
    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    /**
     * Số điện thoại người nhận.
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Tên phường/xã.
     */
    private String ward;

    /**
     * Số nhà + tên đường.
     */
    private String street;

    /**
     * Tỉnh/thành phố.
     */
    private String city;

    /**
     * Tên quận/huyện. Tách khỏi `details` để FE render riêng.
     */
    @Column(length = 100)
    private String district;

    /**
     * Mô tả thêm (số tầng, ghi chú giao hàng…). Optional.
     */
    @Column(length = 500)
    private String details;

    /**
     * Cờ địa chỉ mặc định. Mỗi user chỉ nên có 1 default — service tự enforce.
     */
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
