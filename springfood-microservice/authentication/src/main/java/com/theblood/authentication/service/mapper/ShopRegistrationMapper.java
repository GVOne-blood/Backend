package com.theblood.authentication.service.mapper;


import com.theblood.authentication.model.ShopRegistrationRequest;
import com.theblood.springfood.client.api.ShopClient;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShopRegistrationMapper {

    ShopRegistrationRequest toEntity(ShopClient.ShopApproveDTO dto);

    ShopClient.ShopApproveResponse toShopApproveResponse(ShopRegistrationRequest shopRegistrationRequest);

    List<ShopClient.ShopApproveResponse> toShopApproveResponse(List<ShopRegistrationRequest> shopRegistrationRequest);

}
