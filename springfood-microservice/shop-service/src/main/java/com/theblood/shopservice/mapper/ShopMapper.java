package com.theblood.shopservice.mapper;

import com.theblood.shopservice.dto.response.ShopResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShopMapper {
    @Mapping(source = "id", target = "shopId")
    ShopResponse toShopResponse(com.theblood.shopservice.model.Shop shop);
}
