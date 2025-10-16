package com.theblood.shopservice.mapper;

import com.theblood.shopservice.dto.response.ShopResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShopMapper {

    ShopResponse toShopResponse(com.theblood.shopservice.model.Shop shop);
}
