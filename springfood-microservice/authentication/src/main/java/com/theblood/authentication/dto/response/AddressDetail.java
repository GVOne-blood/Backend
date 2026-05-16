package com.theblood.authentication.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Address response DTO. Gửi đầy đủ field structured cho FE thay vì concat
 * thành 1 string như version cũ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressDetail {
    String id;
    String label;
    String recipientName;
    String phoneNumber;
    String streetAddress;       // = Address.street, đặt tên match FE
    String ward;
    String district;
    String city;
    String details;             // mô tả thêm (free text)

    /**
     * @JsonProperty buộc Jackson serialize key thành "isDefault" thay vì "default"
     * (Lombok generate isDefault() getter cho boolean field, Jackson mặc định
     * strip prefix "is" nên ra "default" — không match shape FE expect).
     */
    @JsonProperty("isDefault")
    boolean isDefault;
}
