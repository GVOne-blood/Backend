package com.theblood.productservice.service.mapper;

import com.theblood.productservice.domain.Feedback;
import com.theblood.productservice.domain.Product;
import com.theblood.productservice.service.dto.request.FeedbackRequest;
import com.theblood.productservice.service.dto.response.FeedbackResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    /**
     * Map entity → response DTO. Most fields are 1-1 except for
     *
     * - {@code productId} comes from the related {@link Product} entity, so
     *   we tell MapStruct to use {@link #productToProductId(Product)} to
     *   pull the UUID out as a string.
     * - {@code rate} mirrors the integer {@code rating} column.
     * - {@code feedbackType} keeps its enum name in JSON.
     */
    @Mapping(source = "product", target = "productId", qualifiedByName = "productToProductId")
    @Mapping(source = "shopId", target = "shopId")
    @Mapping(source = "productVariantsId", target = "productVariantsId")
    @Mapping(source = "rating", target = "rate")
    @Mapping(source = "feedbackType", target = "feedbackType")
    @Mapping(source = "feedbackTitle", target = "feedbackTitle")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "user_id", target = "userId")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    FeedbackResponse toDto(Feedback feedback);

    /**
     * Map request → entity. The caller supplies {@code productId} (UUID); we
     * reconstruct a stub {@link Product} with just the ID set so JPA can
     * persist the FK without loading the full row.
     *
     * Audit fields (id, createdAt, updatedAt, isActive, user_id) are populated
     * later in the service layer.
     */
    @Mapping(source = "productId", target = "product", qualifiedByName = "productIdToProduct")
    @Mapping(source = "type", target = "feedbackType")
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "mediaFileId", target = "mediaFileId")
    @Mapping(source = "shopId", target = "shopId")
    @Mapping(source = "productVariantsId", target = "productVariantsId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "user_id", ignore = true)
    @Mapping(target = "feedbackTitle", ignore = true)
    Feedback toEntity(FeedbackRequest feedbackRequest);

    @Named("productToProductId")
    default String productToProductId(Product product) {
        return product != null && product.getId() != null ? product.getId().toString() : null;
    }

    @Named("productIdToProduct")
    default Product productIdToProduct(UUID productId) {
        if (productId == null) return null;
        Product p = new Product();
        p.setId(productId);
        return p;
    }
}
