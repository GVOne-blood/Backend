package com.theblood.shopservice.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Lightweight shop summary used in listing endpoints.
 *
 * <p>The service maps from the {@link com.theblood.shopservice.domain.Shop}
 * entity via {@code ObjectMapper#convertValue}, which goes through a JSON
 * intermediate. {@code AbstractAuditingEntity} declares
 * {@code allowGetters = true} for {@code createdDate}/{@code lastModifiedDate},
 * so those keys appear in the intermediate even though we don't want them on
 * the wire. {@link JsonIgnoreProperties} keeps the deserializer tolerant so a
 * future field added to the entity doesn't break this DTO at runtime.</p>
 *
 * <p>The {@code totalProducts} field also uses {@link JsonAlias} to accept the
 * legacy {@code totalProduct} key (no trailing 's') because the underlying
 * {@code Shop} entity column is {@code total_product}/{@code totalProduct}.
 * Without the alias the {@code convertValue} round-trip silently dropped the
 * value, leaving every shop in the listing response with
 * {@code totalProducts: null}. {@link JsonProperty} pins the wire field name
 * to {@code totalProducts} so the FE doesn't have to learn the legacy key.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopResponse {
    UUID shopId;
    String shopName;
    String logo;
    String introduction;

    /**
     * Number of products the shop is selling. Backed by {@code Shop#totalProduct}
     * (singular) on the entity — see class doc for context on the alias.
     */
    @JsonProperty("totalProducts")
    @JsonAlias({"totalProduct"})
    Integer totalProducts;

    Integer totalSold;
}
