package com.theblood.shopservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Response DTO for shop detail page
 * Contains all information needed to display shop detail view
 *
 * <p>{@link JsonIgnoreProperties} keeps the deserializer tolerant when the
 * service maps via {@code ObjectMapper#convertValue} — the source entity
 * extends an auditing base class whose {@code createdDate}/{@code lastModifiedDate}
 * getters are JSON-visible.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopDetailResponse {
    
    @JsonProperty("shopId")
    String shopId;
    
    @JsonProperty("shopName")
    String shopName;
    
    @JsonProperty("logo")
    String logo;
    
    @JsonProperty("introduction")
    String introduction;
    
    @JsonProperty("shopAddress")
    String shopAddress;
    
    @JsonProperty("city")
    String city;
    
    @JsonProperty("province")
    String province;
    
    @JsonProperty("avgStar")
    BigDecimal avgStar;
    
    @JsonProperty("totalFeedback")
    Integer totalFeedback;
    
    @JsonProperty("activeHours")
    String activeHours;
    
    @JsonProperty("distance")
    Double distance; // Distance in km (calculated based on user location)
    
    @JsonProperty("totalProducts")
    Integer totalProducts;
    
    @JsonProperty("totalSold")
    Integer totalSold;
    
    @JsonProperty("totalOrders")
    Integer totalOrders;
    
    @JsonProperty("phoneNumber")
    String phoneNumber;
    
    @JsonProperty("email")
    String email;
    
    @JsonProperty("shopStatus")
    String shopStatus;
    
    @JsonProperty("isActive")
    Integer isActive;
    
    @JsonProperty("shopType")
    String shopType;
    
    @JsonProperty("businessType")
    String businessType;
}
